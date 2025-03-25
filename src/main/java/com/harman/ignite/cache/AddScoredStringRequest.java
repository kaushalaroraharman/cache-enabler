/*
 ********************************************************************************
 * COPYRIGHT (c) 2024 Harman International Industries, Inc                      *
 *                                                                              *
 * All rights reserved                                                          *
 *                                                                              *
 * This software embodies materials and concepts which are                      *
 * confidential to Harman International Industries, Inc. and is                 *
 * made available solely pursuant to the terms of a written license             *
 * agreement with Harman International Industries, Inc.                         *
 *                                                                              *
 * Designed and Developed by Harman International Industries, Inc.              *
 *------------------------------------------------------------------------------*
 * MODULE OR UNIT: ignite-cache                                                 *
 ********************************************************************************
 */

package com.harman.ignite.cache;

/**
 * Represents options for adding a string to a scored sorted set.
 *
 * @author ssasidharan
 */
public class AddScoredStringRequest {
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
    private String value;
    /**
     * Optional attribute. The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    /** The namespace enabled. */
    private boolean namespaceEnabled;

    /**
     * Instantiates AddScoredStringRequest.
     */
    public AddScoredStringRequest() {
        this.namespaceEnabled = true;
    }

    /**
     * With key.
     *
     * @param key the key
     * @return AddScoredStringRequest
     */
    public AddScoredStringRequest withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * With score.
     *
     * @param score the score
     * @return AddScoredStringRequest
     */
    public AddScoredStringRequest withScore(double score) {
        this.score = score;
        return this;
    }

    /**
     * With value.
     *
     * @param value the value
     * @return AddScoredStringRequest
     */
    public AddScoredStringRequest withValue(String value) {
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
    public AddScoredStringRequest withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    /**
     * With namespace enabled.
     *
     * @param namespaceEnabled the namespace enabled
     * @return AddScoredStringRequest
     */
    public AddScoredStringRequest withNamespaceEnabled(boolean namespaceEnabled) {
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
    public String getValue() {
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
