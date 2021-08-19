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
package com.alipay.sofa.tracer.plugins.skywalking.adapter;

import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.skywalking.utils.ComponentName2ComponentId;
import org.apache.skywalking.apm.agent.core.context.TracingContext;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.apache.skywalking.apm.agent.core.context.trace.EntrySpan;
import org.apache.skywalking.apm.agent.core.context.trace.ExitSpan;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.apache.skywalking.apm.network.common.v3.KeyStringValuePair;
import org.apache.skywalking.apm.network.language.agent.v3.*;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkywalkingSegmentAdapter {
    public SegmentObject convertToSkywalkingSegment(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return null;
        }
        SpanObject entrySpan = constructEntrySpan(sofaTracerSpan);

        //最后上传使用的是proto中的定义 segmentObject  spanObject(在tracesegment中转换成spanObject的时候也是用
        // transform转换的)
        SegmentObject segmentObject = SegmentObject.newBuilder()
            .setTraceSegmentId(sofaTracerSpan.getSofaTracerSpanContext().getSpanId())
            .setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId())
            .setIsSizeLimited(false).setService(constructServiceName(sofaTracerSpan))
            .setServiceInstance(constructServiceInstanceName(sofaTracerSpan)).addSpans(entrySpan)
            .build();
        return segmentObject;
    }

    /**
     * 把sofaTracerSpan中的tags转换SW中
     */
    private void convertSpanTags(SofaTracerSpan sofaTracerSpan, SpanObject.Builder builder) {
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.putAll(sofaTracerSpan.getTagsWithStr());
        tags.putAll(sofaTracerSpan.getTagsWithBool());
        tags.putAll(sofaTracerSpan.getTagsWithNumber());
        for (Map.Entry<String, Object> tag : tags.entrySet()) {
            KeyStringValuePair keyStringValuePair = KeyStringValuePair.newBuilder()
                .setKey(tag.getKey())
                //有没有更加优雅的方式？ SW中上传的tags只能是String类型的？？
                .setValue(tag.getValue().toString()).build();
            builder.addTags(keyStringValuePair);
        }
    }

    /**
     * 转换logs
     */
    private void convertSpanLogs(SofaTracerSpan sofaTracerSpan, SpanObject.Builder builder) {
        List<LogData> logs = sofaTracerSpan.getLogs();
        for (LogData sofaLog : logs) {
            //log里面的数据是一个long类型的时间，和一个Map<String,?>类型的
            Log.Builder logBuilder = Log.newBuilder().setTime(sofaLog.getTime());
            //需要把map里面的数据都转换成 KeyStringValuePair
            for (Map.Entry<String, ?> entry : sofaLog.getFields().entrySet()) {
                KeyStringValuePair keyStringValuePair = KeyStringValuePair.newBuilder()
                    .setKey(entry.getKey())
                    //有没有更加优雅的方式？ SW中上传的tags只能是String类型的？？
                    .setValue(entry.getValue().toString()).build();
                logBuilder.addData(keyStringValuePair);
            }
            Log swLog = logBuilder.build();
            builder.addLogs(swLog);
        }
    }

    /**
     * 构建EntrySpan
     */
    private SpanObject constructEntrySpan(SofaTracerSpan sofaTracerSpan) {
        //构架entryspan  传入的TracingContext直接传入null要是创建实例的话内部有很多监听器
        SpanObject.Builder entrySpanBuilder = SpanObject
            .newBuilder()
            .setSpanId(0)
            .setParentSpanId(-1)
            .setStartTime(sofaTracerSpan.getStartTime())
            .setEndTime(sofaTracerSpan.getEndTime())
            .setOperationName(sofaTracerSpan.getOperationName())
            .setSpanType(SpanType.Entry)
            //从sofa哪里去拿数据？
            //.setSpanLayer(SpanLayer)

            //单元测试中也需要创建Tracer对象
            .setComponentId(
                ComponentName2ComponentId.componentName2IDMap.get(sofaTracerSpan.getSofaTracer()
                    .getTracerType()))

            //错误信息哪里获取？？？
            .setIsError(false).setSkipAnalysis(false)
            //这里测试一下不加入Exit Span因为创建exitspan的话可能出现重复的span展示在SW UI中
            //目前还没有获取remote address的办法？？？？？？？？？？
            .setPeer("165.32.65.251");
        //设置parent segments
        SegmentReference segmentReference = SegmentReference
            .newBuilder()
            //全部都设置成跨进程的？？
            .setRefType(RefType.CrossProcess)
            .setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId())
            //segmentId保留的SofaTracer中的spanId
            //父segment的Id
            .setParentTraceSegmentId(getParentSegmentId(sofaTracerSpan))
            //因为一个segment只有一个span所以直接取固定值0
            .setParentSpanId(0).setParentService("a目前父组件的serviceNme还取不到？？")
            .setParentServiceInstance("父组件的服务实例名称也还没有相关字段！！")
            //调用父组件的entry span的URL路径，是opration name么
            .setParentEndpoint(getParentEndpoint(sofaTracerSpan))
            .setNetworkAddressUsedAtPeer("按照127.0.0.1:8080的格式组织的，目前也是还没有找到对应字段！！").build();
        entrySpanBuilder.addRefs(segmentReference);
        //设置logs
        convertSpanLogs(sofaTracerSpan, entrySpanBuilder);
        //转换tags
        convertSpanTags(sofaTracerSpan, entrySpanBuilder);
        return entrySpanBuilder.build();
    }

    /**
     * 构造exitSpan
     */
    //    private SpanObject constructExitSpan(SofaTracerSpan sofaTracerSpan){
    //        ExitSpan
    //    }
    /**
     * 构造serviceName
     */
    private String constructServiceName(SofaTracerSpan sofaTracerSpan) {
        return sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.LOCAL_APP);
    }

    /**
     * 构造ServiceInstanceName
     */
    private String constructServiceInstanceName(SofaTracerSpan sofaTracerSpan) {
        //目前只是通过机器的ip来构建实例，还可以有哪些其他的方式？？
        InetAddress localIpAddress = NetUtils.getLocalAddress();
        return localIpAddress.getHostAddress();
    }

    /**
     * 获取parentSegmentId
     */
    private String getParentSegmentId(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan.getParentSofaTracerSpan() != null) {
            return sofaTracerSpan.getParentSofaTracerSpan().getSofaTracerSpanContext().getSpanId();
        }
        return StringUtils.EMPTY_STRING;
    }

    /**
     *获取ParentEndpoint，因为可能为空指针所以需要单独拎出来处理
     */
    private String getParentEndpoint(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan.getParentSofaTracerSpan() != null) {
            return sofaTracerSpan.getParentSofaTracerSpan().getOperationName();
        }
        return StringUtils.EMPTY_STRING;
    }
}
