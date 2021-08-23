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

public enum SpanLayer {
    //unknown在proto文件有但是jar包里面没??
    //    UNKNOWN(0),  必须要小写？   Http MQ Database Unknown
//    DB(1), RPC_FRAMEWORK(2), HTTP(3), MQ(4), CACHE(5);

    DB(1), RPC_FRAMEWORK(2), HTTP(3), MQ(4), CACHE(5);

    private int code;

    private SpanLayer(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
