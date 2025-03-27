/*
 * *******************************************************************************
 *
 *  Copyright (c) 2023-24 Harman International
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *
 *
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *       
 *
 *  Unless required by applicable law or agreed to in writing, software
 *
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *
 *  limitations under the License.
 *
 *
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  *******************************************************************************
 */

package org.eclipse.ecsp.cache;

/**
 * Represents the options for putting a key value string to cache.
 *
 * @author ssasidharan
 */
public class PutStringRequest {
    /**
     * Mandatory attribute.
     */
    private String key;

    /**
     * Mandatory attribute.
     */
    private String value;

    /**
     * Optional. If greater than 0 then ttl will be applied.
     */
    private long ttlMs = -1L;

    /**
     * Optional. If value is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in
     * cache should match the value here.
     */
    private String expectedValue;

    /**
     * Optional attribute. The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    /** The namespace enabled. */
    private boolean namespaceEnabled;

    /**
     * Instantiates a new put string request.
     */
    public PutStringRequest() {
        this.namespaceEnabled = true;
    }

    /**
     * With key.
     *
     * @param key the key
     * @return the put string request
     */
    public PutStringRequest withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * With value.
     *
     * @param value the value
     * @return the put string request
     */
    public PutStringRequest withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * If greater than 0 then ttl will be applied.
     *
     * @param ttlMs - time to live in milliseconds
     * @return this
     */
    public PutStringRequest withTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
        return this;
    }

    /**
     * If expectedValue is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in cache
     * should match the value here.
     *
     * @param expectedValue - set expected value
     * @return the put string request
     */
    public PutStringRequest ifCurrentMatches(String expectedValue) {
        this.expectedValue = expectedValue;
        return this;
    }

    /**
     * The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     *
     * @param mutationId
     *         - null is valid.
     * @return this
     */
    public PutStringRequest withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    /**
     * With namespace enabled.
     *
     * @param namespaceEnabled the namespace enabled
     * @return the put string request
     */
    public PutStringRequest withNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
        return this;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the ttl ms.
     *
     * @return the ttl ms
     */
    public long getTtlMs() {
        return ttlMs;
    }

    /**
     * Gets the expected value.
     *
     * @return the expected value
     */
    public String getExpectedValue() {
        return expectedValue;
    }

    /**
     * Gets the mutation id.
     *
     * @return the mutation id
     */
    public String getMutationId() {
        return mutationId;
    }

    /**
     * Gets the namespace enabled.
     *
     * @return the namespace enabled
     */
    public boolean getNamespaceEnabled() {
        return namespaceEnabled;
    }

}