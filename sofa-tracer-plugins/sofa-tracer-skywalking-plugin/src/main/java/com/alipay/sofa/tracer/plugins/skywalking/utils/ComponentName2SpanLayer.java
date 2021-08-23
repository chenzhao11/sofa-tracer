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
package com.alipay.sofa.tracer.plugins.skywalking.utils;

import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.SpanLayer;

import java.util.HashMap;

public class ComponentName2SpanLayer {
    public static final HashMap<String, SpanLayer> map = new HashMap<>();
    static {
        map.put(ComponentNameConstants.DATA_SOURCE, SpanLayer.DB);
        map.put(ComponentNameConstants.DUBBO_CLIENT, SpanLayer.RPC_FRAMEWORK);
        map.put(ComponentNameConstants.DUBBO_SERVER, SpanLayer.RPC_FRAMEWORK);
        map.put(ComponentNameConstants.HTTP_CLIENT, SpanLayer.HTTP);
        map.put(ComponentNameConstants.OK_HTTP, SpanLayer.HTTP);
        map.put(ComponentNameConstants.REST_TEMPLATE, SpanLayer.HTTP);
        map.put(ComponentNameConstants.SPRING_MVC, SpanLayer.HTTP);

        map.put(ComponentNameConstants.FLEXIBLE, SpanLayer.HTTP);
        map.put(ComponentNameConstants.MSG_PUB, SpanLayer.RPC_FRAMEWORK);
        map.put(ComponentNameConstants.MSG_SUB, SpanLayer.RPC_FRAMEWORK);

        map.put(ComponentNameConstants.FEIGN_CLIENT, SpanLayer.HTTP);

        map.put(ComponentNameConstants.KAFKAMQ_CONSUMER, SpanLayer.MQ);
        map.put(ComponentNameConstants.KAFKAMQ_SEND, SpanLayer.MQ);

        map.put(ComponentNameConstants.ROCKETMQ_CONSUMER, SpanLayer.MQ);
        map.put(ComponentNameConstants.ROCKETMQ_SEND, SpanLayer.MQ);
        map.put(ComponentNameConstants.RABBITMQ_CONSUMER, SpanLayer.MQ);
        map.put(ComponentNameConstants.RABBITMQ_SEND, SpanLayer.MQ);

        map.put(ComponentNameConstants.MONGO_CLIENT, SpanLayer.CACHE);
        map.put(ComponentNameConstants.REDIS, SpanLayer.CACHE);
    }
}
