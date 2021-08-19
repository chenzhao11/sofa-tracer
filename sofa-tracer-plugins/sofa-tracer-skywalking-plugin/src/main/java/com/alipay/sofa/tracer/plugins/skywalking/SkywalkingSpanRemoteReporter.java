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
package com.alipay.sofa.tracer.plugins.skywalking;

import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.skywalking.adapter.SkywalkingSegmentAdapter;
import com.alipay.sofa.tracer.plugins.skywalking.adapter.SkywalkingSegmentAdapterNewer;
import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Segment;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.apache.skywalking.apm.agent.core.context.trace.EntrySpan;
import org.apache.skywalking.apm.agent.core.context.trace.ExitSpan;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.springframework.web.client.RestTemplate;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public class SkywalkingSpanRemoteReporter implements SpanReportListener, Flushable, Closeable {
    // 三个需要转换的对象  TraceSegment ExitSpan EntrySpan

    @Override
    public void onSpanReport(SofaTracerSpan sofaTracerSpan) {
        //        SegmentObject segmentObject =  new SkywalkingSegmentAdapter().convertToSkywalkingSegment(sofaTracerSpan);
        Segment segment = new SkywalkingSegmentAdapterNewer()
            .convertToSkywalkingSegment(sofaTracerSpan);
        SkywalkingRestTemplateSender sender = new SkywalkingRestTemplateSender(new RestTemplate(),
            "http://127.0.0.1:12800");
        sender.post(segment);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }
}
