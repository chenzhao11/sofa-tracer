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
package sender;

import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

public class SkywalkingRestTemplateSenderTest {
    String                       data;
    SkywalkingRestTemplateSender sender;

    @Before
    public void init() {
        sender = new SkywalkingRestTemplateSender(new RestTemplate(), "http://127.0.0.1:12800");
        data = "{\n" + "\t\"traceId\": \"a12ffb-5807-463b-a1f8-fb1c860821fdrg9e\",\n"
               + "\t\"serviceInstance\": \"SWTEST\",\n" + "\t\"spans\": [{\n"
               + "\t\t\"operationName\": \"/develop\",\n" + "\t\t\"startTime\": 1629096916596,\n"
               + "\t\t\"endTime\": 1629096916600,\n" + "\t\t\"spanType\": \"Exit\",\n"
               + "\t\t\"spanId\": 1,\n" + "\t\t\"isError\": false,\n"
               + "\t\t\"parentSpanId\": 0,\n" + "\t\t\"componentId\": 6000,\n"
               + "\t\t\"peer\": \"upstream service\",\n" + "\t\t\"spanLayer\": \"Http\"\n"
               + "\t}, {\n" + "\t\t\"operationName\": \"/ingress\",\n"
               + "\t\t\"startTime\": 1629096916596,\n" + "\t\t\"tags\": [{\n"
               + "\t\t\t\"key\": \"http.method\",\n" + "\t\t\t\"value\": \"GET\"\n" + "\t\t}, {\n"
               + "\t\t\t\"key\": \"http.params\",\n"
               + "\t\t\t\"value\": \"http://localhost/ingress\"\n" + "\t\t}],\n"
               + "\t\t\"endTime\": 1629096916677,\n" + "\t\t\"spanType\": \"Entry\",\n"
               + "\t\t\"spanId\": 0,\n" + "\t\t\"parentSpanId\": -1,\n"
               + "\t\t\"isError\": false,\n" + "\t\t\"spanLayer\": \"Http\",\n"
               + "\t\t\"componentId\": 6000\n" + "\t}],\n" + "\t\"service\": \"SW\",\n"
               + "\t\"traceSegmentId\": \"a12ff60b-5807-463b-a1f8-fb1c8608219e\"\n" + "}";
    }

    @Test
    public void testPost() {
        //        List<Segment> segments = new ArrayList<>();
        //        segments.add();
        sender.post(data);
    }

    @Test
    public void getTime() {
        System.out.println(new Date().getTime());
    }
}
