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
 * Captures the options for putting an entity into cache.
 *
 * @author ssasidharan
 * @param <T> Any type that implements IgniteEntity (enforced by IgniteCache contract)
 *
 */
public class PutEntityRequest<T> extends PutEntityBaseRequest<T> {

    /**
     * Optional. If greater than 0 then ttl will be applied
     */
    private long ttlMs = -1L;

    /**
     * Optional. If value is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in
     * cache should match the value here.
     */
    private T expectedValue;

    /**
     * Instantiates a new put entity request.
     */
    public PutEntityRequest() {
        //default constructor
    }

    /**
     * If greater than 0 then ttl will be applied.
     *
     * @param ttlMs the ttl ms
     * @return the put entity request
     */
    public PutEntityRequest<T> withTtlMs(long ttlMs) {
        this.ttlMs = ttlMs;
        return this;
    }

    /**
     * If value is non-null then put becomes a compare and set operation
     * ie, put will be applied only if the existing value in cache should
     * match the value here.
     *
     * @param expectedValue - null is valid. If non-null then put becomes a compare and set operation
     * @return this
     */
    public PutEntityRequest<T> ifCurrentMatches(T expectedValue) {
        this.expectedValue = expectedValue;
        return this;
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
    public T getExpectedValue() {
        return expectedValue;
    }

}
