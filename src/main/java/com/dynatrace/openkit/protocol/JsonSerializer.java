package com.dynatrace.openkit.protocol;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Payload data) {
        return new byte[1];
    }
}
