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

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import static org.eclipse.ecsp.cache.redis.RedisConstants.TEN_THOUSAND;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

/**
 * Test case for the {@link EmbeddedRedisServer} class.
 */
public class EmbeddedRedisServerTest {

    /**
     * Test available port.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void testAvailablePort() throws Throwable {
        EmbeddedRedisServer redis1 = new EmbeddedRedisServer();
        redis1.before();
        await().atMost(TEN_THOUSAND.getValue(), TimeUnit.MILLISECONDS);
        EmbeddedRedisServer redis2 = new EmbeddedRedisServer();
        redis2.before();
        Assert.assertNotEquals(redis1.getMappedPort(), redis2.getMappedPort());
        redis1.after();
        redis2.after();
    }
}
