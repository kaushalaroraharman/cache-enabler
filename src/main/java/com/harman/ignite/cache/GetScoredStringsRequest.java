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
 * Represents the options to get a range of string entries from a scored sorted set.
 *
 * @author ssasidharan
 */
public class GetScoredStringsRequest {
    /**
     * Mandatory attribute.
     */
    private String key;

    /**
     * Refer redis documentation.
     */
    private int startIndex;

    /**
     * Refer redis documentation.
     */
    private int endIndex;

    /**
     * False by default. Check redis documentation for details (for ex zrevrange)
     */
    private boolean reversed;

    /** The namespace enabled. */
    private boolean namespaceEnabled;

    /**
     * Instantiates GetScoredStringsRequest.
     */
    public GetScoredStringsRequest() {
        this.namespaceEnabled = true;
    }

    /**
     * With key.
     *
     * @param key the key
     * @return GetScoredStringsRequest
     */
    public GetScoredStringsRequest withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * With start index.
     *
     * @param startIndex the start index
     * @return GetScoredStringsRequest
     */
    public GetScoredStringsRequest withStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * With end index.
     *
     * @param endIndex the end index
     * @return GetScoredStringsRequest
     */
    public GetScoredStringsRequest withEndIndex(int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    /**
     * From reverse index.
     *
     * @return GetScoredStringsRequest
     */
    public GetScoredStringsRequest fromReverseIndex() {
        this.reversed = true;
        return this;
    }

    /**
     * With namespace enabled.
     *
     * @param namespaceEnabled the namespace enabled
     * @return GetScoredStringsRequest
     */
    public GetScoredStringsRequest withNamespaceEnabled(boolean namespaceEnabled) {
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
     * Gets the start index.
     *
     * @return the start index
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Gets the end index.
     *
     * @return the end index
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Checks if is reversed.
     *
     * @return true, if is reversed
     */
    public boolean isReversed() {
        return reversed;
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
