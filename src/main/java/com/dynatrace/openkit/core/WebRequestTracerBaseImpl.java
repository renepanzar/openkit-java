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

import com.dynatrace.openkit.api.WebRequestTracer;
import com.dynatrace.openkit.protocol.IPayloadGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract base class implementation of the {@link WebRequestTracer} interface.
 */
public abstract class WebRequestTracerBaseImpl implements WebRequestTracer {

    // Dynatrace tag that has to be used for tracing the web request
    private String tag;

    // HTTP information: URL & response code
    protected String url = "<unknown>";
    private int responseCode = -1;
    private int bytesSent = -1;
    private int bytesReceived = -1;

    // start/end time & sequence number
    private long startTime = -1;
    private final AtomicLong endTime = new AtomicLong(-1);
    private int startSequenceNo;
    private int endSequenceNo = -1;

    // Beacon and Action references
    private IPayloadGenerator payloadGenerator;
    private ActionImpl action;

    // *** constructors ***

    WebRequestTracerBaseImpl(IPayloadGenerator payloadGenerator, ActionImpl action) {
        this.payloadGenerator = payloadGenerator;
        this.action = action;

        // creating start sequence number has to be done here, because it's needed for the creation of the tag
        startSequenceNo = payloadGenerator.createSequenceNumber();

        tag = payloadGenerator.createTag(action, startSequenceNo);

        // if start is not called before using the setters the start time (e.g. load time) is not in 1970
        startTime = payloadGenerator.getCurrentTimestamp();
    }

    // *** WebRequestTracer interface methods ***

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public WebRequestTracer setResponseCode(int responseCode) {
        if (!isStopped()) {
            this.responseCode = responseCode;
        }
        return this;
    }

    @Override
    public WebRequestTracer setBytesSent(int bytesSent) {
        if (!isStopped()) {
            this.bytesSent = bytesSent;
        }
        return this;
    }

    @Override
    public WebRequestTracer setBytesReceived(int bytesReceived) {
        if (!isStopped()) {
            this.bytesReceived = bytesReceived;
        }
        return this;
    }

    @Override
    public WebRequestTracer start() {
        if (!isStopped()) {
            startTime = payloadGenerator.getCurrentTimestamp();
        }
        return this;
    }

    @Override
    public void stop() {
        if (!endTime.compareAndSet(-1, payloadGenerator.getCurrentTimestamp())) {
            // stop already called
            return;
        }
        endSequenceNo = payloadGenerator.createSequenceNumber();

        // add web request to payloadGenerator
        payloadGenerator.addWebRequest(action, this);
    }

    @Override
    public void close() {
        stop();
    }

    // *** getter methods ***

    public String getURL() {
        return url;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime.get();
    }

    public int getStartSequenceNo() {
        return startSequenceNo;
    }

    public int getEndSequenceNo() {
        return endSequenceNo;
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    boolean isStopped() {
        return getEndTime() != -1;
    }

}
