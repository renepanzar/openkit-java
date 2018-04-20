package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.protocol.dto.Payload;

public class MobileProtocolV3Serializer implements Serializer {
    @Override
    public byte[] serialize(Payload data) {
        return new byte[1];
    }
}
