package sender;

import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

public class SkywalkingRestTemplateSenderTest {
    String data;
    SkywalkingRestTemplateSender sender ;

    @Before
    public void init(){
    sender = new SkywalkingRestTemplateSender(new RestTemplate(), "http://127.0.0.1:12800");
    data = "{\n" + "\t\"traceId\": \"a12ffb-5807-463b-a1f8-fb1c860821fdrg9e\",\n"
           + "\t\"serviceInstance\": \"SWTEST\",\n" + "\t\"spans\": [{\n"
           + "\t\t\"operationName\": \"/develop\",\n" + "\t\t\"startTime\": 1629096916596,\n"
           + "\t\t\"endTime\": 1629096916600,\n" + "\t\t\"spanType\": \"Exit\",\n"
           + "\t\t\"spanId\": 1,\n" + "\t\t\"isError\": false,\n" + "\t\t\"parentSpanId\": 0,\n"
           + "\t\t\"componentId\": 6000,\n" + "\t\t\"peer\": \"upstream service\",\n"
           + "\t\t\"spanLayer\": \"Http\"\n" + "\t}, {\n" + "\t\t\"operationName\": \"/ingress\",\n"
           + "\t\t\"startTime\": 1629096916596,\n" + "\t\t\"tags\": [{\n"
           + "\t\t\t\"key\": \"http.method\",\n" + "\t\t\t\"value\": \"GET\"\n" + "\t\t}, {\n"
           + "\t\t\t\"key\": \"http.params\",\n" + "\t\t\t\"value\": \"http://localhost/ingress\"\n"
           + "\t\t}],\n" + "\t\t\"endTime\": 1629096916677,\n" + "\t\t\"spanType\": \"Entry\",\n"
           + "\t\t\"spanId\": 0,\n" + "\t\t\"parentSpanId\": -1,\n" + "\t\t\"isError\": false,\n"
           + "\t\t\"spanLayer\": \"Http\",\n" + "\t\t\"componentId\": 6000\n" + "\t}],\n"
           + "\t\"service\": \"SW\",\n"
           + "\t\"traceSegmentId\": \"a12ff60b-5807-463b-a1f8-fb1c8608219e\"\n" + "}";
    }
    @Test
    public void testPost(){

        sender.post(data);
    }
    @Test
    public void getTime(){
        System.out.println(new Date().getTime());
    }
}
