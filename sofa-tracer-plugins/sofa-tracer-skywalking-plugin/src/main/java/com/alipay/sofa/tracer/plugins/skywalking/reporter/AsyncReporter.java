/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.tracer.plugins.skywalking.reporter;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class AsyncReporter implements Closeable, Flushable {
    final AtomicBoolean          closed             = new AtomicBoolean(false);
    final ReentrantLock lock      = new ReentrantLock(false);
    final Condition     available = lock.newCondition();
    final Segment[]     segments;
    final int[] sizesInBytes;
    // 缓冲数组的大小
    final int                    maxBufferSize;
    SkywalkingRestTemplateSender sender;
    //记录缓存中当前有多少个segment
    int                          count              = 0;
    int                          writePos           = 0;
    int                          readPos            = 0;
    // 在关闭reporter线程的时候应该先等数据上报完成
    final CountDownLatch         close              = new CountDownLatch(1);

    // 是sender的最大值zipkin中是默认值2M
    int messageMaxBytes = 2 * 1024 * 1024;
    // 默认是1秒
    long messageTimeoutNanos = TimeUnit.SECONDS.toNanos(1);
    long closeTimeoutNanos = TimeUnit.SECONDS.toNanos(1);

    /**
     *
     * 创建reporter的同时创建一个定时任务
     * @param maxBufferSize Segment缓存数组的最大容量
     * @param sender 发送segments的sender
     */
    public AsyncReporter(int maxBufferSize, SkywalkingRestTemplateSender sender) {
        this.maxBufferSize = maxBufferSize;
        this.segments = new Segment[maxBufferSize];
        this.sizesInBytes = new int[maxBufferSize];
        this.sender = sender;
        // 存放消息缓冲区
        final Message message = new Message(messageMaxBytes, messageTimeoutNanos);
        final Thread flushThread = new Thread("AsyncReporter{" + sender + "}") {
            @Override public void run() {
                try {
                    while (!closed.get()) {
                        //没有结束不断的刷新
                        flush(message);
                    }
                } catch (RuntimeException | Error e) {
                    SelfLog.warn("Unexpected error flushing spans" , e);
                } finally {

                    if (count > 0) {
                        SelfLog.warn("Dropped " + count + " spans due to AsyncReporter.close()");
                    }
                    close.countDown();
                }
            }
        };
        flushThread.setDaemon(true);
        flushThread.start();
    }

    public void report(Segment segment, int segmentByteSize) {
        if (segment == null)
            throw new NullPointerException("segment == null");
        //如果reporter已经关闭或者添加到队列中失败直接把segment丢弃
        if (closed.get() || !addSegment(segment, segmentByteSize)) {
            SelfLog.warn("Dropped one segment because reporter is close or segment queue is full ");
        }
    }

    /**
     * flush 创建的线程不断的调用如果达到时间显示或者是容量限制就发送数据
     * @param message segment的缓冲区
     */

    public void flush(Message message){
        // 如果已经结束了抛出异常
        if (closed.get()) throw new IllegalStateException("closed");
        // 把数据加入到message队列中
        drainTo(message, message.remainingNanos());
        // 判断是不是可以发送message中的数据了
        // loop around if we are running, and the bundle isn't full
        // if we are closed, try to send what's pending
        if (!message.isReady() && !closed.get()) return;
        // 发送message中的数据
        try {
            sender.post(message.getMessage());
        } catch (RuntimeException | Error t) {
            // In failure case, we increment messages and spans dropped.
            int count = message.getCount();
            SelfLog.warn("Dropped " + count + " spans", t);
            // Raise in case the sender was closed out-of-band.
            if (t instanceof IllegalStateException) throw (IllegalStateException) t;
        } finally {
            //发送结束重置message
            message.reset();
        }
    }

    /**
     * 向数组中加入segment，如果添加成功返回true，添加失败返回false
     */
    private boolean addSegment(Segment segment, int segmentByteSize) {
        lock.lock();
        try {
            //队列中已经满了
            if (count == maxBufferSize)
                return false;
            segments[writePos] = segment;
            sizesInBytes[writePos] = segmentByteSize;
            writePos++;
            //当新的位置超出界限过后，回到头部
            if (writePos == maxBufferSize)
                writePos = 0;
            count++;
            // 加入成功，通知等待的
            available.signal(); // alert any drainers
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 把数组中的数据清空返回清空的segment的数量，可以统计丢失segment数量
     */
    private int clear() {
        lock.lock();
        try {
            int result = count;
            count = readPos = writePos = 0;
            Arrays.fill(segments, null);
            return result;
        } finally {
            lock.unlock();
        }
    }


    /**
     *  Blocks for up to nanosTimeout for spans to appear. Then, consume as many as possible.
     *  把当前queue中的数据加入到message中
     * @param message
     * @param nanosTimeout message中的remainingNanos
     * @return
     */
    int drainTo(Message message, long nanosTimeout) {
        try {
            // This may be called by multiple threads. If one is holding a lock, another is waiting. We
            // use lockInterruptibly to ensure the one waiting can be interrupted.
            lock.lockInterruptibly();
            try {
                long nanosLeft = nanosTimeout;
                while (count == 0) {
                    if (nanosLeft <= 0) return 0;
                    nanosLeft = available.awaitNanos(nanosLeft);
                }
                return doDrain(message);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            return 0;
        }
    }

    int doDrain(Message message) {
        int drainedCount = 0;
        // 尽可能读segment到message中除非时间和容量限制
        while (drainedCount < count) {
            Segment next = segments[readPos];
            int nextSizeInBytes = sizesInBytes[readPos];

            if (next == null) break;
            if (message.offer(next, nextSizeInBytes)) {
                drainedCount++;
                segments[readPos] = null;
                sizesInBytes[readPos] = 0;
                if (++readPos == segments.length) readPos = 0; // circle back to the front of the array
            } else {
                break;
            }
        }
        count -= drainedCount;
        return drainedCount;
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) return; // already closed
        try {
            // wait for in-flight spans to send
            if (!close.await(closeTimeoutNanos, TimeUnit.NANOSECONDS)) {
                SelfLog.warn("Timed out waiting for in-flight spans to send");
            }
        } catch (InterruptedException e) {
            SelfLog.warn("Interrupted waiting for in-flight spans to send");
            Thread.currentThread().interrupt();
        }
        int count = clear();
        if (count > 0) {
            SelfLog.warn("Dropped " + count + " spans due to AsyncReporter.close()");
        }
    }

    @Override public void flush() throws IOException {
        // timeoutNanos 设置成0马上发送所有缓存的segment
        flush(new Message(messageMaxBytes,0L));
    }
}



