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

package org.eclipse.ecsp.cache.redis.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.eclipse.ecsp.cache.redis.IgniteCacheRedisImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.redisson.client.RedisException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * This class is used to monitor the health of IgniteCacheRedisImpl.
 */
@Aspect
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HealthMonitorAspect {
    
    /** The Constant LOGGER. */
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(HealthMonitorAspect.class);

    /**
     * Redis health monitor.
     */
    @Pointcut(value = "execution(* org.eclipse.ecsp.cache.redis.IgniteCacheRedisImpl.*(..))")
    private void redisHealthMonitor() {
        //pointcut
    }

    /**
     * This method is used to set the healthy flag to false in case of any
     * exception in IgniteCacheRedisImpl.
     *
     * @param jp        JoinPoint
     * @param exception Throwable
     */
    @AfterThrowing(value = "redisHealthMonitor()", throwing = "exception")
    public void afterThrowingAdvice(JoinPoint jp, Throwable exception)  {
        Object target = jp.getTarget();
        LOGGER.error("Exception occurred in IgniteCacheRedisImpl due to {}.", exception.toString());
        // Checking for only RedisException subclasses for which
        // IgniteCacheRedisImpl
        // healthy will be set to false
        boolean assignableFrom = RedisException.class.isAssignableFrom(exception.getClass());
        if (target instanceof IgniteCacheRedisImpl cacheRedisImpl && assignableFrom) {
            cacheRedisImpl.setHealthy(false);
        }
    }
}
