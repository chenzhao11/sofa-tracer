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
package com.alipay.sofa.tracer.plugins.skywalking.utils.POJO;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.apm.network.language.agent.v3.SpanType;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Span {
    private int                      spanId;
    private int                      parentSpanId;
    private Long                     startTime;
    private Long                     endTime;
    private List<SegmentReference>   refs = new LinkedList<>();
    private String                   operationName;
    //peer在exit span中使用对构建拓扑图有至关重要的作用
    private String                   peer;
    //如果引入的SW依赖包中东西很少可以考虑直接把文件拷贝过来
    private SpanType                 spanType;
    // Span layer represent the component tech stack, related to the network tech.
    private SpanLayer                spanLayer;
    private int                      componentId;
    private boolean                  isError;
    private List<KeyStringValuePair> tags = new LinkedList<>();
    private List<Log>                logs = new LinkedList<>();
    private boolean                  skipAnalysis;

    public void addSegmentReference(SegmentReference segmentReference) {
        refs.add(segmentReference);
    }

    public void addTag(String key, String value) {
        tags.add(new KeyStringValuePair(key, value));
    }

    //需要把lombok自动生成的set函数取消掉
    public void addLog(Log log) {
        logs.add(log);
    }

}
