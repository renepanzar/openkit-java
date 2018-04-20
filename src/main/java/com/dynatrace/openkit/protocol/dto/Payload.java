package com.dynatrace.openkit.protocol.dto;

import java.util.ArrayList;
import java.util.List;

public class Payload {

	private final List<Action> actions = new ArrayList<Action>();
	private final Session session;
	private final Device device;

	public Payload(String clientIPAddress, int sessionNumber, long sessionStartTime, String visitorId) {
		this.device = new Device(clientIPAddress, visitorId);
		this.session = new Session(sessionNumber, sessionStartTime);
	}

	public Device getDevice() {
		return device;
	}

	public Session getSession() {
		return session;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void addAction(Action action) {
		actions.add(action);
	}

	public void clearActions() {
		actions.clear();
	}
}
