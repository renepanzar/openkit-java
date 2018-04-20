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
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.OpenKitConstants;
import com.dynatrace.openkit.api.SSLTrustManager;
import com.dynatrace.openkit.core.OpenKitImpl;
import com.dynatrace.openkit.core.configuration.BeaconCacheConfiguration;
import com.dynatrace.openkit.core.configuration.Configuration;
import com.dynatrace.openkit.core.util.DefaultLogger;
import com.dynatrace.openkit.protocol.Connector;
import com.dynatrace.openkit.protocol.ssl.SSLStrictTrustManager;

/**
 * Abstract base class for concrete builder. Using the builder a OpenKit instance can be created
 */
public abstract class AbstractOpenKitBuilder {

    // immutable fields
    private final String endpointURL;
    private final long deviceID;

    // mutable fields
    private Logger logger;
    private SSLTrustManager trustManager = new SSLStrictTrustManager();
    private boolean verbose;
    private String operatingSystem = OpenKitConstants.DEFAULT_OPERATING_SYSTEM;
    private String manufacturer = OpenKitConstants.DEFAULT_MANUFACTURER;
    private String modelID = OpenKitConstants.DEFAULT_MODEL_ID;
    private String applicationVersion = OpenKitConstants.DEFAULT_APPLICATION_VERSION;
    private long beaconCacheMaxRecordAge = BeaconCacheConfiguration.DEFAULT_MAX_RECORD_AGE_IN_MILLIS;
    private long beaconCacheLowerMemoryBoundary = BeaconCacheConfiguration.DEFAULT_LOWER_MEMORY_BOUNDARY_IN_BYTES;
    private long beaconCacheUpperMemoryBoundary = BeaconCacheConfiguration.DEFAULT_UPPER_MEMORY_BOUNDARY_IN_BYTES;

    private Connector connector;

    /**
     * Creates a new instance of type AbstractOpenKitBuilder
     *
     * @param endpointURL endpoint OpenKit connects to
     * @param deviceID    unique device id
     */
    AbstractOpenKitBuilder(String endpointURL, long deviceID) {
        this.endpointURL = endpointURL;
        this.deviceID = deviceID;
    }

    // ** public methods **

    /**
     * Enables verbose mode. Verbose mode is only enabled if the the default logger is used.
     * If a custom logger is provided by calling  {@code withLogger} debug and info log output
     * depends on the values returned by {@code isDebugEnabled} and {@code isInfoEnabled}.
     *
     * @return {@code this}
     */
    public AbstractOpenKitBuilder enableVerbose() {
        verbose = true;
        return this;
    }

    /**
     * Sets the logger. If no logger is set the default console logger is used. For the default
     * logger verbose mode is enabled by calling {@code enableVerbose}.
     *
     * @param logger the logger
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Defines the version of the application. The value is only set if it is neither null nor empty.
     *
     * @param applicationVersion the application version
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withApplicationVersion(String applicationVersion) {
        if (applicationVersion != null && !applicationVersion.isEmpty()) {
            this.applicationVersion = applicationVersion;
        }
        return this;
    }

    /**
     * Sets the trust manager. Overrides the default trust manager which is {@code SSLStrictTrustmanager} by default-
     *
     * @param trustManager trust manager implementation
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withTrustManager(SSLTrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    /**
     * Sets the operating system information. The value is only set if it is neither null nor empty.
     *
     * @param operatingSystem the operating system
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withOperatingSystem(String operatingSystem) {
        if (operatingSystem != null && !operatingSystem.isEmpty()) {
            this.operatingSystem = operatingSystem;
        }
        return this;
    }

    /**
     * Sets the manufacturer information. The value is only set if it is neither null nor empty.
     *
     * @param manufacturer the manufacturer
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withManufacturer(String manufacturer) {
        if (manufacturer != null && !manufacturer.isEmpty()) {
            this.manufacturer = manufacturer;
        }
        return this;
    }

    /**
     * Sets the model id. The value is only set if it is neither null nor empty.
     *
     * @param modelID the model id
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withModelID(String modelID) {
        if (modelID != null && !modelID.isEmpty()) {
            this.modelID = modelID;
        }
        return this;
    }

    /**
     * Sets the maximum beacon record age of beacon data in cache.
     *
     * @param maxRecordAgeInMilliseconds The maximum beacon record age in milliseconds, or unbounded if negative.
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withBeaconCacheMaxRecordAge(long maxRecordAgeInMilliseconds) {
        this.beaconCacheMaxRecordAge = maxRecordAgeInMilliseconds;
        return this;
    }

    /**
     * Sets the lower memory boundary of the beacon cache.
     *
     * <p>
     * When this is set to a positive value the memory based eviction strategy clears the collected data,
     * until the data size in the cache falls below the configured limit.
     * </p>
     *
     * @param lowerMemoryBoundaryInBytes The lower boundary of the beacon cache or negative if unlimited.
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withBeaconCacheLowerMemoryBoundary(long lowerMemoryBoundaryInBytes) {
        this.beaconCacheLowerMemoryBoundary = lowerMemoryBoundaryInBytes;
        return this;
    }

    /**
     * Sets the upper memory boundary of the beacon cache.
     *
     * <p>
     * When this is set to a positive value the memory based eviction strategy starts to clear
     * data from the beacon cache when the cache size exceeds this setting.
     * </p>
     *
     * @param upperMemoryBoundaryInBytes The lower boundary of the beacon cache or negative if unlimited.
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withBeaconCacheUpperMemoryBoundary(long upperMemoryBoundaryInBytes) {
        this.beaconCacheUpperMemoryBoundary = upperMemoryBoundaryInBytes;
        return this;
    }

    /**
     * Sets the connector used for the communication
     * @param connector Connector to use
     * @return {@code this}
     */
    public AbstractOpenKitBuilder withConnector(Connector connector){
        this.connector = connector;
        return this;
    }

    /**
     * Builds the configuration for the OpenKit instance
     *
     * @return
     */
    abstract Configuration buildConfiguration(Connector connector);

    /**
     * Builds a new {@code OpenKit} instance
     *
     * @return retursn an {@code OpenKit} instance
     */
    public OpenKit build() {
        // create and initialize OpenKit instance
        OpenKitImpl openKit = new OpenKitImpl(getLogger(), buildConfiguration(connector));
        openKit.initialize();

        return openKit;
    }

    // ** internal getter **

    String getApplicationVersion() {
        return applicationVersion;
    }

    String getOperatingSystem() {
        return operatingSystem;
    }

    String getManufacturer() {
        return manufacturer;
    }

    String getModelID() {
        return modelID;
    }

    String getEndpointURL() {
        return endpointURL;
    }

    long getDeviceID() {
        return deviceID;
    }

    SSLTrustManager getTrustManager() {
        return trustManager;
    }

    long getBeaconCacheMaxRecordAge() {
        return beaconCacheMaxRecordAge;
    }

    long getBeaconCacheLowerMemoryBoundary() {
        return beaconCacheLowerMemoryBoundary;
    }

    long getBeaconCacheUpperMemoryBoundary() {
        return beaconCacheUpperMemoryBoundary;
    }

    Logger getLogger() {
        if (logger != null) {
            return logger;
        }

        return new DefaultLogger(verbose);
    }

    Connector getConnector(){
            return connector;
    }
}
