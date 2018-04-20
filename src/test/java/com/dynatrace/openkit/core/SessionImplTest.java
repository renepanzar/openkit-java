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

package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.core.caching.BeaconCacheImpl;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.Beacon;
import com.dynatrace.openkit.protocol.HTTPConnector;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.ConnectorProvider;
import com.dynatrace.openkit.providers.ThreadIDProvider;
import com.dynatrace.openkit.providers.TimingProvider;

import org.junit.Before;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Tests the session implementation having some knowledge of the internals of beacon and beacon cache.
 */
public class SessionImplTest {

    private static final String APP_ID = "appID";
    private static final String APP_NAME = "appName";

    private Logger logger;
    private Beacon beacon;
    private BeaconSender beaconSender;

    @Before
    public void setUp() {
        beacon = createBeacon(null);
    }

    private Beacon createBeacon(ConnectorProvider provider) {
		logger = mock(Logger.class);
		when(logger.isInfoEnabled()).thenReturn(true);
		when(logger.isDebugEnabled()).thenReturn(true);
		beaconSender = mock(BeaconSender.class);
		final BeaconCacheImpl beaconCache = new BeaconCacheImpl();
		final Configuration configuration = mock(Configuration.class);
		when(configuration.getApplicationID()).thenReturn(APP_ID);
		when(configuration.getApplicationName()).thenReturn(APP_NAME);
		when(configuration.getDevice()).thenReturn(new Device("", "", ""));
		when(configuration.isCapture()).thenReturn(true);
		when(configuration.getDeviceID()).thenReturn("w42");
		when(configuration.getMaxBeaconSize()).thenReturn(30 * 1024); // 30kB=default size
		final String clientIPAddress = "127.0.0.1";
		final ThreadIDProvider threadIDProvider = mock(ThreadIDProvider.class);
		final TimingProvider timingProvider = mock(TimingProvider.class);
		final ConnectorProvider clientProvider = provider == null ? mock(ConnectorProvider.class) : provider;
		when(timingProvider.provideTimestampInMilliseconds()).thenReturn(System.currentTimeMillis());
		return new Beacon(logger, beaconCache, configuration, clientIPAddress, threadIDProvider, timingProvider, clientProvider);
	}

    @Test
    public void constructor() {
        // test the constructor call
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // verify default values
        assertThat(session, notNullValue());
        assertThat(session.getEndTime(), is(-1L));
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void enterActionWithNullActionName() {
        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // add/enter "null-action"
        final RootAction rootAction = session.enterAction(null);

        // we definitely got a NullRootAction instance
        assertThat(rootAction, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(logger, times(1)).warning("Session.enterAction: actionName must not be null or empty");
    }

    @Test
    public void enterActionWithEmptyActionName() {
        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // add/enter "null-action"
        final RootAction rootAction = session.enterAction("");

        // we definitely got a NullRootAction instance
        assertThat(rootAction, is(instanceOf(NullRootAction.class)));

        // ensure that some log message has been written
        verify(logger, times(1)).warning("Session.enterAction: actionName must not be null or empty");
    }

    @Test
    public void enterNotClosedAction() {
        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // add/enter one action
        final RootAction rootAction = session.enterAction("Some action");
        assertThat(rootAction, notNullValue());

        // verify that (because the actions is still active) it is not in the beacon cache (thus the cache is empty)
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void enterSingleAction() {
        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // add/enter one action
        final RootAction rootAction = session.enterAction("Some action");
        rootAction.leaveAction();

        // verify that the action is closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));
    }

    @Test
    public void enterMultipleActions() {
        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // add/enter two actions
        final RootAction rootAction1 = session.enterAction("Some action 1");
        rootAction1.leaveAction();
        final RootAction rootAction2 = session.enterAction("Some action 2");
        rootAction2.leaveAction();

        // verify that the actions are closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));
    }

    @Test
    public void enterSameAction() {
        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // add/enter two actions
        final RootAction rootAction1 = session.enterAction("Some action");
        rootAction1.leaveAction();
        final RootAction rootAction2 = session.enterAction("Some action");
        rootAction2.leaveAction();

        // verify that the actions are closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));
        // verify that multiple actions with same name are possible
        assertThat(rootAction1, not(rootAction2));
    }

    @Test
    public void identifyUserWithNullTagDoesNothing() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // identify a "null-user" must be possible
        session.identifyUser(null);

        // verify the correct methods being called
        verify(logger, times(1)).warning("Session.identifyUser: userTag must not be null or empty");
        verifyZeroInteractions(beacon);
        verify(beacon, times(0)).identifyUser(anyString());
    }

    @Test
    public void identifyUserWithEmptyTagDoesNothing() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // identify a "null-user" must be possible
        session.identifyUser("");

        // verify the correct methods being called
        verify(logger, times(1)).warning("Session.identifyUser: userTag must not be null or empty");
        verifyZeroInteractions(beacon);
        verify(beacon, times(0)).identifyUser(anyString());
    }

    @Test
    public void identifySingleUser() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // identify a single user
        final String userTag = "Some user";
        session.identifyUser(userTag);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(1)).identifyUser(userTag);
    }

    @Test
    public void identifyMultipleUsers() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // identify multiple users
        final String userTag1 = "Some user";
        final String userTag2 = "Some other user";
        final String userTag3 = "Yet another user";
        session.identifyUser(userTag1);
        session.identifyUser(userTag2);
        session.identifyUser(userTag3);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(1)).identifyUser(userTag1);
        verify(beacon, times(1)).identifyUser(userTag2);
        verify(beacon, times(1)).identifyUser(userTag3);
    }

    @Test
    public void identifySameUser() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // identify the same user twice
        final String userTag = "Some user";
        session.identifyUser(userTag);
        session.identifyUser(userTag);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(2)).identifyUser(userTag);
    }

    @Test
    public void reportingCrashWithNullErrorNameDoesNotReportAnything() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // report a crash, passing null values
        session.reportCrash(null, "some reason", "some stack trace");

        // verify the correct methods being called
        verify(logger, times(1)).warning("Session.reportCrash: errorName must not be null or empty");
        verifyZeroInteractions(beacon, beacon);
    }

    @Test
    public void reportingCrashWithEmptyErrorNameDoesNotReportAnything() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // report a crash, passing null values
        session.reportCrash("", "some reason", "some stack trace");

        // verify the correct methods being called
        verify(logger, times(1)).warning("Session.reportCrash: errorName must not be null or empty");
        verifyZeroInteractions(beacon, beacon);
    }

    @Test
    public void reportingCrashWithNullReasonAndStacktraceWorks() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // report a crash, passing null values
        session.reportCrash("errorName", null, null);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(1)).reportCrash("errorName", null, null);
    }

    @Test
    public void reportSingleCrash() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // report a single crash
        final String errorName = "error name";
        final String reason = "error reason";
        final String stacktrace = "the stacktrace causing the error";
        session.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(1)).reportCrash(errorName, reason, stacktrace);
    }

    @Test
    public void reportMultipleCrashes() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // report multiple crashes
        final String errorName1 = "error name 1";
        final String reason1 = "error reason 1";
        final String stacktrace1 = "the stacktrace causing the error 1";
        session.reportCrash(errorName1, reason1, stacktrace1);
        final String errorName2 = "error name 2";
        final String reason2 = "error reason 2";
        final String stacktrace2 = "the stacktrace causing the error 2";
        session.reportCrash(errorName2, reason2, stacktrace2);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(1)).reportCrash(errorName1, reason1, stacktrace1);
        verify(beacon, times(1)).reportCrash(errorName2, reason2, stacktrace2);
    }

    @Test
    public void reportSameCrash() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // report the same cache twice
        final String errorName = "error name";
        final String reason = "error reason";
        final String stacktrace = "the stacktrace causing the error";
        session.reportCrash(errorName, reason, stacktrace);
        session.reportCrash(errorName, reason, stacktrace);

        // verify the correct methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(2)).reportCrash(errorName, reason, stacktrace);
    }

    @Test
    public void endSession() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // end the session
        session.end();

        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(1)).getCurrentTimestamp();
        verify(beacon, times(1)).endSession(session);
        verify(beaconSender, times(1)).finishSession(session);
        assertThat(session.getEndTime(), not(-1L));
    }

    @Test
    public void endSessionTwice() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // end the session twice
        session.end();
        session.end();

        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(2)).getCurrentTimestamp();
        verify(beacon, times(1)).endSession(session);
        verify(beaconSender, times(1)).finishSession(session);
        assertThat(session.getEndTime(), not(-1L));
    }

    @Test
    public void endSessionWithOpenRootActions() {
		// mock a valid status response via the HTTPConnector to be sure the beacon cache is empty
		final HTTPConnector httpClient = mock(HTTPConnector.class);
		final StatusResponse statusResponse = new StatusResponse("", 200);
		when(httpClient.sendBeaconRequest(isA(String.class), any(byte[].class))).thenReturn(statusResponse);
		final ConnectorProvider clientProvider = mock(ConnectorProvider.class);
		when(clientProvider.createClient(any(HTTPClientConfiguration.class))).thenReturn(httpClient);

    	final Beacon beacon = createBeacon(clientProvider);

        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // end the session containing open (=not left) actions
        session.enterAction("Some action 1");
        session.enterAction("Some action 2");
        session.end();



        session.sendBeacon();
        // verify that the actions if the action is still active, it is not in the beacon cache (thus cache is empty)
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void sendBeacon() {
        // create test environment
        final Beacon beacon = mock(Beacon.class);
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        session.sendBeacon();

        // verify the proper methods being called
        verify(beaconSender, times(1)).startSession(session);
        verify(beacon, times(1)).send();
    }

    @Test
    public void clearCapturedData() {
        // create test environment
        final SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // end the session containing closed actions (moved to the beacon cache)
        final RootAction rootAction1 = session.enterAction("Some action 1");
        rootAction1.leaveAction();
        final RootAction rootAction2 = session.enterAction("Some action 2");
        rootAction2.leaveAction();
        // verify that the actions are closed, thus moved to the beacon cache (thus the cache is no longer empty)
        assertThat(session.isEmpty(), is(false));

        // clear the captured data
        session.clearCapturedData();

        // verify that the cached items are cleared and the cache is empty
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void aNewlyConstructedSessionIsNotEnded() {

        // given
        SessionImpl session = new SessionImpl(logger, beaconSender, beacon);

        // when, then
        assertThat(session.isSessionEnded(), is(false));
    }

    @Test
    public void aSessionIsEndedIfEndIsCalled() {

        // given
        SessionImpl session = new SessionImpl(logger, beaconSender,  beacon);

        // when end is called
        session.end();

        // then the session is ended
        assertThat(session.isSessionEnded(), is(true));
    }

    @Test
    public void enterActionGivesNullRootActionIfSessionIsAlreadyEnded() {

        // given
        SessionImpl session = new SessionImpl(logger, beaconSender, beacon);
        session.end();

        // when entering an action on already ended session
        RootAction obtained = session.enterAction("Test");

        // then
        assertThat(obtained, is(notNullValue()));
        assertThat(obtained, is(instanceOf(NullRootAction.class)));
    }

    @Test
    public void identifyUserDoesNothingIfSessionIsEnded() {

        // given
        SessionImpl session = new SessionImpl(logger, beaconSender, beacon);
        session.end();
        beacon.clearData();

        // when trying to identify a user on an ended session
        session.identifyUser("Jane Doe");

        // then
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void reportCrashDoesNothingIfSessionIsEnded() {

        // given
        SessionImpl session = new SessionImpl(logger, beaconSender, beacon);
        session.end();
        beacon.clearData();

        // when trying to identify a user on an ended session
        session.reportCrash("errorName", "reason", "stacktrace");

        // then
        assertThat(session.isEmpty(), is(true));
    }

    @Test
    public void closeSessionEndsTheSession() throws IOException {

        // given
        Closeable target = new SessionImpl(logger, beaconSender, beacon);
        beacon.clearData();

        // when
        target.close();

        // then
        assertThat(beacon.isEmpty(), is(false));
        verify(beaconSender, times(1)).finishSession((SessionImpl)target);
    }
}
