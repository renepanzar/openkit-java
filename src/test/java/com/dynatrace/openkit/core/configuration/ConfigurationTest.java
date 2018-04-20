/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.configuration;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.protocol.HTTPConnector;
import com.dynatrace.openkit.protocol.JsonSerializer;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.test.providers.TestSessionIDProvider;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationTest {

    @Test
    public void aDefaultConstructedConfigurationEnablesCapturing() {

        // given
        TestConfiguration target = new TestConfiguration();

        // then
        assertThat(target.isCapture(), is(true));
    }

    @Test
    public void enableAndDisableCapturing() {

        // given
        TestConfiguration target = new TestConfiguration();

        // when capturing is enabled
        target.enableCapture();

        // then
        assertThat(target.isCapture(), is(true));

        // and when capturing is disabled again
        target.disableCapture();

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void capturingIsDisabledIfStatusResponseIsNull() {

        // given
        TestConfiguration target = new TestConfiguration();
        target.enableCapture();

        // when status response to handle is null
        target.updateSettings(null);

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void capturingIsDisabledIfResponseCodeIndicatesFailures() {

        // given
        TestConfiguration target = new TestConfiguration();
        target.enableCapture();

        StatusResponse response = mock(StatusResponse.class);
        when(response.isCapture()).thenReturn(true);
        when(response.getResponseCode()).thenReturn(400);

        // when status response indicates erroneous response
        target.updateSettings(response);

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void capturingIsEnabledFromStatusResponse() {
        // given
        TestConfiguration target = new TestConfiguration();
        target.disableCapture();

        StatusResponse response = mock(StatusResponse.class);
        when(response.isCapture()).thenReturn(true);
        when(response.getResponseCode()).thenReturn(200);

        // when capturing is enabled in status response
        target.updateSettings(response);

        // then
        assertThat(target.isCapture(), is(true));
    }

    @Test
    public void capturingIsDisabledFromStatusResponse() {
        // given
        TestConfiguration target = new TestConfiguration();
        target.enableCapture();

        StatusResponse response = mock(StatusResponse.class);
        when(response.isCapture()).thenReturn(false);
        when(response.getResponseCode()).thenReturn(200);

        // when capturing is disabled in status response
        target.updateSettings(response);

        // then
        assertThat(target.isCapture(), is(false));
    }

    @Test
    public void getBeaconCacheConfiguration() {

        // given
        BeaconCacheConfiguration beaconCacheConfiguration = mock(BeaconCacheConfiguration.class);
        TestConfiguration target = new TestConfiguration(OpenKitType.DYNATRACE, "", "", 777, "", beaconCacheConfiguration);

        // when
        BeaconCacheConfiguration obtained = target.getBeaconCacheConfiguration();

        // then
        assertThat(obtained, is(sameInstance(beaconCacheConfiguration)));
    }

    private final class TestConfiguration extends Configuration {

        private TestConfiguration() {
            this(OpenKitType.DYNATRACE, "", "", 42, "");
        }

        private TestConfiguration(OpenKitType openKitType, String applicationName, String applicationID, long deviceID, String endpointURL) {
            this(openKitType, applicationName, applicationID, deviceID, endpointURL,
                new BeaconCacheConfiguration(-1, -1, -1));
        }

        private TestConfiguration(OpenKitType openKitType, String applicationName, String applicationID, long deviceID, String endpointURL, BeaconCacheConfiguration beaconCacheConfiguration) {
            super(openKitType, applicationName, applicationID, deviceID, endpointURL,
                new TestSessionIDProvider(), new SSLStrictTrustManager(),
                new Device("", "", ""), "", beaconCacheConfiguration,
                new HTTPConnector("", "", new JsonSerializer()));
        }
    }
}
