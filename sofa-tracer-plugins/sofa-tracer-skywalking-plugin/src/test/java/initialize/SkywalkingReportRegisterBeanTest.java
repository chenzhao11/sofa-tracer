package initialize;

import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.sofa.tracer.plugins.skywalking.initialize.SkywalkingReportRegisterBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SkywalkingReportRegisterBeanTest {
    private ClassPathXmlApplicationContext applicationContext;

    @Before
    public void init() {
        applicationContext = new ClassPathXmlApplicationContext("spring-bean.xml");
    }

    @Test
    public void testAfterPropertiesSet() {
        Object zipkinReportRegisterBean = applicationContext.getBean("SkywalkingReportRegisterBean");
        Assert.assertTrue(zipkinReportRegisterBean instanceof SkywalkingReportRegisterBean);
        Assert.assertTrue(SpanReportListenerHolder.getSpanReportListenersHolder().size() > 0);
    }
}
