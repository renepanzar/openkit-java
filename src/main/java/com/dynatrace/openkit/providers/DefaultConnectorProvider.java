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

package com.dynatrace.openkit.providers;

import com.dynatrace.openkit.api.Logger;
import com.dynatrace.openkit.core.configuration.HTTPClientConfiguration;
import com.dynatrace.openkit.protocol.HTTPConnector;

/**
 * Implementation of an ConnectorProvider which creates a HTTP client for executing status check, beacon send and time sync requests.
 */
public class DefaultConnectorProvider implements ConnectorProvider {

    private final Logger logger;

    public DefaultConnectorProvider(Logger logger) {
        this.logger = logger;
    }

    @Override
    public HTTPConnector createConnector(Object configuration) {
        return new HTTPConnector(logger, (HTTPClientConfiguration)configuration);
    }

}
