/*
 * *******************************************************************************
 *  COPYRIGHT (c) 2024 Harman International Industries, Inc               *
 *                                                                               *
 *  All rights reserved                                                          *
 *                                                                               *
 *  This software embodies materials and concepts which are                      *
 *  confidential to Harman International Industries, Inc. and is                 *
 *  made available solely pursuant to the terms of a written license             *
 *  agreement with Harman International Industries, Inc.                         *
 *                                                                               *
 *  Designed and Developed by Harman International Industries, Inc.              *
 * ------------------------------------------------------------------------------*
 *  MODULE OR UNIT: ignite-cache                                                 *
 * *******************************************************************************
 */

package com.harman.ignite.cache;

/**
 * Represents the options for putting a key value string to cache.
 *
 * @author ssasidharan
 */
public class DeleteEntryRequest {
    /**
     * Mandatory attribute. The key to associate with the value.
     */
    private String key;

    /**
     * Optional attribute.
     * The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     */
    private String mutationId;

    /** The namespace enabled. */
    private boolean namespaceEnabled;

    /**
     * Instantiates DeleteEntryRequest.
     */
    public DeleteEntryRequest() {
        this.namespaceEnabled = true;
    }

    /**
     * With key.
     *
     * @param key the key
     * @return DeleteEntryRequest
     */
    public DeleteEntryRequest withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * The identifier that will be returned when asynchronous operations complete.
     * Async operations are executed in a pipeline,
     * and this value will be returned when the pipeline has been executed successfully.
     *
     * @param mutationId
     *         - null is valid.
     * @return DeleteEntryRequest
     */
    public DeleteEntryRequest withMutationId(String mutationId) {
        this.mutationId = mutationId;
        return this;
    }

    /**
     * With namespace enabled.
     *
     * @param namespaceEnabled the namespace enabled
     * @return DeleteEntryRequest
     */
    public DeleteEntryRequest withNamespaceEnabled(boolean namespaceEnabled) {
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