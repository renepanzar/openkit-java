package com.dynatrace.openkit.protocol;

import com.dynatrace.openkit.protocol.dto.Payload;

public interface Connector {
	/**
	 * sends a status check request and returns a status response
	 *
	 * @return
	 */
	public StatusResponse sendStatusRequest();

	/**
	 * sends a beacon send request and returns a status response
	 *
	 * @param clientIPAddress
	 * @param payload
	 * @return
	 */
	public StatusResponse sendBeaconRequest(String clientIPAddress, Payload payload);

	/**
	 * sends a time sync request and returns a time sync response
	 *
	 * @return
	 */
	public TimeSyncResponse sendTimeSyncRequest();
}
