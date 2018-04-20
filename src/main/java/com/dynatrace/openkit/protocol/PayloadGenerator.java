package com.dynatrace.openkit.protocol;

import java.util.concurrent.atomic.AtomicInteger;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.ActionImpl;
import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.WebRequestTracerBaseImpl;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.dto.Action;
import com.dynatrace.openkit.protocol.dto.Payload;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

public class PayloadGenerator {

	// protocol version
	private static final int PROTOCOL_VERSION = 3;
	private static final int PLATFORM_TYPE_OPENKIT = 1;
	private static final String AGENT_TECHNOLOGY_TYPE = "okjava";

	// max name length
	private static final int MAX_NAME_LEN = 250;

	// web request tag prefix constant
	private static final String TAG_PREFIX = "MT";
	private final long sessionStartTime;

	// next ID and sequence number
	private AtomicInteger nextID = new AtomicInteger(0);
	private AtomicInteger nextSequenceNumber = new AtomicInteger(0);

	// AbstractConfiguration reference
	private final Configuration configuration;

	// dependencies
	private final Logger logger;
	private final BeaconCacheImpl beaconCache;
	private final TimingProvider timingProvider;
	private final ThreadIDProvider threadIDProvider;

	private final Payload payload;

	// *** constructors ***

	/**
	 * Constructor.
	 *
	 * @param logger Logger for logging messages.
	 * @param beaconCache Cache storing beacon related data.
	 * @param configuration OpenKit related configuration.
	 * @param clientIPAddress The client's IP address.
	 * @param threadIDProvider Provider for retrieving thread id.
	 * @param timingProvider Provider for time related methods.
	 */
	public PayloadGenerator(Logger logger, BeaconCacheImpl beaconCache, Configuration configuration, String clientIPAddress, ThreadIDProvider threadIDProvider, TimingProvider timingProvider) {
		this.logger = logger;
		this.beaconCache = beaconCache;
		this.timingProvider = timingProvider;

		this.configuration = configuration;
		this.threadIDProvider = threadIDProvider;
		this.sessionStartTime = timingProvider.provideTimestampInMilliseconds();

		this.payload = new Payload(
				clientIPAddress,
				configuration.createSessionNumber(),
				sessionStartTime,
				String.valueOf(configuration.getDeviceID()));
	}

	/**
	 * Create a unique identifier.
	 *
	 * <p>
	 * The identifier returned is only unique per Beacon.
	 * Calling this method on two different Beacon instances, might give the same result.
	 * </p>
	 *
	 * @return A unique identifier.
	 */
	public int createID() {
		return nextID.incrementAndGet();
	}

	/**
	 * Get the current timestamp in milliseconds by delegating to TimingProvider
	 *
	 * @return Current timestamp in milliseconds.
	 */
	public long getCurrentTimestamp() {
		return timingProvider.provideTimestampInMilliseconds();
	}

	/**
	 * Create a unique sequence number.
	 *
	 * <p>
	 * The sequence number returned is only unique per Beacon.
	 * Calling this method on two different Beacon instances, might give the same result.
	 * </p>
	 *
	 * @return A unique sequence number.
	 */
	public int createSequenceNumber() {
		return nextSequenceNumber.incrementAndGet();
	}

	/**
	 * Add {@link ActionImpl} to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param action The action to add.
	 */
	public void addAction(ActionImpl action) {
		Action actionDto = new Action();

		// fill with data

		synchronized (this) {
			payload.addAction(actionDto);
		}
	}

	/**
	 * Add {@link SessionImpl} to Beacon when session is ended.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param session The session to add.
	 */
	public void endSession(SessionImpl session) {
		// not yet implemented
	}

	/**
	 * Add key-value-pair to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this value was reported.
	 * @param valueName Value's name.
	 * @param value Actual value to report.
	 */
	public void reportValue(ActionImpl parentAction, String valueName, int value) {
		// not yet implemented
	}

	/**
	 * Add key-value-pair to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this value was reported.
	 * @param valueName Value's name.
	 * @param value Actual value to report.
	 */
	public void reportValue(ActionImpl parentAction, String valueName, double value) {
		// not yet implemented
	}

	/**
	 * Add key-value-pair to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this value was reported.
	 * @param valueName Value's name.
	 * @param value Actual value to report.
	 */
	public void reportValue(ActionImpl parentAction, String valueName, String value) {
		// not yet implemented
	}

	/**
	 * Add event (aka. named event) to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this event was reported.
	 * @param eventName Event's name.
	 */
	public void reportEvent(ActionImpl parentAction, String eventName) {
		// not yet implemented
	}

	/**
	 * Add error to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this error was reported.
	 * @param errorName Error's name.
	 * @param errorCode Some error code.
	 * @param reason Reason for that error.
	 */
	public void reportError(ActionImpl parentAction, String errorName, int errorCode, String reason) {
		// not yet implemented
	}

	/**
	 * Add crash to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param errorName Error's name.
	 * @param reason Reason for that error.
	 * @param stacktrace Crash stacktrace.
	 */
	public void reportCrash(String errorName, String reason, String stacktrace) {
		// not yet implemented
	}

	/**
	 * Add web request to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param parentAction The {@link com.dynatrace.openkit.api.Action} on which this web request was reported.
	 * @param webRequestTracer Web request tracer to serialize.
	 */
	public void addWebRequest(ActionImpl parentAction, WebRequestTracerBaseImpl webRequestTracer) {
		// not yet implemented
	}
	/**
	 * Add user identification to Beacon.
	 *
	 * <p>
	 * The serialized data is added to {@link com.dynatrace.openkit.core.caching.BeaconCache}.
	 * </p>
	 *
	 * @param userTag User tag containing data to serialize.
	 */
	public void identifyUser(String userTag) {
		// not yet implemented
	}

	/**
	 * Send current state of Beacon.
	 *
	 * <p>
	 * This method tries to send all so far collected and serialized data.
	 * </p>
	 *
	 * @param provider Provider for getting an {@link HTTPConnector} required to send the data.
	 *
	 * @return Returns the last status response retrieved from the server side, or {@code null} if an error occurred.
	 */
	public StatusResponse send() {

		// TODO introduce connector

		synchronized (this) {
			// TODO send
			payload.clearActions();
		}

		// return valid dummy response
		return new StatusResponse( "",200);
	}

	/**
	 * Clears all previously collected data for this Beacon.
	 *
	 * <p>
	 * This only affects the so far serialized data, which gets removed from the cache.
	 * </p>
	 */
	public void clearData() {

	}

	/**
	 * helper method for truncating name at max name size
	 */
	private String truncate(String name) {
		name = name.trim();
		if (name.length() > MAX_NAME_LEN) {
			name = name.substring(0, MAX_NAME_LEN);
		}
		return name;
	}

	/**
	 * Get a timestamp relative to the time this session (aka. beacon) was created.
	 *
	 * @param timestamp The absolute timestamp for which to get a relative one.
	 * @return Relative timestamp.
	 */
	private long getTimeSinceSessionStartTime(long timestamp) {
		return timestamp - sessionStartTime;
	}
}
