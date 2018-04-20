package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.protocol.dto.Action;
import com.dynatrace.openkit.protocol.dto.Payload;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MobileProtocolV3Serializer implements Serializer {

    private static final String BEACON_KEY_PROTOCOL_VERSION = "vv";
    private static final String BEACON_KEY_OPENKIT_VERSION = "va";

    private static final String BEACON_KEY_PLATFORM_TYPE = "pt";
    private static final String BEACON_KEY_AGENT_TECHNOLOGY_TYPE = "tt";

    private static final String BEACON_KEY_VISITOR_ID = "vi";
    private static final String BEACON_KEY_SESSION_NUMBER = "sn";
    private static final String BEACON_KEY_CLIENT_IP_ADDRESS = "ip";

    // Action related constants
    private static final String BEACON_KEY_EVENT_TYPE = "et";
    private static final String BEACON_KEY_NAME = "na";
    private static final String BEACON_KEY_THREAD_ID = "it";
    private static final String BEACON_KEY_ACTION_ID = "ca";
    private static final String BEACON_KEY_PARENT_ACTION_ID = "pa";
    private static final String BEACON_KEY_START_SEQUENCE_NUMBER = "s0";
    private static final String BEACON_KEY_TIME_0 = "t0";
    private static final String BEACON_KEY_END_SEQUENCE_NUMBER = "s1";
    private static final String BEACON_KEY_TIME_1 = "t1";

    // version constants
    public static final String OPENKIT_VERSION = "7.0.0000";
    private static final int PROTOCOL_VERSION = 3;
    private static final int PLATFORM_TYPE_OPENKIT = 1;
    private static final String AGENT_TECHNOLOGY_TYPE = "okjava";

    private static final String BEACON_KEY_SESSION_START_TIME = "tv";
    private static final String BEACON_KEY_TIMESYNC_TIME = "ts";

    private static final char BEACON_DATA_DELIMITER = '&';


    // in Java 6 there is no constant for "UTF-8" in the JDK yet, so we define it ourselves
    public static final String CHARSET = "UTF-8";

    @Override
    public byte[] serialize(Payload data) {
        String basicData;
        try {
            basicData = createBasicBeaconData(data) + BEACON_DATA_DELIMITER + createTimestampData(data);
            basicData += serializeActions(data);
            return basicData.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            // if encoding fails, skip the payload
            return new byte[1];
        }


    }

    /**
     * Serialization helper method for adding key/value pairs with string values
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     * @param stringValue The value to add.
     */
    private void addKeyValuePair(StringBuilder builder, String key, String stringValue) {
        String encodedValue;
        try {
            encodedValue = URLEncoder.encode(stringValue, CHARSET);
        } catch (UnsupportedEncodingException e) {
            // if encoding fails, skip this key/value pair
            return;
        }

        appendKey(builder, key);
        builder.append(encodedValue);
    }

    /**
     * Serialization helper method for adding key/value pairs with long values
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     * @param longValue The value to add.
     */
    private void addKeyValuePair(StringBuilder builder, String key, long longValue) {
        appendKey(builder, key);
        builder.append(longValue);
    }

    /**
     * Serialization helper method for adding key/value pairs with int values
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     * @param intValue The value to add.
     */
    private void addKeyValuePair(StringBuilder builder, String key, int intValue) {
        appendKey(builder, key);
        builder.append(intValue);
    }

    /**
     * Serialization helper method for appending a key.
     *
     * @param builder The string builder storing serialized data.
     * @param key The key to add.
     */
    private void appendKey(StringBuilder builder, String key) {
        if (!builder.toString().isEmpty()) {
            builder.append('&');
        }
        builder.append(key);
        builder.append('=');
    }

    private String createBasicBeaconData(Payload data) {
        StringBuilder basicBeaconBuilder = new StringBuilder();

        // version and application information
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_PROTOCOL_VERSION, PROTOCOL_VERSION);
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_OPENKIT_VERSION, OPENKIT_VERSION);
        /*addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_ID, configuration.getApplicationID());
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_NAME, configuration.getApplicationName());
        if (configuration.getApplicationVersion() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_APPLICATION_VERSION, configuration.getApplicationVersion());
        }*/
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_PLATFORM_TYPE, PLATFORM_TYPE_OPENKIT);
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_AGENT_TECHNOLOGY_TYPE, AGENT_TECHNOLOGY_TYPE);

        // device/visitor ID, session number and IP address
        String visitorID = data.getDevice().getVisitorId();
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_VISITOR_ID, visitorID);
        int sessionNumber = data.getSession().getId();
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_SESSION_NUMBER, sessionNumber);
        String clientIPAddress = data.getDevice().getClientIPAddress();
        addKeyValuePair(basicBeaconBuilder, BEACON_KEY_CLIENT_IP_ADDRESS, clientIPAddress);

        // platform information
        /*if (configuration.getDevice().getOperatingSystem() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_OS, configuration.getDevice().getOperatingSystem());
        }
        if (configuration.getDevice().getManufacturer() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_MANUFACTURER, configuration.getDevice()
                                                                                             .getManufacturer());
        }
        if (configuration.getDevice().getModelID() != null) {
            addKeyValuePair(basicBeaconBuilder, BEACON_KEY_DEVICE_MODEL, configuration.getDevice().getModelID());
        }*/

        return basicBeaconBuilder.toString();
    }

    /**
     * Serialization helper method for creating basic timestamp data.
     *
     * @return Serialized data.
     */
    private String createTimestampData(Payload data) {
        StringBuilder timestampBuilder = new StringBuilder();

        // timestamp information
        addKeyValuePair(timestampBuilder, BEACON_KEY_SESSION_START_TIME, data.getSession().getStartTime());
        addKeyValuePair(timestampBuilder, BEACON_KEY_TIMESYNC_TIME, data.getSession().getStartTime());
       /* if (!timingProvider.isTimeSyncSupported()) {
            addKeyValuePair(timestampBuilder, BEACON_KEY_TRANSMISSION_TIME, timingProvider.provideTimestampInMilliseconds());
        }*/

        return timestampBuilder.toString();
    }

    /**
     * Serialization for building basic event data.
     *
     * @param builder String builder storing serialized data.
     * @param eventType The event's type.
     * @param name Event's name.
     */
    private void buildBasicEventData(StringBuilder builder, EventType eventType, String name, int threadID) {
        addKeyValuePair(builder, BEACON_KEY_EVENT_TYPE, eventType.protocolValue());
        if (name != null) {
            addKeyValuePair(builder, BEACON_KEY_NAME, name);
        }
        addKeyValuePair(builder, BEACON_KEY_THREAD_ID, threadID);
    }

    public String addAction(Action action) {
        StringBuilder actionBuilder = new StringBuilder();

        buildBasicEventData(actionBuilder, action.getEventType(), action.getName(), action.getThreadId());

        addKeyValuePair(actionBuilder, BEACON_KEY_ACTION_ID, action.getActionId());
        addKeyValuePair(actionBuilder, BEACON_KEY_PARENT_ACTION_ID, action.getParentActionId());
        addKeyValuePair(actionBuilder, BEACON_KEY_START_SEQUENCE_NUMBER, action.getStartSequenceNumber());
        addKeyValuePair(actionBuilder, BEACON_KEY_TIME_0, action.getStartTime());
        addKeyValuePair(actionBuilder, BEACON_KEY_END_SEQUENCE_NUMBER, action.getEndSequenceNumber());
        addKeyValuePair(actionBuilder, BEACON_KEY_TIME_1, action.getEndTime());

        return actionBuilder.toString();
    }

    private String serializeActions(Payload data) {
        String serializedActions = "";

        for (Action a : data.getActions()) {
            serializedActions += BEACON_DATA_DELIMITER + addAction(a);
        }

        return serializedActions;
    }
}
