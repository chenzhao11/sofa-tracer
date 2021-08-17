package com.alipay.sofa.tracer.plugins.skywalking.utils.POJO;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.apm.network.language.agent.v3.RefType;

@Setter
@Getter
public class SegmentReference {
    private RefType refType;
    private String traceId;
    private String parentTraceSegmentId;
    private int  parentSpanId;
    private String parentService;
    private String parentServiceInstance;
    private String parentEndpoint;
    // The network address, including ip/hostname and port, which is used in the client side.
    // Such as Client --> use 127.0.11.8:913 -> Server
    // then, in the reference of entry span reported by Server, the value of this field is 127.0.11.8:913.
    // This plays the important role in the SkyWalking STAM(Streaming Topology Analysis Method)
    // For more details, read https://wu-sheng.github.io/STAM/
    private String networkAddressUsedAtPeer;

}
