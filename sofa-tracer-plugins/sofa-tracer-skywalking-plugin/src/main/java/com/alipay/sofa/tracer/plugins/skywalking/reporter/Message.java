package com.alipay.sofa.tracer.plugins.skywalking.reporter;

import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;

import java.util.ArrayList;

public class Message {
    final int          maxBytes;
    // 设置的超时发送message时间
    final    long               timeoutNanos;
    final    ArrayList<Segment> message = new ArrayList<>();
    boolean full = false;
    // 如果有多余一个segment每添加一个segment中间会多一个逗号
    boolean hasAtLeastOneSpan = false;
    // 消息目前的长度
    int messageSizeInBytes;
    // 超过这个时间就会发送message中数据，是当前系统的时间加上超时时间
    long deadlineNanoTime;


    public Message(int maxBytes, Long timeoutNanos){
        this.maxBytes = maxBytes;
        this.timeoutNanos = timeoutNanos;
        // 一开始就有两个中括号
        this.messageSizeInBytes = 2;
    }

    /**
     * 把传入的segment加入到缓存队列中
     * @param next
     * @return
     */
    public boolean offer(Segment next, int nextSizeInBytes){
        int x = countMessageSizeInBytes(nextSizeInBytes);
        int y = maxBytes;
        // 包含下一个的字节数和最大字节数的比较
        int includingNextVsMaxBytes = (x < y) ? -1 : ((x == y) ? 0 : 1); // Integer.compare, but JRE 6
        // 超过最大字节数
        if (includingNextVsMaxBytes > 0) {
            full = true;
            return false; // can't fit the next message into this buffer
        }

        addSegmentToMessage(next);
        messageSizeInBytes = x;
        // 刚好填满
        if (includingNextVsMaxBytes == 0) full = true;
        return true;
    }


    private int countMessageSizeInBytes(int nextSizeInBytes){
        // 如果是多于1个这是需要添加逗号
        return messageSizeInBytes + nextSizeInBytes + (hasAtLeastOneSpan ? 1 : 0);
    }

    /**
     * 重置
     */
    public void reset() {
        messageSizeInBytes = 2;
        hasAtLeastOneSpan = false;
        //清空过后会重新计算
        deadlineNanoTime = 0;
        full = false;
        message.clear();
    }

    /**
     * 获取当前的message
     * @return
     */
    public ArrayList<Segment> getMessage(){
        return this.message;
    }

    /**
     * 向message中添加segment
     * @param next
     */
    private void addSegmentToMessage(Segment next){
        this.message.add(next);
        if(!hasAtLeastOneSpan){
            hasAtLeastOneSpan = true;
        }
    }

    /**
     * 还剩下的超时时间，不大于0表示超时
     * @return
     */
    long remainingNanos() {
        if (message.isEmpty()) {
            deadlineNanoTime = System.nanoTime() + timeoutNanos;
        }
        return Math.max(deadlineNanoTime - System.nanoTime(), 0);
    }
    /**
     * 判断是不是准备好了可以发送了
     * @return
     */
    boolean isReady() {
        return full || remainingNanos() <= 0;
    }

}
