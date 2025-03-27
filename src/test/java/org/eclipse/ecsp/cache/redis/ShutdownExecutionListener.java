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

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.redisson.api.RedissonClient;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This class is used to shut down the redisson client after the test class execution.
 *
 * @see ShutdownExecutionEvent
 */
public class ShutdownExecutionListener extends AbstractTestExecutionListener {

    /** The Constant LOGGER. */
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ShutdownExecutionListener.class);

    /**
     * Before test class.
     *
     * @param testContext the test context
     * @throws Exception the exception
     */
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        // initialize resources
    }

    /**
     * After test class.
     *
     * @param testContext the test context
     * @throws Exception the exception
     */
    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        LOGGER.info("Shutting down redisson client...");
        RedissonClient redissonClient = (RedissonClient) testContext.getApplicationContext().getBean("redissonClient");
        redissonClient.shutdown();
    }
}
