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

import java.util.Set;

/**
 * Support for getting Map from IgniteCache.
 * Sample DataStructure - Map&lt;String,T&gt;
 *
 * @author avadakkootko
 */
public class GetMapOfEntitiesRequest extends GetEntityRequest {

    /**
     * This attribute is optional.
     * It is used to get specific key values from child map of multimap.
     * To get the entire map parent key is sufficient.
     */
    private Set<String> fields;

    /**
     * Instantiates GetMapOfEntitiesRequest.
     */
    public GetMapOfEntitiesRequest() {
        //default constructor
    }

    /**
     * With fields.
     *
     * @param fields the fields
     * @return GetMapOfEntitiesRequest
     */
    public GetMapOfEntitiesRequest withFields(Set<String> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public Set<String> getFields() {
        return fields;
    }
}
