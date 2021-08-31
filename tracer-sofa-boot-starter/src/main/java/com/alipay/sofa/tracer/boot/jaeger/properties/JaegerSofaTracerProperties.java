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
package com.alipay.sofa.tracer.boot.jaeger.properties;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.sofa.tracer.plugins.jaeger.properties.JaegerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.alipay.sofa.tracer.jaeger")
public class JaegerSofaTracerProperties {
    /**
     * jaeger-collector HTTP endpoint
     */
    private String  baseUrl = "http://localhost:14268/";
    /**
     * jaeger reporter is disabled by default
     */
    private boolean enabled = false;

    /**
     * the max packet size in default it is 2MB
     */
    private int maxPacketSizeBytes = 2 * 1024 * 1024;
    /**
     *The interval of writing FlushCommand to the command queue
     */

    private int flushIntervalMill = 1000;
    /**
     * size of the command queue is too large will waste space, and too small will cause the span to be lost
     */
    private Integer maxQueueSize = 10000;
    /**
     * Timeout for writing CloseCommand
     */
    private Integer closeEnqueueTimeoutMill =  1000;


    public int getFlushIntervalMill(){
        return this.closeEnqueueTimeoutMill;
    }
    public void setFlushIntervalMill(int flushIntervalMill){
        this.flushIntervalMill =flushIntervalMill;
    }
    public int getMaxQueueSize(){
        return this.maxQueueSize;
    }
    public void setMaxQueueSize(int maxQueueSize){
        this.maxQueueSize = maxQueueSize;
    }
    public void setCloseEnqueueTimeoutMill(int closeEnqueueTimeoutMill){
        this.closeEnqueueTimeoutMill = closeEnqueueTimeoutMill;
    }
    public int getCloseEnqueueTimeoutMill(){
        return this.closeEnqueueTimeoutMill;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getmaxPacketSizeBytes(){
        return this.maxPacketSizeBytes;
    }

    public void setMaxPacketSizeBytes(int maxPacketSizeBytes){
        this.maxPacketSizeBytes = maxPacketSizeBytes;
    }



}
