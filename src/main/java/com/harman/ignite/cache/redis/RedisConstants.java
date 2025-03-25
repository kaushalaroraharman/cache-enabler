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

package com.harman.ignite.cache.redis;

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
