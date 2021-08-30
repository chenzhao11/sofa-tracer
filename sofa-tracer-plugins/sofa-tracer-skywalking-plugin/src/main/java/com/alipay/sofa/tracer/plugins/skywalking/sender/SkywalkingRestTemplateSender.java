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
package com.alipay.sofa.tracer.plugins.skywalking.sender;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.tracer.plugins.skywalking.reporter.AsyncReporter;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class SkywalkingRestTemplateSender {
    private RestTemplate restTemplate;
    private String       url;

    public SkywalkingRestTemplateSender(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "v3/segments";
    }

    //    public void post(SegmentObject segmentObject) {
    //        HttpHeaders httpHeaders = new HttpHeaders();
    //        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    //        String json = SegmentObject2JSON.toJSONString(segmentObject);
    //
    //        RequestEntity<String> requestEntity = new RequestEntity<String>(json, httpHeaders,
    //            HttpMethod.POST, URI.create(this.url));
    //        this.restTemplate.exchange(requestEntity, String.class);
    //    }

    //为了测试方便加上的
    public void post(String json) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<String> requestEntity = new RequestEntity<String>(json, httpHeaders,
            HttpMethod.POST, URI.create(this.url));
        this.restTemplate.exchange(requestEntity, String.class);
    }

    //测试自定义的POJO
    public boolean post(List<Segment> segments) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        String json = JSON.toJSONString(segments);
        System.out.println("需要发送的segment是" + json);

        RequestEntity<String> requestEntity = new RequestEntity<String>(json, httpHeaders,
            HttpMethod.POST, URI.create(this.url));
        //发送请求出错也不用处理？

        Logger logger = Logger.getLogger(AsyncReporter.class.getName());
        logger.warning("开始发送信息");
        ResponseEntity<String> response = this.restTemplate.exchange(requestEntity, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            return false;
        }

        logger.warning(response.toString());
        logger.warning(response.getStatusCodeValue() + "");
        return true;

    }

    //测试自定义的POJO
    public void post(Segment segments) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String json = JSON.toJSONString(segments);
        RequestEntity<String> requestEntity = new RequestEntity<String>(json, httpHeaders,
            HttpMethod.POST, URI.create(this.url));
        //发送请求出错也不用处理？
        this.restTemplate.exchange(requestEntity, String.class);
    }
}
