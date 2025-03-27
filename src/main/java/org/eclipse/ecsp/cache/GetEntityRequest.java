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
 * Represents the options for getting an entity from the cache.
 *
 * @author ssasidharan
 */
public class GetEntityRequest {
    /**
     * Mandatory attribute.
     */
    private String key;

    /** The namespace enabled. */
    private boolean namespaceEnabled;

    /**
     * Instantiates GetEntityRequest.
     */
    public GetEntityRequest() {
        this.namespaceEnabled = true;
    }

    /**
     * With key.
     *
     * @param key the key
     * @return GetEntityRequest
     */
    public GetEntityRequest withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * With namespace enabled.
     *
     * @param namespaceEnabled the namespace enabled
     * @return GetEntityRequest
     */
    public GetEntityRequest withNamespaceEnabled(boolean namespaceEnabled) {
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
     * Gets the namespace enabled.
     *
     * @return the namespace enabled
     */
    public boolean getNamespaceEnabled() {
        return namespaceEnabled;
    }
}
