package com.dynatrace.openkit.protocol;

public interface Serializer {

    /**
     * Serialize the Payload into a specific format
     * @param data the data to serialize
     */
    void serialize(Payload data);
}
