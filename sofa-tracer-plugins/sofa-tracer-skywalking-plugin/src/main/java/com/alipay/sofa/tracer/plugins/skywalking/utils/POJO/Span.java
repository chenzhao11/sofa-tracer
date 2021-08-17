package com.alipay.sofa.tracer.plugins.skywalking.utils.POJO;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.network.language.agent.v3.SpanType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Span {
    private int                    spanId;
    private int                    parentSpanId;
    private Long                   startTime;
    private Long                   endTime;
    private List<SegmentReference> refs = new LinkedList<>();
    private String                 operationName;
    //peer在exit span中使用对构建拓扑图有至关重要的作用
    private String                 peer;
    //如果引入的SW依赖包中东西很少可以考虑直接把文件拷贝过来
    private SpanType spanType;
    // Span layer represent the component tech stack, related to the network tech.
    private SpanLayer spanLayer;
    private int componentId;
    private boolean                isError;
    private List<KeyStringValuePair>                   tags = new LinkedList<>();
    private List<Log>              logs = new LinkedList<>();
    private boolean  skipAnalysis;
    public void addSegmentReference(SegmentReference segmentReference){
        refs.add(segmentReference);
    }
    public void addTag(String key, String value){
        tags.add(new KeyStringValuePair(key, value));
    }

    //需要把lombok自动生成的set函数取消掉
    public void addLog(Log log){
        logs.add(log);
    }

}
