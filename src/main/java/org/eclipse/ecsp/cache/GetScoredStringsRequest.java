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
