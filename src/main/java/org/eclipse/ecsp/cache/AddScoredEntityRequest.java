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
 * Represents the options for adding an entity to a scored sorted set.
 *
 * @author ssasidharan
 * @param <T> Any type that implements IgniteEntity (enforced by IgniteCache contract)
 *
 */
public class AddScoredEntityRequest<T> {
    /**
     * Mandatory attribute. The key to associate with the value.
     */
    private String key;
    /**
     * Set entries will be ordered by this value. Mandatory.
     */
    private double score;
    /**
     * The actual value to associate with the score. Mandatory attribute.
     */
    private T value;
    /**
     * Optional attribute. The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    /** The namespace enabled. */
    private boolean namespaceEnabled;

    /**
     * Instantiates AddScoredEntityRequest.
     */
    public AddScoredEntityRequest() {
        this.namespaceEnabled = true;
    }

    /**
     * With key.
     *
     * @param key the key
     * @return AddScoredEntityRequest
     */
    public AddScoredEntityRequest<T> withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * With score.
     *
     * @param score the score
     * @return AddScoredEntityRequest
     */
    public AddScoredEntityRequest<T> withScore(double score) {
        this.score = score;
        return this;
    }

    /**
     * With value.
     *
     * @param value the value
     * @return AddScoredEntityRequest
     */
    public AddScoredEntityRequest<T> withValue(T value) {
        this.value = value;
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
    public AddScoredEntityRequest<T> withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    /**
     * With namespace enabled.
     *
     * @param namespaceEnabled the namespace enabled
     * @return AddScoredEntityRequest
     */
    public AddScoredEntityRequest<T> withNamespaceEnabled(boolean namespaceEnabled) {
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
     * Gets the score.
     *
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public T getValue() {
        return value;
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
