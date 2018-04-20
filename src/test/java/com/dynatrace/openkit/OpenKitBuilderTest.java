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

package com.dynatrace.openkit;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.HTTPConnector;
import com.dynatrace.openkit.protocol.JsonSerializer;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class OpenKitBuilderTest {
    private static final String endpoint = "https://localhost:12345";
    private static final String appID = "asdf123";
    private static final String appName = "myName";
    private static final String deviceID = "1234";
    private static final String appVersion = "1.2.3.4";
    private static final String os = "custom OS";
    private static final String manufacturer = "custom manufacturer";
    private static final String modelID = "custom model id";

    @Test
    public void defaultsAreSetForAppMon() {
        verifyDefaultsAreSet(new AppMonOpenKitBuilder( appName, deviceID).buildConfiguration( new HTTPConnector("", "", new JsonSerializer())));
    }

    @Test
    public void defaultsAreSetForDynatrace() {
        verifyDefaultsAreSet(new DynatraceOpenKitBuilder(deviceID).buildConfiguration( new HTTPConnector("", "", new JsonSerializer())));
    }

    private void verifyDefaultsAreSet(Configuration configuration) {

        // default values
        assertThat(configuration.getApplicationVersion(), is(equalTo(OpenKitConstants.DEFAULT_APPLICATION_VERSION)));
        assertThat(configuration.getDevice().getManufacturer(), is(equalTo(OpenKitConstants.DEFAULT_MANUFACTURER)));
        assertThat(configuration.getDevice()
                                .getOperatingSystem(), is(equalTo(OpenKitConstants.DEFAULT_OPERATING_SYSTEM)));
        assertThat(configuration.getDevice().getModelID(), is(equalTo(OpenKitConstants.DEFAULT_MODEL_ID)));

        // default trust manager
        assertThat(configuration.getHttpClientConfig().getSSLTrustManager(), instanceOf(SSLStrictTrustManager.class));

        // default values for beacon cache configuration
        assertThat(configuration.getBeaconCacheConfiguration(), is(notNullValue()));
        assertThat(configuration.getBeaconCacheConfiguration().getMaxRecordAge(), is(BeaconCacheConfiguration.DEFAULT_MAX_RECORD_AGE_IN_MILLIS));
        assertThat(configuration.getBeaconCacheConfiguration().getCacheSizeUpperBound(), is(BeaconCacheConfiguration.DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES));
        assertThat(configuration.getBeaconCacheConfiguration().getCacheSizeLowerBound(), is(BeaconCacheConfiguration.DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES));
    }

    @Test
    public void applicationNameIsSetCorrectlyForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(appName, deviceID).buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getApplicationName(), is(equalTo(appName)));
    }

    @Test
    public void canOverrideTrustManagerForAppMon() {
        SSLTrustManager trustManager = mock(SSLTrustManager.class);

        Configuration target = new AppMonOpenKitBuilder(appName, deviceID)
            .withTrustManager(trustManager)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void canOverrideTrustManagerForDynatrace() {
        SSLTrustManager trustManager = mock(SSLTrustManager.class);

        Configuration target = new DynatraceOpenKitBuilder(deviceID)
            .withTrustManager(trustManager)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getHttpClientConfig().getSSLTrustManager(), is(sameInstance(trustManager)));
    }

    @Test
    public void canSetApplicationVersionForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(appName, deviceID)
            .withApplicationVersion(appVersion)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetApplicationVersionForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(deviceID)
            .withApplicationVersion(appVersion)
            .buildConfiguration(new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getApplicationVersion(), is(equalTo(appVersion)));
    }

    @Test
    public void canSetOperatingSystemForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(appName, deviceID)
            .withOperatingSystem(os)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetOperatingSystemForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(deviceID)
            .withOperatingSystem(os)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getDevice().getOperatingSystem(), is(equalTo(os)));
    }

    @Test
    public void canSetManufacturerForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(appName, deviceID)
            .withManufacturer(manufacturer)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetManufactureForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder(deviceID)
            .withManufacturer(manufacturer)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getDevice().getManufacturer(), is(equalTo(manufacturer)));
    }

    @Test
    public void canSetModelIDForAppMon() {
        Configuration target = new AppMonOpenKitBuilder(appName, deviceID)
            .withModelID(modelID)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getDevice().getModelID(), is(equalTo(modelID)));
    }

    @Test
    public void canSetModelIDForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder( deviceID)
            .withModelID(modelID)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getDevice().getModelID(), is(equalTo(modelID)));
    }

    @Test
    public void canSetAppNameForDynatrace() {
        Configuration target = new DynatraceOpenKitBuilder( deviceID)
            .withApplicationName(appName)
            .buildConfiguration( new HTTPConnector("", "", new JsonSerializer()));

        assertThat(target.getApplicationName(), is(equalTo(appName)));
    }

    @Test
    public void canSetLogger() {
        // given
        Logger logger = mock(Logger.class);

        // when
        Logger target = new DynatraceOpenKitBuilder( deviceID).withLogger(logger).getLogger();

        // then
        assertThat(target, is(sameInstance(logger)));
    }

    @Test
    public void defaultLoggerIsUsedByDefault() {
        // when
        Logger target = new DynatraceOpenKitBuilder( deviceID).getLogger();

        // then
        assertThat(target, is(instanceOf(DefaultLogger.class)));
        assertThat(target.isDebugEnabled(), is(false));
        assertThat(target.isInfoEnabled(), is(false));
    }

    @Test
    public void verboseIsUsedInDefaultLogger() {
        // when
        Logger target = new DynatraceOpenKitBuilder( deviceID).enableVerbose().getLogger();

        // then
        assertThat(target, is(instanceOf(DefaultLogger.class)));
        assertThat(target.isDebugEnabled(), is(true));
        assertThat(target.isInfoEnabled(), is(true));
    }

    @Test
    public void canSetCustomMaxBeaconRecordAgeForDynatrace() {

        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder( deviceID);
        final long maxRecordAge = 123456L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheMaxRecordAge(maxRecordAge);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(DynatraceOpenKitBuilder.class)));
        assertThat((DynatraceOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheMaxRecordAge(), is(maxRecordAge));
    }

    @Test
    public void canSetCustomMaxBeaconRecordAgeForAppMon() {

        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder( appName, deviceID);
        final long maxRecordAge = 123456L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheMaxRecordAge(maxRecordAge);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(AppMonOpenKitBuilder.class)));
        assertThat((AppMonOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheMaxRecordAge(), is(maxRecordAge));
    }

    @Test
    public void canSetBeaconCacheLowerMemoryBoundaryForDynatrace() {

        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder( deviceID);
        final long lowerMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheLowerMemoryBoundary(lowerMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(DynatraceOpenKitBuilder.class)));
        assertThat((DynatraceOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheLowerMemoryBoundary(), is(lowerMemoryBoundary));
    }

    @Test
    public void canSetBeaconCacheLowerMemoryBoundaryForAppMon() {

        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder( appName, deviceID);
        final long lowerMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheLowerMemoryBoundary(lowerMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(AppMonOpenKitBuilder.class)));
        assertThat((AppMonOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheLowerMemoryBoundary(), is(lowerMemoryBoundary));
    }

    @Test
    public void canSetBeaconCacheUpperMemoryBoundaryForDynatrace() {

        // given
        DynatraceOpenKitBuilder target = new DynatraceOpenKitBuilder( deviceID);
        final long upperMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheUpperMemoryBoundary(upperMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(DynatraceOpenKitBuilder.class)));
        assertThat((DynatraceOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheUpperMemoryBoundary(), is(upperMemoryBoundary));
    }

    @Test
    public void canSetBeaconCacheUpperMemoryBoundaryForAppMon() {

        // given
        AppMonOpenKitBuilder target = new AppMonOpenKitBuilder(  appName, deviceID);
        final long upperMemoryBoundary = 42L * 1024L;

        // when
        AbstractOpenKitBuilder obtained = target.withBeaconCacheUpperMemoryBoundary(upperMemoryBoundary);

        // then
        assertThat(obtained, is(Matchers.<AbstractOpenKitBuilder>instanceOf(AppMonOpenKitBuilder.class)));
        assertThat((AppMonOpenKitBuilder)obtained, is(sameInstance(target)));
        assertThat(target.getBeaconCacheUpperMemoryBoundary(), is(upperMemoryBoundary));
    }
}
