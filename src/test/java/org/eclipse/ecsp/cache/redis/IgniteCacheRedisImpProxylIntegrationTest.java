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

import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.redisson.client.WriteRedisConnectionException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.eclipse.ecsp.cache.redis.RedisConstants.TEN_THOUSAND;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

/**
 * IgniteCacheRedisImpl Proxy Integration Test.
 */
public class IgniteCacheRedisImpProxylIntegrationTest {

    /** The Constant LOGGER. */
    private static final IgniteLogger LOGGER =
            IgniteLoggerFactory.getLogger(IgniteCacheRedisImpProxylIntegrationTest.class);

    /** The redis. */
    @ClassRule
    public static EmbeddedRedisServer redis = new EmbeddedRedisServer();

    /** The redisson client. */
    private RedissonClient redissonClient;

    /** The ignite cache. */
    private IgniteCacheRedisImpl igniteCache;

    /**
     * Setup method.
     *
     * @throws Throwable Raised exception
     */
    @Before
    public void setup() throws Throwable {
        redis.before();
        LOGGER.info("Initializing redisson client...");
        Map<String, String> props = getPropertiesMap();
        RedisConfig redisConfig = new RedisConfig();
        igniteCache = new IgniteCacheRedisImpl();
        redissonClient = redisConfig.builder().build(props);
        igniteCache.setRedissonClient(redissonClient);
    }

    /**
     * Stop.
     */
    @After
    public void stop() {
        LOGGER.info("Shutting down Redisson Client...");
        redissonClient.shutdown();
    }

    /**
     * Test put get string with exception.
     */
    @Test()
    public void testPutGetStringWithException() {
        boolean exceptionOccurred = false;
        Assert.assertNotNull(igniteCache);
        redis.after();
        PutStringRequest putStringRequest = new PutStringRequest().withKey("hello").withValue("world");
        try {
            igniteCache.putString(putStringRequest);
        } catch (WriteRedisConnectionException e) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(exceptionOccurred);
        Assert.assertFalse(igniteCache.isHealthy(true));
    }

    /**
     * Test is healthy with exception.
     */
    @Test
    public void testIsHealthyWithException() {
        Assert.assertNotNull(igniteCache);
        redis.after();
        Assert.assertFalse(igniteCache.isHealthy(true));
    }

    /**
     * Test is healthy without exception.
     *
     * @throws InterruptedException the interrupted exception
     */
    @Test
    public void testIsHealthyWithoutException() throws InterruptedException {
        await().atMost(TEN_THOUSAND.getValue(), TimeUnit.MILLISECONDS);
        Assert.assertNotNull(igniteCache);
        Assert.assertTrue(igniteCache.isHealthy(true));
        redis.after();
    }
    
    /**
     * Gets the properties map.
     *
     * @return the properties map
     */
    private Map<String, String> getPropertiesMap() {
        Map<String, String> props = new HashMap<>();

        props.put("redis.address", "127.0.0.1:" + redis.getMappedPort());
        props.put("redis.sentinels", "");
        props.put("redis.master.name", "");
        props.put("redis.dns.monitoring.interval", "5000");
        props.put("redis.read.mode", "SLAVE");
        props.put("redis.subscription.mode", "SLAVE");
        props.put("redis.subscription.conn.min.idle.size", "1");
        props.put("redis.subscription.conn.pool.size", "50");
        props.put("redis.slave.conn.min.idle.size", "32");
        props.put("redis.slave.pool.size", "64");
        props.put("redis.master.conn.min.idle.size", "32");
        props.put("redis.master.conn.pool.size", "64");
        props.put("redis.idle.conn.timeout", "10000");
        props.put("redis.conn.timeout", "10000");
        props.put("redis.timeout", "3000");
        props.put("redis.retry.attempts", "3");
        props.put("redis.retry.interval", "1500");
        props.put("redis.reconnection.timeout", "3000");
        props.put("redis.failed.attempts", "3");
        props.put("redis.database", "0");
        props.put("redis.password", "");
        props.put("redis.subscriptions.per.conn", "5");
        props.put("redis.client.name", "yellow");
        props.put("redis.conn.min.idle.size", "32");
        props.put("redis.conn.pool.size", "64");
        props.put("redis.cluster.masters", "");
        props.put("redis.scan.interval", "1000");
        props.put("redis.scan.limit", "10");
        props.put("redis.regex.scan.filename", "scanregex.txt");
        props.put("redis.netty.threads", "0");
        props.put("redis.decode.in.executor", "false");
        props.put("redis.check.slots.coverage", "false");
        props.put("redis.executor.threads", "32");
        props.put("redis.keep.alive", "true");
        props.put("redis.ping.connection.interval", "60000");
        props.put("redis.tcp.no.delay", "true");
        props.put("redis.transport.mode", "NIO");
        return props;
    }

}
