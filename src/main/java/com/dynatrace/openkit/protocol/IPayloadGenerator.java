package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.core.ActionImpl;
import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.WebRequestTracerBaseImpl;

public interface IPayloadGenerator {

	int createID();

	long getCurrentTimestamp();

	int createSequenceNumber();

	String createTag(ActionImpl parentAction, int sequenceNo);

	void addAction(ActionImpl action);

	void endSession(SessionImpl session);

	void reportValue(ActionImpl parentAction, String valueName, int value);

	void reportValue(ActionImpl parentAction, String valueName, double value);

	void reportValue(ActionImpl parentAction, String valueName, String value);

	void reportEvent(ActionImpl parentAction, String eventName);

	void reportError(ActionImpl parentAction, String errorName, int errorCode, String reason);

	void reportCrash(String errorName, String reason, String stacktrace);

	void addWebRequest(ActionImpl parentAction, WebRequestTracerBaseImpl webRequestTracer);

	void identifyUser(String userTag);

	StatusResponse send();

	void clearData();

	boolean isEmpty();
}
