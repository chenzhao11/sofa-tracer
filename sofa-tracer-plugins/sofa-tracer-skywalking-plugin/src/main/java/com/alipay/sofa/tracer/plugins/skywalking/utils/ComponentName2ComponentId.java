package com.alipay.sofa.tracer.plugins.skywalking.utils;

import com.alipay.common.tracer.core.constants.ComponentNameConstants;

import java.util.HashMap;

/**
 * 把SOFATracer中的ComponentName转换成SkyWalking中的ComponentId
 */
public class ComponentName2ComponentId {
    public static final HashMap<String,Integer> componentName2IDMap = new HashMap<>();
    static {
        //转换中componentName按照   SW中的Id参考https://github.com/apache/skywalking/blob/master/oap-server/server-bootstrap/src/main/resources/component-libraries.yml
        //关于数据库的类别需要从专门的tag中取得
        componentName2IDMap.put(ComponentNameConstants.DATA_SOURCE,0);
        //SW there is only dubbo
        componentName2IDMap.put(ComponentNameConstants.DUBBO_CLIENT,3);
        componentName2IDMap.put(ComponentNameConstants.DUBBO_SERVER,3);
        componentName2IDMap.put(ComponentNameConstants.HTTP_CLIENT,2);
        componentName2IDMap.put(ComponentNameConstants.OK_HTTP,12);
        //为什么叫做在SW中叫做SpringRestTemplate
        componentName2IDMap.put(ComponentNameConstants.REST_TEMPLATE,13);
        componentName2IDMap.put(ComponentNameConstants.SPRING_MVC,14);
        //下面的组件没有在SW中找到？？
        componentName2IDMap.put(ComponentNameConstants.FLEXIBLE,0);
        componentName2IDMap.put(ComponentNameConstants.MSG_PUB,0);
        componentName2IDMap.put(ComponentNameConstants.MSG_SUB,0);

        componentName2IDMap.put(ComponentNameConstants.FEIGN_CLIENT,11);
        //kafka还有一个标签是kafka没有分消费者和生产者
        componentName2IDMap.put(ComponentNameConstants.KAFKAMQ_CONSUMER,41);
        componentName2IDMap.put(ComponentNameConstants.KAFKAMQ_SEND,40);
        //同上
        componentName2IDMap.put(ComponentNameConstants.ROCKETMQ_CONSUMER,39);
        componentName2IDMap.put(ComponentNameConstants.ROCKETMQ_SEND,38);
        componentName2IDMap.put(ComponentNameConstants.RABBITMQ_CONSUMER,53);
        componentName2IDMap.put(ComponentNameConstants.RABBITMQ_SEND,52);
        //下面两个在SW都有多个ID？
        componentName2IDMap.put(ComponentNameConstants.MONGO_CLIENT,42);
        componentName2IDMap.put(ComponentNameConstants.REDIS,7);

    }
}
