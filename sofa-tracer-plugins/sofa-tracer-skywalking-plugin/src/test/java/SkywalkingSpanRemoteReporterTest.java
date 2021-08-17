import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.skywalking.SkywalkingSpanRemoteReporter;
import com.alipay.sofa.tracer.plugins.skywalking.adapter.SkywalkingSegmentAdapter;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SkywalkingSpanRemoteReporterTest {
    private SkywalkingSpanRemoteReporter reporter = new SkywalkingSpanRemoteReporter();

    private final String             tracerType = ComponentNameConstants.DATA_SOURCE;

    private SofaTracer               sofaTracer;

    private SofaTracerSpan           sofaTracerSpan;
    @Before
    public void init() throws InterruptedException {
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "SofaTraceZipkinTest")
                .build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest3").start();
        sofaTracerSpan.setTag("tagsStrkey", "tagsStrVal");
        sofaTracerSpan.setTag("tagsBooleankey", true);
        sofaTracerSpan.setTag("tagsBooleankey", 2018);
        sofaTracerSpan.setBaggageItem("baggageKey", "baggageVal");
        sofaTracerSpan.setTag(CommonSpanTags.LOCAL_APP, "SWSofaTracerSpanTest");
        Map<String, String> logMap = new HashMap<String, String>();
        logMap.put("logKey", "logVal");
        LogData logData = new LogData(System.currentTimeMillis(), logMap);
        sofaTracerSpan.log(logData);
        // mock process
        Thread.sleep(30);
        sofaTracerSpan.setEndTime(System.currentTimeMillis());
    }

    @Test
    public void testOnSpanReport() {
        reporter.onSpanReport(sofaTracerSpan);
    }
}
