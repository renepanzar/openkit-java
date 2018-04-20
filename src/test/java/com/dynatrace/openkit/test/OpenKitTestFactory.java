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

package com.dynatrace.openkit.test;

import com.dynatrace.openkit.core.Device;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.configuration.OpenKitType;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.HTTPConnector;
import com.dynatrace.openkit.protocol.JsonSerializer;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import com.dynatrace.openkit.test.providers.TestSessionIDProvider;

public class OpenKitTestFactory {

    /**
     * Default constructor is set to private for not allowing to instantiate this class and hiding the constructor from javadoc.
     */
    private OpenKitTestFactory() {
    }

    public static OpenKitTestImpl createAppMonLocalInstance(String applicationName, TestConfiguration testConfiguration) {

        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getAppMonConfig(applicationName, testConfiguration), false);
        applyTestConfiguration(openKitTestImpl, testConfiguration);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    public static OpenKitTestImpl createAppMonRemoteInstance(String applicationName, String deviceID) {
        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getAppMonConfig(applicationName, deviceID), true);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    public static OpenKitTestImpl createDynatraceLocalInstance(String applicationName, String applicationID, TestConfiguration testConfiguration) {
        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getDynatraceConfig(applicationName, testConfiguration
            .getDeviceID()), false);
        applyTestConfiguration(openKitTestImpl, testConfiguration);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    public static OpenKitTestImpl createDynatraceRemoteInstance(String applicationName, String applicationID, String deviceID) {
        OpenKitTestImpl openKitTestImpl = new OpenKitTestImpl(new DefaultLogger(true), getDynatraceConfig(applicationName, deviceID), true);
        openKitTestImpl.initialize();
        return openKitTestImpl;
    }

    private static void applyTestConfiguration(OpenKitTestImpl openKitTestImpl, TestConfiguration testConfiguration) {
        if (testConfiguration == null) {
            return;
        }
        if (testConfiguration.getStatusResponse() != null) {
            openKitTestImpl.setStatusResponse(testConfiguration.getStatusResponse(), testConfiguration.getStatusResponseCode());
        }
        if (testConfiguration.getTimeSyncResponse() != null) {
            openKitTestImpl.setTimeSyncResponse(testConfiguration.getTimeSyncResponse(), testConfiguration.getTimeSyncResponseCode());
        }
    }

    private static Configuration getAppMonConfig(String applicationName, TestConfiguration testConfiguration) {
        return new Configuration(
            OpenKitType.APPMON,
            applicationName,
            testConfiguration.getDeviceID(),
            new TestSessionIDProvider(),
            new SSLStrictTrustManager(),
            testConfiguration.getDevice(),
            testConfiguration.getApplicationVersion(),
            new BeaconCacheConfiguration(-1, -1, -1),
            new HTTPConnector("", "", new JsonSerializer()));
    }

    private static Configuration getAppMonConfig(String applicationName, String deviceID) {
        return new Configuration(
            OpenKitType.APPMON,
            applicationName,
            deviceID,
            new TestSessionIDProvider(),
            new SSLStrictTrustManager(),
            new Device("", "", ""),
            "",
            new BeaconCacheConfiguration(-1, -1, -1),
            new HTTPConnector("", "", new JsonSerializer()));
    }

    private static Configuration getDynatraceConfig(String applicationName, String deviceID) {
        return new Configuration(
            OpenKitType.DYNATRACE,
            applicationName,
            deviceID,
            new TestSessionIDProvider(),
            new SSLStrictTrustManager(),
            new Device("", "", ""),
            "",
            new BeaconCacheConfiguration(-1, -1, -1),
            new HTTPConnector("", "", new JsonSerializer()));
    }
}
