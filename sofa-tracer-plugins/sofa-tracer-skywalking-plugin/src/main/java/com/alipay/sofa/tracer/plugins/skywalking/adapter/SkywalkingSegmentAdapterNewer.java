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

import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.skywalking.utils.ComponentName2ComponentId;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Log;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Segment;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.SegmentReference;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Span;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.network.language.agent.v3.RefType;
import org.apache.skywalking.apm.network.language.agent.v3.SpanType;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkywalkingSegmentAdapterNewer {
    public Segment convertToSkywalkingSegment(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return null;
        }
        Segment segment = new Segment();
        segment.setTraceSegmentId(sofaTracerSpan.getSofaTracerSpanContext().getSpanId());
        segment.setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId());
        segment.setSizeLimited(false);
        segment.setService(constructServiceName(sofaTracerSpan));
        segment.setServiceInstance(constructServiceInstanceName(sofaTracerSpan));
        // 判断是不是Client Span后者如果是构建成 ExitSpan
        segment.addSpan(constructSpan(sofaTracerSpan, sofaTracerSpan.isServer()));
        return segment;
    }

    /**
     * 构造EntrySpan 或 ExitSpan
     */
    private Span constructSpan(SofaTracerSpan sofaTracerSpan, boolean isEntrySpan) {
        Span span = new Span();
        span.setSpanId(0);
        span.setParentSpanId(-1);
        span.setStartTime(sofaTracerSpan.getStartTime());
        span.setEndTime(sofaTracerSpan.getEndTime());
        span.setOperationName(sofaTracerSpan.getOperationName());
        if (isEntrySpan) {
            span.setSpanType(SpanType.Entry);
        } else {
            span.setSpanType(SpanType.Exit);
        }
        //怎么判断是哪一层的？？
        span.setSpanLayer(SpanLayer.DB);
        span.setComponentId(ComponentName2ComponentId.componentName2IDMap.get(sofaTracerSpan
            .getSofaTracer().getTracerType()));
        //??
        span.setError(isError(sofaTracerSpan));
        span.setSkipAnalysis(false);
        //转换tag
        span = convertSpanTags(sofaTracerSpan, span);
        //转换log
        convertSpanLogs(sofaTracerSpan, span);
        // 如果不是root添加ref
        if (!sofaTracerSpan.getSpanReferences().isEmpty()) {
            span = addSegmentReference(sofaTracerSpan, span);
        }

        //如果是Exit Span需要设置一些字段
        if (!isEntrySpan && sofaTracerSpan.getTagsWithStr().containsKey(CommonSpanTags.PEER_HOST)) {
            span.setPeer(sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.PEER_HOST));
        }

        return span;
    }

    /**
     * 构造ExitSpan
     */
    private Span constructExitSpan(SofaTracerSpan sofaTracerSpan) {
        //如果log里面有even = cr cs说明含有exit span
        List<com.alipay.common.tracer.core.span.LogData> sofaLogDatas = sofaTracerSpan.getLogs();
        Long startTime = -1L;
        Long endTime = -1L;
        for (com.alipay.common.tracer.core.span.LogData sofalogData : sofaLogDatas) {
            Object event = sofalogData.getFields().get(LogData.EVENT_TYPE_KEY);
            if (event != null && event.toString().equals(LogData.CLIENT_SEND_EVENT_VALUE)) {
                startTime = sofalogData.getTime();
            }
            if (event != null && event.toString().equals(LogData.CLIENT_RECV_EVENT_VALUE)) {
                endTime = sofalogData.getTime();
            }
        }
        if (startTime == -1L && endTime == -1L) {
            return null;
        }
        Span exitSpan = new Span();
        exitSpan.setPeer(sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.PEER_HOST));
        exitSpan.setError(false);
        exitSpan.setParentSpanId(0);
        exitSpan.setSpanId(1);
        exitSpan.setSpanType(SpanType.Exit);
        exitSpan.setStartTime(startTime);
        exitSpan.setEndTime(endTime);
        //判断是不是同时有cs和cr缺少一个就是error??
        exitSpan.setError(false);
        return exitSpan;
    }

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
        return sofaTracerSpan.getSofaTracerSpanContext().getParentId();
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

    /**
     * 转换tag
     */
    private Span convertSpanTags(SofaTracerSpan sofaTracerSpan, Span swSpan) {
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.putAll(sofaTracerSpan.getTagsWithStr());
        tags.putAll(sofaTracerSpan.getTagsWithBool());
        tags.putAll(sofaTracerSpan.getTagsWithNumber());
        for (Map.Entry<String, Object> tag : tags.entrySet()) {
            //number Boolean 和 string转换成string一样的么
            swSpan.addTag(tag.getKey(), tag.getValue().toString());
        }
        return swSpan;
    }

    /**
     * 转换logs
     */
    private Span convertSpanLogs(SofaTracerSpan sofaTracerSpan, Span swSpan) {
        List<LogData> logs = sofaTracerSpan.getLogs();
        for (LogData sofaLog : logs) {
            //log里面的数据是一个long类型的时间，和一个Map<String,?>类型的
            Log log = new Log();
            log.setTime(sofaLog.getTime());
            //需要把map里面的数据都转换成 KeyStringValuePair
            for (Map.Entry<String, ?> entry : sofaLog.getFields().entrySet()) {
                //有没有更加优雅的方式？ SW中上传的tags只能是String类型的？？
                log.addLogs(entry.getKey(), entry.getValue().toString());
            }
            swSpan.addLog(log);
        }
        return swSpan;
    }

    /**
     * 添加reference
     */
    private Span addSegmentReference(SofaTracerSpan sofaTracerSpan, Span swSpan) {
        //添加ref
        SegmentReference segmentReference = new SegmentReference();
        //全部都设置成跨进程的？？
        segmentReference.setRefType(RefType.UNRECOGNIZED);
        segmentReference.setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId());
        //segmentId保留的SofaTracer中的spanId
        //父segment的Id
        segmentReference.setParentTraceSegmentId(getParentSegmentId(sofaTracerSpan));
        //因为一个segment只有一个span所以直接取固定值0
        segmentReference.setParentSpanId(0);

        //        //        a目前父组件的serviceNme还取不到？？
        //        segmentReference .setParentService("hello");
        //        //        父组件的服务实例名称也还没有相关字段！！
        //        segmentReference .setParentServiceInstance("no2");
        //        //调用父组件的entry span的URL路径，是opration name么
        //        segmentReference .setParentEndpoint(getParentEndpoint(sofaTracerSpan));

        //        按照127.0.0.1:8080的格式组织的，目前也是还没有找到对应字段！！
        String networkAddressUsedAtPeer = getNetworkAddressUsedAtPeer(sofaTracerSpan);
        if (networkAddressUsedAtPeer != null) {
            segmentReference.setNetworkAddressUsedAtPeer(networkAddressUsedAtPeer);
        }
        swSpan.addSegmentReference(segmentReference);
        return swSpan;
    }

    /**
     * 判断是否出现了错误
     */
    private boolean isError(SofaTracerSpan sofaTracerSpan) {
        return !SofaTracerConstant.RESULT_CODE_SUCCESS.equals(sofaTracerSpan.getTagsWithStr().get(
            CommonSpanTags.RESULT_CODE));
    }

    /**
     * 生成NetworkAddressUsedAtPeer
     */
    private String getNetworkAddressUsedAtPeer(SofaTracerSpan sofaTracerSpan) {
        Map<String, String> strTags = sofaTracerSpan.getTagsWithStr();
        String host = strTags.get(CommonSpanTags.LOCAL_HOST);
        String port = strTags.get(CommonSpanTags.LOCAL_PORT);
        if (host != null && port != null) {
            return host + ":" + port;
        }
        return null;
    }

}
