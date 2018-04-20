package com.dynatrace.openkit.protocol.dto;

public class Session {
	private final int id;
	private final long startTime;

	public Session(int id, long startTime) {
		this.id = id;
		this.startTime = startTime;
	}

	public int getId() {
		return id;
	}

	public long getStartTime() {
		return startTime;
	}
}
