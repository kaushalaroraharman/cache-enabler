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

package org.eclipse.ecsp.cache.redis;

/**
 * Constants for Redis.
 */
public enum RedisConstants {
    
    /** The minus one. */
    MINUS_ONE(-1),
    
    /** The two. */
    TWO(2),
    
    /** The three. */
    THREE(3),
    
    /** The five. */
    FIVE(5),
    
    /** The ten. */
    TEN(10),
    
    /** The hundred. */
    HUNDRED(100),
    
    /** The one fifty. */
    ONE_FIFTY(150),
    
    /** The two hundred. */
    TWO_HUNDRED(200),
    
    /** The thousand. */
    THOUSAND(1000),
    
    /** The server port. */
    SERVER_PORT(6379),
    
    /** The sentinel port. */
    SENTINEL_PORT(26379),
    
    /** The ten thousand. */
    TEN_THOUSAND(10000);

    /** The value. */
    private final int value;

    /**
     * Instantiates a new redis constants.
     *
     * @param value the value
     */
    RedisConstants(int value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
