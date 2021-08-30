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

import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.skywalking.model.*;
import com.alipay.sofa.tracer.plugins.skywalking.utils.ComponentName2ComponentId;
import com.alipay.sofa.tracer.plugins.skywalking.utils.ComponentName2SpanLayer;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkywalkingSegmentAdapter {
    /**
     * convert sofaTracerSpan to segment in Skywalking
     * @param sofaTracerSpan
     * @return the segment in Skywalking
     */
    public Segment convertToSkywalkingSegment(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return null;
        }
        Segment segment = new Segment();
        segment.setTraceSegmentId(generateSegmentId(sofaTracerSpan));
        segment.setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId());
        segment.setSizeLimited(false);
        segment.setService(constructServiceName(sofaTracerSpan));
        segment.setServiceInstance(constructServiceInstanceName(sofaTracerSpan));
        segment.addSpan(constructSpan(sofaTracerSpan));
        return segment;
    }

    /**
     * generate segmentId  traceId + FNV64HashCode(SpanId) + 0/1
     * the client and server span generate by dubbo or sofaRpc share the same traceId and spanId,
     * so we need to append 0(server),1(client) to the end of segmentId.
     * @param sofaTracerSpan
     * @return segmentId
     */
    private String generateSegmentId(SofaTracerSpan sofaTracerSpan) {
        return sofaTracerSpan.getSofaTracerSpanContext().getTraceId()
               + FNV64HashCode(sofaTracerSpan.getSofaTracerSpanContext().getSpanId())
               + (sofaTracerSpan.isServer() ? SofaTracerConstant.SERVER : SofaTracerConstant.CLIENT);
    }

    /**
     * construct EntrySpan or ExitSpan determined by the type of sofaTracerSpan
     * @param sofaTracerSpan
     * @return EntrySpan or ExitSpan
     */
    private Span constructSpan(SofaTracerSpan sofaTracerSpan) {
        Span span = new Span();
        // only have one span every segment
        span.setSpanId(0);
        span.setParentSpanId(-1);
        span.setStartTime(sofaTracerSpan.getStartTime());
        span.setEndTime(sofaTracerSpan.getEndTime());
        span.setOperationName(sofaTracerSpan.getOperationName());
        if (sofaTracerSpan.isServer()) {
            span.setSpanType(SpanType.Entry);
        } else {
            span.setSpanType(SpanType.Exit);
        }
        //map tracerType in sofaTracer to SpanLayer in skyWalking
        span.setSpanLayer(ComponentName2SpanLayer.map.get(sofaTracerSpan.getSofaTracer()
            .getTracerType()));

        //map tracerType in sofaTracer to ComponentId in skyWalking
        span.setComponentId(getComponentId(sofaTracerSpan));
        span.setError(isError(sofaTracerSpan));
        span.setSkipAnalysis(true);
        span = convertSpanTags(sofaTracerSpan, span);
        convertSpanLogs(sofaTracerSpan, span);
        // if has patentId then need to add segmentReference
        if (!StringUtils.isBlank(sofaTracerSpan.getSofaTracerSpanContext().getParentId())) {
            span = addSegmentReference(sofaTracerSpan, span);
        }

        String remoteHost = sofaTracerSpan.getTagsWithStr().get("remote.host");
        String remotePort = sofaTracerSpan.getTagsWithStr().get("remote.port");
        String remoteIp = sofaTracerSpan.getTagsWithStr().get("remote.ip");

        // exit和entry都需要设置？不然拓扑图会多出一个节点？
        if (sofaTracerSpan.isClient() && remoteHost != null && remotePort != null) {
            span.setPeer(remoteHost + ":" + remotePort);
        }
        if (sofaTracerSpan.isServer() && remoteHost != null && remotePort != null) {
            span.setPeer(remoteHost + ":" + remotePort);
        }
        // if the span is formed by sofaRPC
        if (sofaTracerSpan.getSofaTracer().getTracerType().equals("RPC_TRACER") && remoteIp != null) {
            span.setPeer(remoteIp.split(":")[0]);
        }
        return span;
    }

    /**
     * construct serviceName
     * @param sofaTracerSpan
     * @return serviceName
     */
    private String constructServiceName(SofaTracerSpan sofaTracerSpan) {
        return sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.LOCAL_APP);
    }

    /**
     * 构造ServiceInstanceName,可以考虑不传入sofaTracerSpan提高一点效率
     */
    private String constructServiceInstanceName(SofaTracerSpan sofaTracerSpan) {
        //目前只是通过机器的ip来构建实例，还可以有哪些其他的方式？？
        InetAddress localIpAddress = NetUtils.getLocalAddress();
        return constructServiceName(sofaTracerSpan) + "@" + localIpAddress.getHostAddress();
    }

    /**
     * 获取parentSegmentId
     */
    private String getParentSegmentId(SofaTracerSpan sofaTracerSpan) {
        //长度应该限制一下？但是长度限制的话可能有的segmentId一样会覆盖
        //自己是serverspan父span只能是client ，自己是client父只能server(X)
        //在sofaRPC中也有可能是 server->server
        // if the span is the server span of RPC, then it's parentSegmentId is traceId + spanId +client
        if (sofaTracerSpan.isServer()
            && ComponentName2SpanLayer.map.get(sofaTracerSpan.getSofaTracer().getTracerType())
                .equals(SpanLayer.RPCFramework)) {
            return sofaTracerSpan.getSofaTracerSpanContext().getTraceId()
                   + FNV64HashCode(sofaTracerSpan.getSofaTracerSpanContext().getSpanId())
                   + SofaTracerConstant.CLIENT;
        }
        String prefix = sofaTracerSpan.getSofaTracerSpanContext().getTraceId()
                        + FNV64HashCode(sofaTracerSpan.getSofaTracerSpanContext().getParentId());
        return prefix
               + (sofaTracerSpan.isServer() ? SofaTracerConstant.CLIENT : SofaTracerConstant.SERVER);

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
        //全部都设置成跨进程的？？,使用unrecognize不会绘制拓扑图
        segmentReference.setRefType(RefType.CrossProcess);
        segmentReference.setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId());
        //segmentId保留的SofaTracer中的spanId
        //父segment的Id
        segmentReference.setParentTraceSegmentId(getParentSegmentId(sofaTracerSpan));
        //因为一个segment只有一个span所以直接取固定值0
        segmentReference.setParentSpanId(0);

        //        segmentReference.setParentService("dubbo-consumer");
        //        segmentReference.setParentServiceInstance("dubbo-consumer@172.28.16.1");
        //        segmentReference.setParentEndpoint("HelloService#SayHello");

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
    // The network address, including ip/hostname and port, which is used in the client side.
    // Such as Client --> use 127.0.11.8:913 -> Server
    // then, in the reference of entry span reported by Server, the value of this field is 127.0.11.8:913.
    // This plays the important role in the SkyWalking STAM(Streaming Topology Analysis Method)
    // For more details, read https://wu-sheng.github.io/STAM/
    // 不能拿到？
    private String getNetworkAddressUsedAtPeer(SofaTracerSpan sofaTracerSpan) {

        // if is sofaRpc get localIp
        if (sofaTracerSpan.getSofaTracer().getTracerType().equals("RPC_TRACER")) {
            return NetUtils.getLocalIpv4();
            //            return "127.0.0.1";
        }
        Map<String, String> strTags = sofaTracerSpan.getTagsWithStr();
        String host = strTags.get(CommonSpanTags.LOCAL_HOST);
        String port = strTags.get(CommonSpanTags.LOCAL_PORT);
        if (host != null && port != null) {
            return host + ":" + port;
        }
        return null;
    }

    /**
     * 获取tracerType对应的componentId
     */
    private int getComponentId(SofaTracerSpan sofaTracerSpan) {
        String tracerType = sofaTracerSpan.getSofaTracer().getTracerType();
        final int UNKNOWN = ComponentName2ComponentId.componentName2IDMap.get("UNKNOWN");
        if (StringUtils.isBlank(tracerType)) {
            return UNKNOWN;
        }
        if (tracerType.equals(ComponentNameConstants.DATA_SOURCE)) {
            String database = sofaTracerSpan.getTagsWithStr().get("database.type");
            if (StringUtils.isBlank(database)) {
                return UNKNOWN;
            } else {
                //如果不是null，才返回
                if (ComponentName2ComponentId.componentName2IDMap.containsKey(database)) {
                    return ComponentName2ComponentId.componentName2IDMap.get(database);
                }
                return UNKNOWN;
            }
        }

        // 如果在map里面找不到也不应该返回null，应该返回的是一个int  ===bug==
        if (ComponentName2ComponentId.componentName2IDMap.containsKey(tracerType)) {
            return ComponentName2ComponentId.componentName2IDMap.get(tracerType);
        } else {
            return UNKNOWN;
        }
    }

    /**
     * from http://en.wikipedia.org/wiki/Fowler_Noll_Vo_hash
     *
     * @param data String data
     * @return fnv hash code
     */
    public static long FNV64HashCode(String data) {
        //hash FNVHash64 : http://www.isthe.com/chongo/tech/comp/fnv/index.html#FNV-param
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < data.length(); ++i) {
            char c = data.charAt(i);
            hash ^= c;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

}
