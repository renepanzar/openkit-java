package com.dynatrace.openkit.protocol.dto;

import com.dynatrace.openkit.protocol.EventType;

public class Action {

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public int getParentActionId() {
		return parentActionId;
	}

	public void setParentActionId(int parentActionId) {
		this.parentActionId = parentActionId;
	}

	public int getStartSequenceNumber() {
		return startSequenceNumber;
	}

	public void setStartSequenceNumber(int startSequenceNumber) {
		this.startSequenceNumber = startSequenceNumber;
	}

	public int getEndSequenceNumber() {
		return endSequenceNumber;
	}

	public void setEndSequenceNumber(int endSequenceNumber) {
		this.endSequenceNumber = endSequenceNumber;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	// basic event data
	private EventType eventType;
	private String name;
	private int threadId;

	// action specific data
	private int actionId;
	private int parentActionId;
	private int startSequenceNumber;
	private int endSequenceNumber;
	private long startTime;
	private long endTime;
}
