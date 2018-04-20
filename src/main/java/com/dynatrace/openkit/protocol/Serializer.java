package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.protocol.dto.Payload;

public interface Serializer {

    /**
     * Serialize the Payload into a specific format
     * @param data the data to serialize
     */
    byte[] serialize(Payload data);
}
