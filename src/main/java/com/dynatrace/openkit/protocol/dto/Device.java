package com.dynatrace.openkit.protocol.dto;

import com.dynatrace.openkit.core.util.InetAddressValidator;

public class Device {

	private final String clientIPAddress;
	private final String visitorId;

	public Device(String clientIPAddress, String visitorId) {
		if (InetAddressValidator.isValidIP(clientIPAddress)) {
			this.clientIPAddress = clientIPAddress;
		} else {
			this.clientIPAddress = "";
		}
		this.visitorId = visitorId;
	}

	public String getClientIPAddress() {
		return clientIPAddress;
	}

	public String getVisitorId() {
		return visitorId;
	}
}
