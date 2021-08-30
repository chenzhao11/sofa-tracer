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

import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;

import java.io.Closeable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncReporter implements Closeable {
    static final Logger          logger             = Logger.getLogger(AsyncReporter.class
                                                        .getName());
    final AtomicBoolean          closed             = new AtomicBoolean(false);
    final ReentrantLock          lock               = new ReentrantLock(false);
    final Segment[]              segments;
    final int                    maxSize;
    SkywalkingRestTemplateSender sender;
    //记录缓存中当前有多少个segment
    int                          count              = 0;
    int                          writePos           = 0;
    int                          readPos            = 0;
    //已将成功上报的segment数目
    int                          segmentUplinkedNum = 0;
    //丢弃的segment数目
    int                          segmentDroppedNum  = 0;
    //上次记录相关数量的时间
    Long                         lastLogTime;
    //关闭reporter之前的等待时间(毫秒)
    final long                   closeTimeoutMill   = 1000;
    // 在关闭reporter线程的时候应该先等数据上报完成
    final CountDownLatch         close              = new CountDownLatch(1);

    /**
     *
     * 创建reporter的同时创建一个定时任务
     * @param maxBufferSize Segment缓存数组的最大容量
     * @param sender 发送segments的sender
     * @param flushInterval 发送数据的间隔
     */
    public AsyncReporter(int maxBufferSize, SkywalkingRestTemplateSender sender, int flushInterval) {
        this.maxSize = maxBufferSize;
        this.segments = new Segment[maxBufferSize];
        this.sender = sender;
        this.lastLogTime = System.currentTimeMillis();
        //需要创建一个定时任务不断从上面队列中取出数据上传
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                boolean isClosed = closed.get();
                List<Segment> segmentList = getAllSegments();

                try {
                    //检查reporter是否关闭如果关闭就停止定时任务
                    if (isClosed) {
                        scheduler.shutdown();
                    }
                    //使用sender发送组装好的segment数据
                    if (segmentList.isEmpty()) {
                        return;
                    }

                    //不用根据返回的情况看是否成功？
                    System.out.println("reporter中进入sender：");
                    // if fail,  add metric
                    if (!sender.post(segmentList)) {
                        //need to lock?
                        segmentDroppedNum += segmentList.size();
                    }
                    ;
                    System.out.println("reporter发送成功！");
                    segmentUplinkedNum += segmentList.size();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Unexpected exception during sending segments", e);
                    // 发送失败需要添加segmentDroppedNum
                    segmentDroppedNum += segmentList.size();
                } finally {
                    //每30S记录一次日志
                    if (System.currentTimeMillis() - lastLogTime > 30000) {
                        if (segmentUplinkedNum > 0) {
                            logger.log(Level.INFO, String.format(
                                "%d trace segments have been sent to collector.",
                                segmentUplinkedNum));
                            segmentUplinkedNum = 0;
                        }
                        if (segmentDroppedNum > 0) {
                            logger.log(Level.INFO, String.format(
                                "%d trace segments have been abandoned", segmentDroppedNum));
                            segmentDroppedNum = 0;
                        }
                    }
                    //关闭线程池后不再接受新的任务，上面的任务执行结束后就不会有
                    if (isClosed) {
                        //统计丢失信息，countdown
                        logger.log(Level.INFO, String.format(
                            "%d trace segments have been abandoned", segmentDroppedNum + clear()));
                        close.countDown();
                    }
                }
            }
        }, 0,//根据配置文件配置这个间隔时间
            flushInterval, TimeUnit.MILLISECONDS);
    }

    public void report(Segment segment) {
        if (segment == null)
            throw new NullPointerException("segment == null");
        //如果reporter已经关闭或者添加到队列中失败直接把segment丢弃
        if (closed.get() || !addSegment(segment)) {
            segmentDroppedNum++;
        }
    }

    /**
     * 向数组中加入segment，如果添加成功返回true，添加失败返回false
     */
    private boolean addSegment(Segment segment) {
        lock.lock();
        try {
            //队列中已经满了
            if (count == maxSize)
                return false;
            segments[writePos] = segment;
            writePos++;
            //当新的位置超出界限过后，回到头部
            if (writePos == maxSize)
                writePos = 0;
            count++;
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
     * 把数组中的数据全部取出来
     */
    private List<Segment> getAllSegments() {
        lock.lock();
        try {
            List<Segment> result = new ArrayList<>();
            int readCount = 0;
            while (readCount < count) {
                Segment next = segments[readPos];
                if (next == null)
                    break;
                result.add(next);
                readCount++;
                segments[readPos] = null;
                //读指针到最后的位置，回到开头
                if (++readPos == maxSize)
                    readPos = 0;
            }
            count -= readCount;
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true))
            return; // already closed
        try {
            // wait for in-flight spans to send, 默认的等待时间是1S
            if (!close.await(closeTimeoutMill, TimeUnit.MILLISECONDS)) {
                logger.warning("Timed out waiting for in-flight segments to send");
            }
        } catch (InterruptedException e) {
            logger.warning("Interrupted waiting for in-flight segments to send");
            Thread.currentThread().interrupt();
        }
    }
}
