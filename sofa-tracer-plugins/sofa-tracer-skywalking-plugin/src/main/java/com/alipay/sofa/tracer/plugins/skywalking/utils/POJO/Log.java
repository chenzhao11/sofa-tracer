package com.alipay.sofa.tracer.plugins.skywalking.utils.POJO;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Log {
    private Long                     time;
    private List<KeyStringValuePair> data = new LinkedList<>();
    public void addLogs(String key, String value){
        data.add(new KeyStringValuePair(key,value));
    }
}
