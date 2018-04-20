package com.dynatrace.openkit.protocol;

public class HTTPConnector implements Connector {

    private Serializer serializer;
    private String applicationID;
    private String endpointURL;

    /**
     * Constructs a HTTPConnector given a serializer and the endpointURL/applicationID configuration
     * @param applicationID application id to use
     * @param endpointURL endpoint url to connect to
     * @param serializer serializer to use for the HTTPConnector
     */
    HTTPConnector(String applicationID, String endpointURL, Serializer serializer){
        this.serializer = serializer;
        this.applicationID = applicationID;
        this.endpointURL = endpointURL;
    }

    @Override
    public void init() {

    }

    @Override
    public void send(Payload data) {

    }
}
