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
import com.alibaba.fastjson.JSON;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Log;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Segment;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.SegmentReference;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Span;

import java.util.List;

public class Test {
    @org.junit.Test
    public void testJson() {
        Segment segment = new Segment();

        Log log = new Log();
        log.setTime(343435443L);
        log.addLogs("log", "ced");
        log.addLogs("hello", "ldfaldhfj");

        SegmentReference segmentReference = new SegmentReference();
        segmentReference.setParentEndpoint("dfadsfasfd");

        Span span = new Span();
        span.setOperationName("ceshi");
        span.setSpanId(145446);
        span.addLog(log);
        span.addSegmentReference(segmentReference);
        span.addTag("tag", "value");
        span.addTag("heljafldfla", "dasdfhash");
        segment.setService("測試");
        segment.addSpan(span);

        System.out.println(JSON.toJSONString(segment));
    }
}
