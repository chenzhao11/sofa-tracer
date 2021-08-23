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
    @Override public void afterPropertiesSet() throws Exception {
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
        String baseUrl = SofaTracerConfiguration.getProperty(SkywalkingProperties.SKYWALKING_BASE_URL_KEY, "http://localhost:12800");
        int maxBufferSize = Integer.valueOf(SofaTracerConfiguration.getProperty(SkywalkingProperties.SKYWALKING_MAX_BUFFER_SIZE_KEY, "10000"));
        //设置默认的上传间隔，时间单位是mm
        int flushIntervalMill = Integer.valueOf(SofaTracerConfiguration.getProperty(SkywalkingProperties.SKYWALKING_FLUSH_INTERVAL_MILL_KEY, "200"));

        SpanReportListener spanReportListener = new SkywalkingSpanRemoteReporter(baseUrl, maxBufferSize, flushIntervalMill);
        List<SpanReportListener> spanReportListenerList = new ArrayList<SpanReportListener>();
        spanReportListenerList.add(spanReportListener);
        SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
    }
}
