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
package com.alipay.sofa.tracer.plugins.skywalking.initialize;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.skywalking.SkywalkingSpanRemoteReporter;
import com.alipay.sofa.tracer.plugins.skywalking.properties.SkywalkingProperties;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

/**
 *  在非Spring Boot工程中可以用来解析配置属性以及注册上报监听
 */
public class SkywalkingReportRegisterBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        // if do not match report condition,it will be return right now
        boolean enabled = false;
        String enabledStr = SofaTracerConfiguration
            .getProperty(SkywalkingProperties.SKYWALKING_IS_ENABLED_KEY);
        if (StringUtils.isNotBlank(enabledStr) && "true".equalsIgnoreCase(enabledStr)) {
            enabled = true;
        }
        if (!enabled) {
            return;
        }
        //默认baseUrl是 http://localhost:12800
        String baseUrl = SofaTracerConfiguration.getProperty(
            SkywalkingProperties.SKYWALKING_BASE_URL_KEY, "http://localhost:12800");
        int maxBufferSize = Integer.valueOf(SofaTracerConfiguration.getProperty(
            SkywalkingProperties.SKYWALKING_MAX_BUFFER_SIZE_KEY, "10000"));

        SpanReportListener spanReportListener = new SkywalkingSpanRemoteReporter(baseUrl, maxBufferSize);
        List<SpanReportListener> spanReportListenerList = new ArrayList<SpanReportListener>();
        spanReportListenerList.add(spanReportListener);
        SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
    }
}
