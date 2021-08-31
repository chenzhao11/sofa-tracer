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
package com.alipay.sofa.tracer.plugins.jaeger.initialize;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.jaeger.JaegerSofaTracerSpanRemoteReporter;
import com.alipay.sofa.tracer.plugins.jaeger.properties.JaegerProperties;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

public class JaegerReportRegisterBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws TTransportException {

        //Use the configuration file named sofa.tracer.properties to configure the command queue in jaeger
        //The interval of writing FlushCommand to the command queue
        int flushInterval = SofaTracerConfiguration.getIntegerDefaultIfNull(JaegerProperties.JAEGER_AGENT_FLUSH_INTERVAL_MS_KEY, 1000);
        // size of the command queue is too large will waste space, and too small will cause the span to be lost
        int maxQueueSize = SofaTracerConfiguration.getIntegerDefaultIfNull(JaegerProperties.JAEGER_AGENT_MAX_QUEUE_SIZE_KEY, 100);
        //Timeout for writing CloseCommand
        int closeEnqueueTimeout = SofaTracerConfiguration.getIntegerDefaultIfNull(JaegerProperties.JAEGER_AGENT_CLOSE_ENQUEUE_TIMEOUT_MILLIS_KEY, 1000);


        boolean enabled = false;
        String enabledStr = SofaTracerConfiguration
            .getProperty(JaegerProperties.JAEGER_AGENT_IS_ENABLED_KEY);
        if (StringUtils.isNotBlank(enabledStr) && "true".equalsIgnoreCase(enabledStr)) {
            enabled = true;
        }
        if (!enabled) {
            return;
        }
        String host = SofaTracerConfiguration.getProperty(JaegerProperties.JAEGER_AGENT_HOST_KEY);
        int port = Integer.parseInt(SofaTracerConfiguration
            .getProperty(JaegerProperties.JAEGER_AGENT_PORT_KEY));
        int maxPacketSize = Integer.parseInt(SofaTracerConfiguration
            .getProperty(JaegerProperties.JAEGER_AGENT_MAX_PACKET_SIZE_KEY));
        SpanReportListener spanReportListener = new JaegerSofaTracerSpanRemoteReporter(host, port,
            maxPacketSize,
            SofaTracerConfiguration.getProperty(JaegerProperties.JAEGER_AGENT_SERVICE_NAME_KEY));
        List<SpanReportListener> spanReportListenerList = new ArrayList<SpanReportListener>();
        spanReportListenerList.add(spanReportListener);
        SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
    }
}
