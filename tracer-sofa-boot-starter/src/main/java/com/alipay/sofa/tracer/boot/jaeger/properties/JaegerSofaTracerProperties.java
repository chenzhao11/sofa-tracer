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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.alipay.sofa.tracer.jaeger")
public class JaegerSofaTracerProperties {
    /**
     * jaeger-collector HTTP endpoint
     */
    private String  collectorBaseUrl            = "http://localhost:14268/";

    /**
     * jaeger reporter is disabled by default
     */
    private boolean collectorEnabled            = false;

    /**
     * the max packet size in default it is 2MB
     */
    private int     collectorMaxPacketSizeBytes = 2 * 1024 * 1024;

    /**
     * the address of agent
     */
    private String  agentHost                   = "127.0.0.1";
    /**
     * whether report span to jaeger agent
     */
    private boolean agentEnabled                = false;
    /**
     * jaeger agent port to accept jaeger.thrift
     */
    private int     agentPort                   = 6831;
    /**
     * the max byte of the packet
     * In UDP over IPv4, the limit is 65,507 bytes
     */
    private int     agentMaxPacketSizeBytes     = 65000;

    /**
     *The interval of writing FlushCommand to the command queue
     */
    private int     flushIntervalMill           = 1000;
    /**
     * size of the command queue is too large will waste space, and too small will cause the span to be lost
     */
    private Integer maxQueueSize                = 10000;
    /**
     * Timeout for writing CloseCommand
     */
    private Integer closeEnqueueTimeoutMill     = 1000;

    public void setCollectorBaseUrl(String collectorBaseUrl) {
        this.collectorBaseUrl = collectorBaseUrl;
    }

    public String getCollectorBaseUrl() {
        return this.collectorBaseUrl;
    }

    public void setCollectorEnabled(boolean collectorEnabled) {
        this.collectorEnabled = collectorEnabled;
    }

    public boolean getCollectorEnabled() {
        return this.collectorEnabled;
    }

    public void setCollectorMaxPacketSizeBytes(int collectorMaxPacketSizeBytes) {
        this.collectorMaxPacketSizeBytes = collectorMaxPacketSizeBytes;
    }

    public int getCollectorMaxPacketSizeBytes() {
        return this.collectorMaxPacketSizeBytes;
    }

    public boolean getAgentEnabled() {
        return this.agentEnabled;
    }

    public void setAgentEnabled(boolean agentEnabled) {
        this.agentEnabled = agentEnabled;
    }

    public String getAgentHost() {
        return this.agentHost;
    }

    public void setAgentHost(String agentHost) {
        this.agentHost = agentHost;
    }

    public int getAgentPort() {
        return this.agentPort;
    }

    public void setAgentPort(int agentPort) {
        this.agentPort = agentPort;
    }

    public void setAgentMaxPacketSizeBytes(int agentMaxPacketSizeBytes) {
        this.agentMaxPacketSizeBytes = agentMaxPacketSizeBytes;
    }

    public int getAgentMaxPacketSizeBytes() {
        return this.agentMaxPacketSizeBytes;
    }

    public int getFlushIntervalMill() {
        return this.closeEnqueueTimeoutMill;
    }

    public void setFlushIntervalMill(int flushIntervalMill) {
        this.flushIntervalMill = flushIntervalMill;
    }

    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public void setCloseEnqueueTimeoutMill(int closeEnqueueTimeoutMill) {
        this.closeEnqueueTimeoutMill = closeEnqueueTimeoutMill;
    }

    public int getCloseEnqueueTimeoutMill() {
        return this.closeEnqueueTimeoutMill;
    }

}
