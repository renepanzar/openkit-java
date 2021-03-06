/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.protocol.Connector;
import com.dynatrace.openkit.protocol.HTTPConnector;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.ConnectorProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * State context for beacon sending states.
 */
public class BeaconSendingContext {

    /**
     * Default sleep time in milliseconds (used by {@link #sleep()}).
     */
    static final long DEFAULT_SLEEP_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);

    private final Configuration configuration;
    private final ConnectorProvider connectorProvider;
    private final TimingProvider timingProvider;

    /**
     * container storing all open sessions
     */
    private final LinkedBlockingQueue<SessionImpl> openSessions = new LinkedBlockingQueue<SessionImpl>();

    /**
     * container storing all finished sessions
     */
    private final LinkedBlockingQueue<SessionImpl> finishedSessions = new LinkedBlockingQueue<SessionImpl>();
    /**
     * boolean indicating whether shutdown was requested or not
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    /**
     * countdown latch updated when init was done - which can either be success or failure
     */
    private final CountDownLatch initCountDownLatch = new CountDownLatch(1);
    /**
     * current state of beacon sender
     */
    private AbstractBeaconSendingState currentState;
    /**
     * state following after current state, nextState is usually set by doExecute of the current state
     */
    private AbstractBeaconSendingState nextState;
    /**
     * timestamp when open sessions were last sent
     */
    private long lastOpenSessionBeaconSendTime;
    /**
     * timestamp when last status check was done
     */
    private long lastStatusCheckTime;
    /**
     * timestamp when last time sync was done
     */
    private long lastTimeSyncTime = -1;
    /**
     * boolean indicating whether init was successful or not
     */
    private final AtomicBoolean initSucceeded = new AtomicBoolean(false);
    /**
     * boolean indicating whether the server supports a time sync (true) or not (false).
     */
    private boolean timeSyncSupported = true;

    /**
     * Constructor.
     *
     * <p>
     * The state is initialized to {@link BeaconSendingInitState},
     * </p>
     */
    public BeaconSendingContext(Configuration configuration,
                                ConnectorProvider connectorProvider,
                                TimingProvider timingProvider) {

        this.configuration = configuration;
        this.connectorProvider = connectorProvider;
        this.timingProvider = timingProvider;

        currentState = new BeaconSendingInitState();
    }

    /**
     * Executes the current state.
     */
    public void executeCurrentState() {
        nextState = null;
        currentState.execute(this);

        if(nextState != null){ // currentState.execute(...) can trigger state changes
            // TODO: roland.ettinger log transition
            currentState = nextState;
            nextState = null;
        }

    }

    /**
     * Requests a shutdown.
     */
    public void requestShutdown() {
        shutdown.set(true);
    }

    /**
     * Gets a boolean flag indicating whether shutdown was requested before or not.
     */
    public boolean isShutdownRequested() {
        return shutdown.get();
    }

    /**
     * Wait until OpenKit has been fully initialized.
     *
     * <p>
     * If initialization is interrupted (e.g. {@link #requestShutdown()} was called), then this method also returns.
     * </p>
     *
     * @return {@code true} OpenKit is fully initialized, {@code false} OpenKit init got interrupted.
     */
    public boolean waitForInit() {
        try {
            initCountDownLatch.await();
        } catch (InterruptedException e) {
            requestShutdown();
            Thread.currentThread().interrupt();
        }
        return initSucceeded.get();
    }

    /**
     * Wait until OpenKit has been fully initialized or timeout expired.
     *
     * <p>
     * If initialization is interrupted (e.g. {@link #requestShutdown()} was called), then this method also returns.
     * </p>
     *
     * @param timeoutMillis
     *            The maximum number of milliseconds to wait for initialization being completed.
     * @return {@code true} if OpenKit is fully initialized, {@code false} if OpenKit init got interrupted or time to wait expired.
     */
    public boolean waitForInit(long timeoutMillis) {
        try {
            if (!initCountDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                return false; // timeout expired
            }
        } catch (InterruptedException e) {
            requestShutdown();
            Thread.currentThread().interrupt();
        }

        return initSucceeded.get();
    }

    /**
     * Get a boolean indicating whether OpenKit is initialized or not.
     *
     * @return {@code true} if OpenKit is initialized, {@code false} otherwise.
     */
    public boolean isInitialized() {
        return initSucceeded.get();
    }

    /**
     * Gets a boolean indicating whether the current state is a terminal state or not.
     *
     * @return {@code true} if the current state is a terminal state, {@code false} otherwise.
     */
    public boolean isInTerminalState() {
        return currentState.isTerminalState();
    }

    /**
     * Gets a boolean flag indicating whether capturing is turned on or off.
     *
     * @return {@code true} if capturing is turned on, {@code false} otherwise.
     */
    public boolean isCaptureOn() {
        return configuration.isCapture();
    }

    /**
     * Initialize time synchronization with cluster time.
     *
     * @param clusterTimeOffset   the cluster offset
     * @param isTimeSyncSupported {@code true} if time sync is supported, otherwise {@code false}
     */
    void initializeTimeSync(long clusterTimeOffset, boolean isTimeSyncSupported) {
        timingProvider.initialize(clusterTimeOffset, isTimeSyncSupported);
    }

    /**
     * Gets a boolean flag indicating whether time sync is supported or not.
     *
     * @return {@code true} if time sync is supported, {@code false} otherwise.
     */
    boolean isTimeSyncSupported() {
        return timeSyncSupported;
    }

    /**
     * Disables the time sync
     */
    public void disableTimeSyncSupport() {
        timeSyncSupported = false;
    }

    /**
     * Gets a boolean flag indicating whether the time sync has been performed before
     *
     * @return {@code true} if time sync was performed, {@code false} otherwise
     */
    boolean isTimeSynced() {
        return !isTimeSyncSupported() || getLastTimeSyncTime() >= 0;
    }

    /**
     * Gets the current state.
     *
     * @return current state.
     */
    AbstractBeaconSendingState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the next state.
     *
     * @param nextState Next state when state transition is performed.
     */
    void setNextState(AbstractBeaconSendingState nextState) {
        currentState = nextState;
    }

    /**
     * Complete OpenKit initialization.

     * <p>
     * This will wake up every caller waiting in the {@link #waitForInit()} method.
     * </p>
     *
     * @param success {@code true} if OpenKit was successfully initialized, {@code false} if it was interrupted.
     */
    void initCompleted(boolean success) {
        initSucceeded.set(success);
        initCountDownLatch.countDown();
    }

    /**
     * Gets the HTTP client provider.
     *
     * @return A class responsible for retrieving an instance of {@link HTTPConnector}.
     */
    ConnectorProvider getHTTPClientProvider() {
        return connectorProvider;
    }

    /**
     * Convenience method to retrieve an HTTP client.
     *
     * @return HTTP client received from {@link ConnectorProvider}.
     */
    Connector getConnector() {
        return connectorProvider.createConnector(configuration.getHttpClientConfig());
    }

    /**
     * Gets the current timestamp.
     *
     * @return current timestamp as milliseconds elapsed since epoch (1970-01-01T00:00:00.000)
     */
    long getCurrentTimestamp() {
        return timingProvider.provideTimestampInMilliseconds();
    }

    /**
     * Sleep some time ({@link #DEFAULT_SLEEP_TIME_MILLISECONDS}.
     *
     * @throws InterruptedException When sleeping thread got interrupted.
     */
    void sleep() throws InterruptedException {
        sleep(DEFAULT_SLEEP_TIME_MILLISECONDS);
    }

    /**
     * Sleep given amount of milliseconds.
     *
     * @param millis The number of milliseconds to sleep.
     * @throws InterruptedException When sleeping thread got interrupted.
     */
    void sleep(long millis) throws InterruptedException {
        timingProvider.sleep(millis);
    }

    /**
     * Get timestamp when open sessions were sent last.
     */
    long getLastOpenSessionBeaconSendTime() {
        return lastOpenSessionBeaconSendTime;
    }

    /**
     * Set timestamp when open sessions were sent last.
     */
    void setLastOpenSessionBeaconSendTime(long timestamp) {
        lastOpenSessionBeaconSendTime = timestamp;
    }

    /**
     * Get timestamp when last status check was performed.
     */
    long getLastStatusCheckTime() {
        return lastStatusCheckTime;
    }

    /**
     * Set timestamp when last status check was performed.
     */
    void setLastStatusCheckTime(long timestamp) {
        lastStatusCheckTime = timestamp;
    }

    /**
     * Get the send interval for open sessions.
     */
    int getSendInterval() {
        return configuration.getSendInterval();
    }

    /**
     * Disable data capturing.
     */
    void disableCapture() {
        // first disable in configuration, so no further data will get collected
        configuration.disableCapture();
        clearAllSessionData();
    }

    /**
     * Handle the status response received from the server.
     */
    void handleStatusResponse(StatusResponse statusResponse) {
        configuration.updateSettings(statusResponse);

        if (!isCaptureOn()) {
            // capturing was turned off
            clearAllSessionData();
        }
    }

    /**
     * Clear captured data from all sessions.
     */
    private void clearAllSessionData() {
        // clear captured data from finished sessions
        for (SessionImpl session : finishedSessions) {
            session.clearCapturedData();
        }
        finishedSessions.clear(); // clear finished sessions also

        // clear captured data from open sessions
        for (SessionImpl session : openSessions) {
            session.clearCapturedData();
        }
    }

    /**
     * Gets the next finished session from the list of all finished sessions.
     * <p>
     * <p>
     * This call also removes the session from the underlying data structure.
     * If there are no finished sessions any more, this method returns null.
     * </p>
     *
     * @return A finished session or {@code null} if there is no finished session.
     */
    SessionImpl getNextFinishedSession() {
        return finishedSessions.poll();
    }

    /**
     * Gets all open sessions.
     * <p>
     * <p>
     * This returns a shallow copy of all open sessions.
     * </p>
     */
    SessionImpl[] getAllOpenSessions() {
        return openSessions.toArray(new SessionImpl[0]);
    }

    /**
     * Gets all finished sessions.
     * <p>
     * <p>
     * This returns a shallow copy of all finished sessions and is intended only
     * for testing purposes.
     * </p>
     */
    SessionImpl[] getAllFinishedSessions() {
        return finishedSessions.toArray(new SessionImpl[0]);
    }

    /**
     * Gets the timestamp when time sync was executed last time.
     */
    long getLastTimeSyncTime() {
        return lastTimeSyncTime;
    }

    /**
     * Sets the timestamp when time sync was executed last time.
     */
    void setLastTimeSyncTime(long lastTimeSyncTime) {
        this.lastTimeSyncTime = lastTimeSyncTime;
    }

    /**
     * Start a new session.
     * <p>
     * <p>
     * This add the {@code session} to the internal container of open sessions.
     * </p>
     *
     * @param session The new session to start.
     */
    public void startSession(SessionImpl session) {
        openSessions.add(session);
    }

    /**
     * Push back a finished session, that was previously retrieved via {@link #getNextFinishedSession()}.
     *
     * <p>
     * This method will not check for duplicate entries, so be careful what's pushed back.
     * </p>
     *
     * @param session The session to push back to the list of finished ones.
     */
    public void pushBackFinishedSession(SessionImpl session) {
        finishedSessions.add(session);
    }

    /**
     * Finish a session which has been started previously using {@link #startSession(SessionImpl)}.
     * <p>
     * <p>
     * If the session cannot be found in the container storing all open sessions, the parameter is ignored,
     * otherwise it's removed from the container storing open sessions and added to the finished session container.
     * </p>
     *
     * @param session The session to finish.
     */
    public void finishSession(SessionImpl session) {
        if (openSessions.remove(session)) {
            finishedSessions.add(session);
        }
    }
}
