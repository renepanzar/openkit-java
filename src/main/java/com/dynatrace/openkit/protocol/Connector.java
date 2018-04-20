package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.protocol.dto.Payload;

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

    /**
     * Returns the application id
     * @return application id
     */
    String getApplicationID();
}
