import com.alibaba.fastjson.JSON;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Log;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Segment;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.SegmentReference;
import com.alipay.sofa.tracer.plugins.skywalking.utils.POJO.Span;

import java.util.List;

public class Test {
    @org.junit.Test
    public void testJson(){
        Segment  segment = new Segment();

        Log log = new Log();
        log.setTime(343435443L);
        log.addLogs("log", "ced");
        log.addLogs("hello","ldfaldhfj");

        SegmentReference segmentReference = new SegmentReference();
        segmentReference.setParentEndpoint("dfadsfasfd");

        Span span = new Span();
        span.setOperationName("ceshi");
        span.setSpanId(145446);
        span.addLog(log);
        span.addSegmentReference(segmentReference);
        span.addTag("tag", "value");
        span.addTag("heljafldfla", "dasdfhash");
        segment.setService("測試");
        segment.addSpan(span);

        System.out.println(JSON.toJSONString(segment));
    }
}
