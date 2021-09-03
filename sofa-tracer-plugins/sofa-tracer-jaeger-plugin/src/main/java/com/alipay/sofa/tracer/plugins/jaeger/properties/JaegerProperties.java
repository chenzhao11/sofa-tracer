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
package com.alipay.sofa.tracer.plugins.jaeger.properties;

public class JaegerProperties {
    public static final String JAEGER_COLLECTOR_IS_ENABLED_KEY               = "com.alipay.sofa.tracer.jaeger.collectorEnabled";
    public static final String JAEGER_COLLECTOR_BASE_URL_KEY                 = "com.alipay.sofa.tracer.jaeger.collectorBaseUrl";
    public static final String JAEGER_COLLECTOR_MAX_PACKET_SIZE_KEY          = "com.alipay.sofa.tracer.jaeger.collectorMaxPacketSizeBytes";
    public static final String JAEGER_AGENT_IS_ENABLED_KEY                   = "com.alipay.sofa.tracer.jaeger.agentEnabled";
    public static final String JAEGER_AGENT_HOST_KEY                         = "com.alipay.sofa.tracer.jaeger.agentHost";
    public static final String JAEGER_AGENT_PORT_KEY                         = "com.alipay.sofa.tracer.jaeger.agentPort";
    public static final String JAEGER_AGENT_MAX_PACKET_SIZE_KEY              = "com.alipay.sofa.tracer.jaeger.agentMaxPacketSizeBytes";
    public static final String JAEGER_AGENT_FLUSH_INTERVAL_MS_KEY            = "com.alipay.sofa.tracer.jaeger.flushInterval";
    public static final String JAEGER_AGENT_MAX_QUEUE_SIZE_KEY               = "com.alipay.sofa.tracer.jaeger.maxQueueSize";
    public static final String JAEGER_AGENT_CLOSE_ENQUEUE_TIMEOUT_MILLIS_KEY = "com.alipay.sofa.tracer.jaeger.closeEnqueueTimeout";
    public static final String JAEGER_SERVICE_NAME_KEY                       = "spring.application.name";
}
