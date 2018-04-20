package com.dynatrace.openkit.protocol;

public class MQTTConnector implements Connector {

    Serializer serializer;

    /**
     * Constructs a MQTTConnection given a serializer
     * @param serializer serializer to use for the MQTTConnection
     */
    MQTTConnector(Serializer serializer){
        this.serializer = serializer;
    }

    @Override
    public void init() {

    }

    @Override
    public void send(Payload data) {

    }
}
