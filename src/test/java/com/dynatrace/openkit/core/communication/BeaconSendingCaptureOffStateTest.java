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

import com.dynatrace.openkit.protocol.HTTPClientImpl;
import com.dynatrace.openkit.protocol.StatusResponse;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingCaptureOffStateTest {

    private BeaconSendingContext mockContext;

    @Before
    public void setUp() {
        StatusResponse mockResponse = mock(StatusResponse.class);

        HTTPClientImpl httpClient = mock(HTTPClientImpl.class);
        when(httpClient.sendStatusRequest()).thenReturn(mockResponse);

        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.getHTTPClient()).thenReturn(httpClient);
        when(mockContext.isTimeSynced()).thenReturn(true);
    }

    @Test
    public void aBeaconSendingCaptureOffStateIsNotATerminalState() {
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        //verify that BeaconSendingCaptureOffState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void aBeaconSendingCaptureOffStateHasTerminalStateBeaconSendingFlushSessions() {
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        //verify that terminal state is BeaconSendingFlushSessions
        assertThat(terminalState, is(instanceOf(BeaconSendingFlushSessionsState.class)));
    }

    @Test
    public void aBeaconSendingCaptureOffStateTransitionsToTimeSyncStateWhenNotYetTimeSynched() throws InterruptedException {
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);
        when(mockContext.isTimeSynced()).thenReturn(false);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).disableCapture();

        // also verify that lastStatusCheckTime was updated
        verify(mockContext, times(1)).setLastStatusCheckTime(org.mockito.Matchers.anyLong());
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }

    @Test
    public void aBeaconSendingCaptureOffStateTransitionsToCaptureOnStateWhenCapturingActive() throws InterruptedException {
        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(true);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).disableCapture();

        // also verify that lastStatusCheckTime was updated
        verify(mockContext, times(1)).setLastStatusCheckTime(org.mockito.Matchers.anyLong());
        //verifyNoMoreInteractions(mockContext);
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOnState.class));
    }

    @Test
    public void aBeaconSendingCaptureOffStateWaitsForSpecifiedTimeWhenTimeSyncFails() throws InterruptedException {

        //given
        BeaconSendingCaptureOffState target = new BeaconSendingCaptureOffState();
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);
        when(mockContext.isTimeSynced()).thenReturn(false);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).disableCapture();
        // also verify that lastStatusCheckTime was updated
        verify(mockContext, times(1)).setLastStatusCheckTime(0);
        // verify that the next time sync operation will follow after a sleep of 7200000 ms
        verify(mockContext, times(1)).sleep(7200000);//wait for two hours
        // verify that after sleeping the transition to BeaconSendingTimeSyncState works
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }
}
