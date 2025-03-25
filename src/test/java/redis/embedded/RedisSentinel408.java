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

package redis.embedded;

/**
 * This class is a wrapper for RedisSentinel class.
 */
public class RedisSentinel408 extends AbstractRedisInstance {

    /**
     * Instantiates a new redis sentinel 408.
     *
     * @param actual the actual
     */
    public RedisSentinel408(RedisSentinel actual) {
        super(actual.ports().get(0));
        this.args = actual.args;
    }

    /**
     * Redis ready pattern.
     *
     * @return the string
     */
    @Override
    protected String redisReadyPattern() {
        return ".*Sentinel ID is.*";
    }
}
