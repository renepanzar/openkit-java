package com.dynatrace.openkit.protocol;

public interface Connector {
    /**
     * Initialise the connector
     */
    void init();

    /**
     * Send the data on the given Connector
     * @param data payload to send
     */
    void send(Payload data);
}
