package com.alipay.sofa.tracer.plugins.skywalking.utils.POJO;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Segment {
    private String     traceId;
    private String     traceSegmentId;
    private List<Span> spans = new LinkedList<>();
    //执行同样操作的一组服务名，每一个服务名会在拓扑图中单独显示为一个节点，同一个服务名下的从span中得到的指标汇集到到一起作为这个服务的指标
    private String     service;
    //实例名
    private String     serviceInstance;
    // Whether the segment includes all tracked spans.
    // In the production environment tracked, some tasks could include too many spans for one request context, such as a batch update for a cache, or an async job.
    // The agent/SDK could optimize or ignore some tracked spans for better performance.
    // In this case, the value should be flagged as TRUE.
    private boolean     isSizeLimited;
    public void addSpan(Span span){
        spans.add(span);
    }



}
