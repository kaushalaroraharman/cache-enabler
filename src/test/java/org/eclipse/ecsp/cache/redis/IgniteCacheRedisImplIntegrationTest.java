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

import org.eclipse.ecsp.cache.AddScoredEntityRequest;
import org.eclipse.ecsp.cache.AddScoredStringRequest;
import org.eclipse.ecsp.cache.DeleteEntryRequest;
import org.eclipse.ecsp.cache.DeleteMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.GetEntityRequest;
import org.eclipse.ecsp.cache.GetMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.GetScoredEntitiesRequest;
import org.eclipse.ecsp.cache.GetStringRequest;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutEntityRequest;
import org.eclipse.ecsp.cache.PutMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.cache.redis.IgniteCacheRedisImpl;
import org.eclipse.ecsp.cache.redis.RedisConfig;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.eclipse.ecsp.cache.redis.RedisConstants.FIVE;
import static org.eclipse.ecsp.cache.redis.RedisConstants.HUNDRED;
import static org.eclipse.ecsp.cache.redis.RedisConstants.MINUS_ONE;
import static org.eclipse.ecsp.cache.redis.RedisConstants.ONE_FIFTY;
import static org.eclipse.ecsp.cache.redis.RedisConstants.TEN;
import static org.eclipse.ecsp.cache.redis.RedisConstants.THOUSAND;
import static org.eclipse.ecsp.cache.redis.RedisConstants.THREE;
import static org.eclipse.ecsp.cache.redis.RedisConstants.TWO;
import static org.eclipse.ecsp.cache.redis.RedisConstants.TWO_HUNDRED;

/**
 * Test class for IgniteCacheRedisImpl.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { RedisConfig.class })
@TestPropertySource("/ignite-cache.properties")
@TestExecutionListeners(
        listeners = { ShutdownExecutionListener.class },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class IgniteCacheRedisImplIntegrationTest {

    /** The Constant LOGGER. */
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(IgniteCacheRedisImplIntegrationTest.class);

    /** The redis. */
    @ClassRule
    public static EmbeddedRedisServer redis = new EmbeddedRedisServer();

    /** The redisson client. */
    @Autowired
    private RedissonClient redissonClient;

    /** The ignite cache. */
    @Autowired
    private IgniteCache igniteCache;

    /**
     * Test redis config.
     */
    @Test
    public void testRedisConfig() {
        Assert.assertNotNull(redissonClient);
    }

    /**
     * Test delete entry with namespace enabled.
     */
    @Test
    public void testDeleteEntryWithNamespaceEnabled() {
        Assert.assertNotNull(igniteCache);
        igniteCache.putString(new PutStringRequest().withKey("hello").withValue("world"));
        igniteCache.delete("hello");
        Assert.assertNull(igniteCache.getString("hello"));
        Assert.assertNull(redissonClient.getBucket("namespace:hello").get());
    }

    /**
     * Test delete entryrequest.
     */
    @Test
    public void testDeleteEntryrequest() {
        Assert.assertNotNull(igniteCache);
        igniteCache.putString(new PutStringRequest().withKey("hello").withValue("world"));
        igniteCache.delete(new DeleteEntryRequest().withKey("hello"));
        Assert.assertNull(igniteCache.getString("hello"));
    }
    
    /**
     * Test delete entryrequest with namespace enabled.
     */
    @Test
    public void testDeleteEntryrequestWithNamespaceEnabled() {
        igniteCache.putString(new PutStringRequest().withKey("hello").withValue("world").withNamespaceEnabled(true));
        igniteCache.delete(new DeleteEntryRequest().withKey("hello"));
        Assert.assertNull(redissonClient.getBucket("namespace:hello").get());
    }

    /**
     * Test put get string with namespace enabled.
     */
    @Test
    public void testPutGetStringWithNamespaceEnabled() {
        Assert.assertNotNull(igniteCache);
        igniteCache.putString(new PutStringRequest().withKey("hello").withValue("world").withNamespaceEnabled(true));
        String value = igniteCache.getString("hello");
        Assert.assertEquals("world", value);
        Assert.assertEquals("world", redissonClient.getBucket("namespace:hello").get());
    }

    /**
     * Test put get entity with namespace enabled.
     */
    @Test
    public void testPutGetEntityWithNamespaceEnabled() {
        Assert.assertNotNull(igniteCache);
        IgniteCacheIntegTestEntity entityPut = new IgniteCacheIntegTestEntity();
        entityPut.setId("1000");
        PutEntityRequest<IgniteCacheIntegTestEntity> req = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req.withKey("hello").withValue(entityPut);
        igniteCache.putEntity(req);
        IgniteCacheIntegTestEntity entityRead = igniteCache.getEntity("hello");
        Assert.assertEquals(entityPut, entityRead);
        Assert.assertEquals(entityPut, redissonClient.getBucket("namespace:hello").get());
    }

    /**
     * Test put entity if with namespace enabled.
     */
    @Test
    public void testPutEntityIfWithNamespaceEnabled() {
        IgniteCacheIntegTestEntity oldEntity = new IgniteCacheIntegTestEntity();
        oldEntity.setId("1000");
        oldEntity.setValue("old");
        PutEntityRequest<IgniteCacheIntegTestEntity> req = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req.withKey("hello").withValue(oldEntity);
        igniteCache.putEntity(req);
        IgniteCacheIntegTestEntity newEntity = new IgniteCacheIntegTestEntity();
        newEntity.setId("1000");
        newEntity.setValue("new");

        PutEntityRequest<IgniteCacheIntegTestEntity> req2 = new PutEntityRequest<>();
        req2.withKey("hello").withValue(newEntity);
        req2.ifCurrentMatches(oldEntity);
        igniteCache.putEntity(req2);
        IgniteCacheIntegTestEntity entityRead = igniteCache.getEntity("hello");
        Assert.assertEquals(newEntity, entityRead);
        Assert.assertEquals("new", entityRead.getValue());
        Assert.assertEquals(newEntity, redissonClient.getBucket("namespace:hello").get());
    }

    /**
     * Test put entity if failure.
     */
    @Test
    public void testPutEntityIfFailure() {
        IgniteCacheIntegTestEntity oldEntity = new IgniteCacheIntegTestEntity();
        oldEntity.setId("1000");
        oldEntity.setValue("old");
        PutEntityRequest<IgniteCacheIntegTestEntity> req = new PutEntityRequest<>();
        req.withKey("hello").withValue(oldEntity);
        igniteCache.putEntity(req);
        IgniteCacheIntegTestEntity newEntity = new IgniteCacheIntegTestEntity();
        newEntity.setId("1000");
        newEntity.setValue("new");
        oldEntity.setId("1001");

        PutEntityRequest<IgniteCacheIntegTestEntity> req2 = new PutEntityRequest<>();
        req2.withKey("hello").withValue(newEntity);
        req2.ifCurrentMatches(oldEntity);
        igniteCache.putEntity(req2);
        IgniteCacheIntegTestEntity entityRead = igniteCache.getEntity("hello");
        Assert.assertEquals("1000", entityRead.getId());
        Assert.assertEquals("old", entityRead.getValue());
    }

    /**
     * Test entity scored sorted set.
     */
    @Test
    public void testEntityScoredSortedSet() {
        IgniteCacheIntegTestEntity entity1 = new IgniteCacheIntegTestEntity();
        entity1.setId("1000");
        entity1.setOrder(HUNDRED.getValue());
        igniteCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                        .withKey("helloScoredNonBatched")
                        .withScore(entity1.getOrder()).withValue(entity1));
        IgniteCacheIntegTestEntity entity2 = new IgniteCacheIntegTestEntity();
        entity2.setId("1001");
        entity2.setOrder(TWO_HUNDRED.getValue());
        igniteCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                        .withKey("helloScored")
                        .withScore(entity2.getOrder()).withValue(entity2));
        IgniteCacheIntegTestEntity entity3 = new IgniteCacheIntegTestEntity();
        entity3.setId("1002");
        entity3.setOrder(ONE_FIFTY.getValue());
        igniteCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                        .withKey("helloScored")
                        .withScore(entity3.getOrder()).withValue(entity3));
        List<IgniteCacheIntegTestEntity> entitiesAdded = Arrays.asList(entity1, entity3, entity2);
        List<IgniteCacheIntegTestEntity> entities = igniteCache
                .getEntitiesFromScoredSortedSet(
                        new GetScoredEntitiesRequest()
                                .withKey("helloScored")
                                .withStartIndex(0).withEndIndex(TWO.getValue()));
        Assert.assertEquals(entitiesAdded, entities);
    }

    /**
     * Test entity scored sorted set reversed with namespace enabled.
     */
    @Test
    public void testEntityScoredSortedSetReversedWithNamespaceEnabled() {
        IgniteCacheIntegTestEntity entity1 = new IgniteCacheIntegTestEntity();
        entity1.setId("1000");
        entity1.setOrder(HUNDRED.getValue());
        igniteCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                        .withKey("helloScored")
                        .withScore(entity1.getOrder()).withValue(entity1));
        IgniteCacheIntegTestEntity entity2 = new IgniteCacheIntegTestEntity();
        entity2.setId("1001");
        entity2.setOrder(TWO_HUNDRED.getValue());
        igniteCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                        .withKey("helloScored")
                        .withScore(entity2.getOrder()).withValue(entity2));
        IgniteCacheIntegTestEntity entity3 = new IgniteCacheIntegTestEntity();
        entity3.setId("1002");
        entity3.setOrder(ONE_FIFTY.getValue());
        igniteCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                        .withKey("helloScored")
                        .withScore(entity3.getOrder()).withValue(entity3));
        List<IgniteCacheIntegTestEntity> entitiesAddedReversed = Arrays.asList(entity2, entity3, entity1);
        List<IgniteCacheIntegTestEntity> entitiesReversed = igniteCache.getEntitiesFromScoredSortedSet(
                new GetScoredEntitiesRequest()
                        .withKey("helloScored")
                        .withStartIndex(0)
                        .withEndIndex(TWO.getValue()).fromReverseIndex());
        Assert.assertEquals(entitiesAddedReversed, entitiesReversed);
        Assert.assertEquals(THREE.getValue(), redissonClient.getScoredSortedSet("namespace:helloScored").size());
    }

    // This test verifies an exception and corresponding message the cache
    // implementation depends on to detect an erroneous situation when using
    /**
     * Test batch concurrent execution exception.
     */
    // batch in a multi-threaded scenario.
    @Test
    public void testBatchConcurrentExecutionException() {
        RBatch b = redissonClient.createBatch();
        b.getBucket("concExec").setAsync("should not fail");
        b.execute();
        try {
            Assert.assertTrue("Should not be an exception",
                    b.getBucket("concExec").setAsync("should not fail again") instanceof RFuture);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(e instanceof IllegalStateException
                    || e instanceof NullPointerException);
        }
    }

    /**
     * Test add scored sorted entity batched with namespace enabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testAddScoredSortedEntityBatchedWithNamespaceEnabled() throws InterruptedException, ExecutionException {
        ((IgniteCacheRedisImpl) igniteCache).setBatchSize((int) FIVE.getValue());
        List<Future<String>> futures = new ArrayList<>();
        // test 2 batches at least
        for (int iter = 1; iter <= TWO.getValue(); iter++) {
            for (int i = 1; i <= FIVE.getValue(); i++) {
                IgniteCacheIntegTestEntity entity1 = new IgniteCacheIntegTestEntity();
                entity1.setId(String.valueOf(iter * i)); // id cannot be same
                // for
                // same score. so
                // changing
                // id by iteration
                entity1.setOrder(i);
                Future<String> f = igniteCache.addEntityToScoredSortedSetAsync(
                        new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                                .withKey("helloScoredBatched")
                                .withScore(entity1.getOrder())
                                .withValue(entity1).withMutationId(String.valueOf(i)));
                futures.add(f);
                if (i != FIVE.getValue()) {
                    // because batch size is 5, we don't expect to get completed
                    // futures till 5 items are added
                    assertFuture(f, false, null);
                }
            }
            // at this point, the batch would have executed and the
            // futures should be completed because batch size is 5
            int assertedCount = 0;
            int size = futures.size();
            for (int k = 0; k < size; k++) {
                if (assertFuture(futures.get(k), true, String.valueOf(k + 1))) {
                    assertedCount++;
                }
            }
            Assert.assertEquals(FIVE.getValue(), assertedCount);
            futures.clear();
            List<IgniteCacheIntegTestEntity> entities = igniteCache.getEntitiesFromScoredSortedSet(
                    new GetScoredEntitiesRequest()
                            .withKey("helloScoredBatched")
                            .withStartIndex(0).withEndIndex(FIVE.getValue()));
            Assert.assertFalse(entities.isEmpty());
        }
    }

    /**
     * Test get key value with key regex with namespace provided.
     */
    @Test
    public void testGetKeyValueWithKeyRegexWithNamespaceProvided() {
        PutEntityRequest<IgniteCacheIntegTestEntity> req1 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req1.withKey("TESTKEY1");
        IgniteCacheIntegTestEntity value1 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        req1.withValue(value1);
        PutEntityRequest<IgniteCacheIntegTestEntity> req2 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req2.withKey("TESTKEY2");
        IgniteCacheIntegTestEntity value2 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        req2.withValue(value2);
        PutEntityRequest<IgniteCacheIntegTestEntity> req3 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req3.withKey("KEY2");
        IgniteCacheIntegTestEntity value3 = new IgniteCacheIntegTestEntity("id323", "value323", TEN.getValue());
        req3.withValue(value3);
        igniteCache.putEntity(req1);
        igniteCache.putEntity(req2);
        igniteCache.putEntity(req3);
        Map<String, IgniteEntity> kv;
        kv = igniteCache.getKeyValuePairsForRegex("TESTKEY*", Optional.of(Boolean.TRUE));
        Assert.assertEquals(TWO.getValue(), kv.size());
        Assert.assertTrue(kv.containsKey("namespace:TESTKEY1"));
        Assert.assertTrue(kv.containsKey("namespace:TESTKEY2"));
        Assert.assertEquals(value1, kv.get("namespace:TESTKEY1"));
        Assert.assertEquals(value2, kv.get("namespace:TESTKEY2"));

        kv = igniteCache.getKeyValuePairsForRegex("KEY*", Optional.of(Boolean.TRUE));
        Assert.assertEquals(1, kv.size());
        Assert.assertTrue(kv.containsKey("namespace:KEY2"));
        Assert.assertEquals(value3, kv.get("namespace:KEY2"));

        kv = igniteCache.getKeyValuePairsForRegex("KEY*", Optional.empty());
        Assert.assertEquals(1, kv.size());
        Assert.assertTrue(kv.containsKey("namespace:KEY2"));
        Assert.assertEquals(value3, kv.get("namespace:KEY2"));

        kv = igniteCache.getKeyValuePairsForRegex("KEY*", Optional.empty());
        Assert.assertEquals(1, kv.size());
        Assert.assertTrue(kv.containsKey("namespace:KEY2"));
        Assert.assertEquals(value3, kv.get("namespace:KEY2"));
    }

    /**
     * Test get key value with key regex with namespace disabled.
     */
    @Test
    public void testGetKeyValueWithKeyRegexWithNamespaceDisabled() {
        PutEntityRequest<IgniteCacheIntegTestEntity> req1 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req1.withKey("TESTKEY1");
        IgniteCacheIntegTestEntity value1 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        req1.withValue(value1).withNamespaceEnabled(false);
        PutEntityRequest<IgniteCacheIntegTestEntity> req2 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req2.withKey("TESTKEY2");
        IgniteCacheIntegTestEntity value2 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        req2.withValue(value2).withNamespaceEnabled(false);
        PutEntityRequest<IgniteCacheIntegTestEntity> req3 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req3.withKey("KEY2");
        IgniteCacheIntegTestEntity value3 = new IgniteCacheIntegTestEntity("id323", "value323", TEN.getValue());
        req3.withValue(value3).withNamespaceEnabled(false);
        igniteCache.putEntity(req1);
        igniteCache.putEntity(req2);
        igniteCache.putEntity(req3);
        Map<String, IgniteEntity> kv = null;
        kv = igniteCache.getKeyValuePairsForRegex("TESTKEY*", Optional.of(Boolean.FALSE));
        Assert.assertEquals(TWO.getValue(), kv.size());
        Assert.assertTrue(kv.containsKey("TESTKEY1"));
        Assert.assertTrue(kv.containsKey("TESTKEY2"));
        Assert.assertEquals(value1, kv.get("TESTKEY1"));
        Assert.assertEquals(value2, kv.get("TESTKEY2"));

        kv = igniteCache.getKeyValuePairsForRegex("KEY*", Optional.of(Boolean.FALSE));
        Assert.assertEquals(1, kv.size());
        Assert.assertTrue(kv.containsKey("KEY2"));
        Assert.assertEquals(value3, kv.get("KEY2"));
    }

    /**
     * Test get key value with incorrect key regex.
     */
    @Test
    public void testGetKeyValueWithIncorrectKeyRegex() {
        PutEntityRequest<IgniteCacheIntegTestEntity> req1 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req1.withKey("TESTKEY1");
        IgniteCacheIntegTestEntity value1 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        req1.withValue(value1);
        PutEntityRequest<IgniteCacheIntegTestEntity> req2 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req2.withKey("TESTKEY2");
        IgniteCacheIntegTestEntity value2 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        req2.withValue(value2);
        PutEntityRequest<IgniteCacheIntegTestEntity> req3 = new PutEntityRequest<IgniteCacheIntegTestEntity>();
        req3.withKey("KEY2");
        IgniteCacheIntegTestEntity value3 = new IgniteCacheIntegTestEntity("id323", "value323", TEN.getValue());
        req3.withValue(value3);
        igniteCache.putEntity(req1);
        igniteCache.putEntity(req2);
        igniteCache.putEntity(req3);
        Map<String, IgniteEntity> kv = null;
        kv = igniteCache.getKeyValuePairsForRegex("INCORRECT*", Optional.of(Boolean.TRUE));
        Assert.assertEquals(0, kv.size());
    }

    /**
     * Test get key value with namespace disabled in optional.
     */
    @Test
    public void testGetKeyValueWithNamespaceDisabledInOptional() {
        PutEntityRequest<IgniteCacheIntegTestEntity> req1 = new PutEntityRequest<>();
        IgniteCacheIntegTestEntity value1 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        req1.withKey("TESTKEY1").withValue(value1).withNamespaceEnabled(false);
        PutEntityRequest<IgniteCacheIntegTestEntity> req2 = new PutEntityRequest<>();
        IgniteCacheIntegTestEntity value2 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        req2.withKey("TESTKEY2").withValue(value2).withNamespaceEnabled(false);
        igniteCache.putEntity(req1);
        igniteCache.putEntity(req2);
        Map<String, IgniteEntity> kv = null;
        kv = igniteCache.getKeyValuePairsForRegex("TESTKEY2", Optional.of(Boolean.FALSE));
        Assert.assertEquals(1, kv.size());
    }

    /**
     * Test map put request with namespace enabled.
     */
    @Test
    public void testMapPutRequestWithNamespaceEnabled() {
        PutMapOfEntitiesRequest<IgniteCacheIntegTestEntity> mapReq = new PutMapOfEntitiesRequest<>();
        mapReq.withKey("putservice1");
        Map<String, IgniteCacheIntegTestEntity> value = new HashMap<String, IgniteCacheIntegTestEntity>();
        IgniteCacheIntegTestEntity entity1 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        IgniteCacheIntegTestEntity entity2 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        value.put("A", entity1);
        value.put("B", entity2);
        mapReq.withValue(value);
        igniteCache.putMapOfEntities(mapReq);
        RMap<String, IgniteCacheIntegTestEntity> rmap = redissonClient.getMap("namespace:putservice1");
        Assert.assertEquals(TWO.getValue(), rmap.size());
        Assert.assertEquals(entity1, rmap.get("A"));
        Assert.assertEquals(entity2, rmap.get("B"));

        // Replace A with a new value and add a new element C
        value = new HashMap<String, IgniteCacheIntegTestEntity>();
        IgniteCacheIntegTestEntity entity3 = new IgniteCacheIntegTestEntity("id323", "value323", TEN.getValue());
        IgniteCacheIntegTestEntity entity4 = new IgniteCacheIntegTestEntity("id423", "value423", TEN.getValue());
        value.put("A", entity3);
        value.put("C", entity4);
        mapReq = new PutMapOfEntitiesRequest<>();
        mapReq.withKey("putservice1");
        mapReq.withValue(value);
        igniteCache.putMapOfEntities(mapReq);
        rmap = redissonClient.getMap("namespace:putservice1");
        Assert.assertEquals(THREE.getValue(), rmap.size());
        Assert.assertEquals(entity3, rmap.get("A"));
        Assert.assertEquals(entity2, rmap.get("B"));
        Assert.assertEquals(entity4, rmap.get("C"));

    }

    /**
     * Test map get request with namespace enabled.
     */
    @Test
    public void testMapGetRequestWithNamespaceEnabled() {
        PutMapOfEntitiesRequest<IgniteCacheIntegTestEntity> mapReq = new PutMapOfEntitiesRequest<>();
        mapReq.withKey("getservice1");
        Map<String, IgniteCacheIntegTestEntity> value1 = new HashMap<>();
        IgniteCacheIntegTestEntity entity11 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        IgniteCacheIntegTestEntity entity12 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        value1.put("entity11", entity11);
        value1.put("entity12", entity12);
        mapReq.withValue(value1);
        igniteCache.putMapOfEntities(mapReq);

        PutMapOfEntitiesRequest<IgniteCacheIntegTestEntity> mapReq2 = new PutMapOfEntitiesRequest<>();
        mapReq2.withKey("getservice2");
        Map<String, IgniteCacheIntegTestEntity> value2 = new HashMap<String, IgniteCacheIntegTestEntity>();
        IgniteCacheIntegTestEntity entity21 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        IgniteCacheIntegTestEntity entity22 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        value2.put("entity21", entity21);
        value2.put("entity22", entity22);
        mapReq2.withValue(value2);
        igniteCache.putMapOfEntities(mapReq2);

        GetMapOfEntitiesRequest getMapReq1 = new GetMapOfEntitiesRequest();
        getMapReq1.withKey("getservice1");
        Map<String, IgniteCacheIntegTestEntity> valueRec1 = igniteCache.getMapOfEntities(getMapReq1);
        Assert.assertEquals(TWO.getValue(), valueRec1.size());
        Assert.assertEquals(entity11, valueRec1.get("entity11"));
        Assert.assertEquals(entity12, valueRec1.get("entity12"));
        Assert.assertEquals(TWO.getValue(), redissonClient.getMap("namespace:getservice1").size());

        GetMapOfEntitiesRequest getMapReq2 = new GetMapOfEntitiesRequest();
        getMapReq2.withKey("getservice2");
        Map<String, IgniteCacheIntegTestEntity> valueRec2 = igniteCache.getMapOfEntities(getMapReq2);
        Assert.assertEquals(TWO.getValue(), valueRec2.size());
        Assert.assertEquals(entity21, valueRec2.get("entity21"));
        Assert.assertEquals(entity22, valueRec2.get("entity22"));
        Assert.assertEquals(TWO.getValue(), redissonClient.getMap("namespace:getservice2").size());

        // Main Key not present
        GetMapOfEntitiesRequest getMapReq3 = new GetMapOfEntitiesRequest();
        getMapReq3.withKey("getservice3");
        Map<String, IgniteCacheIntegTestEntity> valueRec3 = igniteCache.getMapOfEntities(getMapReq3);
        Assert.assertEquals(0, valueRec3.size());
        Assert.assertEquals(0, redissonClient.getMap("namespace:getservice3").size());
    }

    /**
     * Test map get request with sub keys with namespace enabled.
     */
    @Test
    public void testMapGetRequestWithSubKeysWithNamespaceEnabled() {
        PutMapOfEntitiesRequest<IgniteCacheIntegTestEntity> mapReq = new PutMapOfEntitiesRequest<>();
        mapReq.withKey("getSubService1");
        Map<String, IgniteCacheIntegTestEntity> value1 = new HashMap<String, IgniteCacheIntegTestEntity>();
        IgniteCacheIntegTestEntity entity11 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        IgniteCacheIntegTestEntity entity12 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        value1.put("entity11", entity11);
        value1.put("entity12", entity12);
        mapReq.withValue(value1);
        igniteCache.putMapOfEntities(mapReq);

        GetMapOfEntitiesRequest getMapReq1 = new GetMapOfEntitiesRequest();
        getMapReq1.withKey("getSubService1");
        Set<String> subKeys = new HashSet<String>();
        subKeys.add("entity11");
        getMapReq1.withFields(subKeys);
        Map<String, IgniteCacheIntegTestEntity> valueRec1 = igniteCache.getMapOfEntities(getMapReq1);
        Assert.assertEquals(1, valueRec1.size());
        Assert.assertEquals(entity11, valueRec1.get("entity11"));
        Assert.assertEquals(TWO.getValue(), redissonClient.getMap("namespace:getSubService1").size());

        // subkeys not present
        Set<String> subKeysNotPresent = new HashSet<String>();
        subKeysNotPresent.add("entity100");
        getMapReq1.withFields(subKeysNotPresent);
        Map<String, IgniteCacheIntegTestEntity> valueRec2 = igniteCache.getMapOfEntities(getMapReq1);
        Assert.assertEquals(0, valueRec2.size());
        //Assert.assertEquals(0,redissonClient.getMap("namespace:getSubService1").size());

        // Main key not present
        GetMapOfEntitiesRequest getMapReq2 = new GetMapOfEntitiesRequest();
        getMapReq2.withKey("getSubServiceNotPresent");
        subKeys = new HashSet<String>();
        subKeys.add("entity11");
        getMapReq2.withFields(subKeys);
        Map<String, IgniteCacheIntegTestEntity> valueRec3 = igniteCache.getMapOfEntities(getMapReq1);
        Assert.assertEquals(0, valueRec3.size());
        Assert.assertEquals(0, redissonClient.getMap("namespace:getSubServiceNotPresent").size());
    }

    /**
     * Test map delete request with namespace enabled.
     */
    @Test
    public void testMapDeleteRequestWithNamespaceEnabled() {
        PutMapOfEntitiesRequest<IgniteCacheIntegTestEntity> mapReq = new PutMapOfEntitiesRequest<>();
        mapReq.withKey("delService1");
        Map<String, IgniteCacheIntegTestEntity> value1 = new HashMap<String, IgniteCacheIntegTestEntity>();
        IgniteCacheIntegTestEntity entity11 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        IgniteCacheIntegTestEntity entity12 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        value1.put("entity11", entity11);
        value1.put("entity12", entity12);
        mapReq.withValue(value1);
        igniteCache.putMapOfEntities(mapReq);

        DeleteMapOfEntitiesRequest request = new DeleteMapOfEntitiesRequest();
        request.withKey("delService1");
        igniteCache.deleteMapOfEntities(request);

        GetMapOfEntitiesRequest getMapReq1 = new GetMapOfEntitiesRequest();
        getMapReq1.withKey("delService1");
        Map<String, IgniteCacheIntegTestEntity> valueRec1 = igniteCache.getMapOfEntities(getMapReq1);
        Assert.assertEquals(0, valueRec1.size());
        Assert.assertEquals(0, redissonClient.getMap("namespace:delService1").size());

        // Check if key not present null pointer exception is thrown
        getMapReq1 = new GetMapOfEntitiesRequest();
        getMapReq1.withKey("delServiceNotPresent");
        igniteCache.getMapOfEntities(getMapReq1);

    }

    /**
     * Test map delete request with subkeys with namespace enabled.
     */
    @Test
    public void testMapDeleteRequestWithSubkeysWithNamespaceEnabled() {
        PutMapOfEntitiesRequest<IgniteCacheIntegTestEntity> mapReq = new PutMapOfEntitiesRequest<>();
        mapReq.withKey("delSubService1");
        Map<String, IgniteCacheIntegTestEntity> value1 = new HashMap<String, IgniteCacheIntegTestEntity>();
        IgniteCacheIntegTestEntity entity11 = new IgniteCacheIntegTestEntity("id123", "value123", TEN.getValue());
        IgniteCacheIntegTestEntity entity12 = new IgniteCacheIntegTestEntity("id223", "value223", TEN.getValue());
        value1.put("entity11", entity11);
        value1.put("entity12", entity12);
        mapReq.withValue(value1);
        igniteCache.putMapOfEntities(mapReq);
        Assert.assertEquals(TWO.getValue(), redissonClient.getMap("namespace:delSubService1").size());

        DeleteMapOfEntitiesRequest delRequest = new DeleteMapOfEntitiesRequest();
        delRequest.withKey("delSubService1");
        Set<String> subKeys = new HashSet<String>();
        subKeys.add("entity11");
        delRequest.withFields(subKeys);
        igniteCache.deleteMapOfEntities(delRequest);

        GetMapOfEntitiesRequest getMapReq1 = new GetMapOfEntitiesRequest();
        getMapReq1.withKey("delSubService1");
        Map<String, IgniteCacheIntegTestEntity> valueRec1 = igniteCache.getMapOfEntities(getMapReq1);
        Assert.assertEquals(1, valueRec1.size());
        Assert.assertEquals(1, redissonClient.getMap("namespace:delSubService1").size());

        //Delete something thats not present
        DeleteMapOfEntitiesRequest delRequest2 = new DeleteMapOfEntitiesRequest();
        GetMapOfEntitiesRequest getMapReq2 = new GetMapOfEntitiesRequest();
        delRequest2.withKey("delSubService1");
        getMapReq2.withKey("delSubService1");
        subKeys = new HashSet<String>();
        subKeys.add("entityNotPresent");
        delRequest2.withFields(subKeys);
        igniteCache.deleteMapOfEntities(delRequest2);
        valueRec1 = igniteCache.getMapOfEntities(getMapReq2);
        Assert.assertEquals(1, valueRec1.size());
        Assert.assertEquals(1, redissonClient.getMap("namespace:delSubService1").size());
    }

    /**
     * Test add scored sorted entity batched with null mutation id.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testAddScoredSortedEntityBatchedWithNullMutationId() throws InterruptedException, ExecutionException {
        ((IgniteCacheRedisImpl) igniteCache).setBatchSize(FIVE.getValue());
        List<Future<String>> futures = new ArrayList<>();
        // test 2 batches at least
        for (int iter = 1; iter <= TWO.getValue(); iter++) {
            for (int i = 1; i <= FIVE.getValue(); i++) {
                IgniteCacheIntegTestEntity entity1 = new IgniteCacheIntegTestEntity();
                entity1.setId(String.valueOf(iter * i)); // id cannot be same
                // for
                // same score. so
                // changing
                // id by iteration
                entity1.setOrder(i);
                Future<String> f = igniteCache.addEntityToScoredSortedSetAsync(
                        new AddScoredEntityRequest<IgniteCacheIntegTestEntity>()
                                .withKey("helloScoredBatchedNullMutationIds")
                                .withScore(entity1.getOrder()).withValue(entity1).withMutationId(null));
                futures.add(f);
                if (i != FIVE.getValue()) {
                    // because batch size is 5, we don't expect to get completed
                    // futures till 5 items are added
                    assertFuture(f, false, null);
                }
            }
            // at this point, the batch would have executed and the
            // futures should be completed because batch size is 5
            int assertedCount = 0;
            int size = futures.size();
            for (int k = 0; k < size; k++) {
                if (assertFuture(futures.get(k), true, null)) {
                    assertedCount++;
                }
            }
            Assert.assertEquals(FIVE.getValue(), assertedCount);
            futures.clear();
        }
    }

    /**
     * Assert future.
     *
     * @param f the f
     * @param successful the successful
     * @param mutationId the mutation id
     * @return true, if successful
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    private boolean assertFuture(Future<String> f, boolean successful, String mutationId)
            throws
            InterruptedException,
            ExecutionException {
        try {
            String s = f.get(THOUSAND.getValue(), TimeUnit.MILLISECONDS);
            if (successful) {
                Assert.assertTrue(f.isDone());
                Assert.assertEquals(mutationId, s);
                return true;
            } else {
                Assert.assertNull(s);
            }
        } catch (TimeoutException te) {
            // expected
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted exception");
            throw e;
        } catch (ExecutionException e) {
            LOGGER.error("Execution exception");
            throw e;
        }
        return false;
    }

    /**
     * Test get string request with namespace enabled.
     */
    @Test
    public void testGetStringRequestWithNamespaceEnabled() {
        igniteCache.putString(new PutStringRequest().withKey("hello").withValue("world"));
        Assert.assertEquals("world", igniteCache.getString(new GetStringRequest().withKey("hello")));
        Assert.assertEquals("world", redissonClient.getBucket("namespace:hello").get());
    }

    /**
     * Test get entity request with namespace enabled.
     */
    @Test
    public void testGetEntityRequestWithNamespaceEnabled() {
        PutEntityRequest putEntityRequest = new PutEntityRequest<>();
        IgniteCacheIntegTestEntity value3 = new IgniteCacheIntegTestEntity("id323", "value323", TEN.getValue());
        putEntityRequest.withKey("hello").withValue(value3);
        igniteCache.putEntity(putEntityRequest);
        Assert.assertEquals(value3, igniteCache.getEntity(new GetEntityRequest().withKey("hello")));
        Assert.assertEquals(value3, redissonClient.getBucket("namespace:hello").get());
    }

    /**
     * Test add string to scored sorted set with namespace enabled.
     */
    @Test
    public void testAddStringToScoredSortedSetWithNamespaceEnabled() {
        AddScoredStringRequest addScoredStringRequest = new AddScoredStringRequest();
        addScoredStringRequest.withKey("hi").withValue("world").withScore(1D);
        igniteCache.addStringToScoredSortedSet(addScoredStringRequest);
        Assert.assertEquals("namespace:hi", addScoredStringRequest.getKey());
    }

    /**
     * Test for addStringToScoredSortedSetAsync with namespace enabled.
     */
    public static class IgniteCacheIntegTestEntity implements IgniteEntity, Comparable<IgniteCacheIntegTestEntity> {

        /** The id. */
        private String id;
        
        /** The value. */
        private String value;
        
        /** The order. */
        private int order;

        /** The schema version. */
        private Version schemaVersion;

        /**
         * Constructor.
         *
         * @param id
         *            id
         * @param value
         *            value
         * @param order
         *            order
         */
        public IgniteCacheIntegTestEntity(String id, String value, int order) {
            this.id = id;
            this.value = value;
            this.order = order;
        }

        /**
         * Instantiates a new ignite cache integ test entity.
         */
        public IgniteCacheIntegTestEntity() {
        }

        /**
         * Gets the schema version.
         *
         * @return the schema version
         */
        @Override
        public Version getSchemaVersion() {
            return schemaVersion;
        }

        /**
         * Sets the schema version.
         *
         * @param arg0 the new schema version
         */
        @Override
        public void setSchemaVersion(Version arg0) {
            this.schemaVersion = arg0;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the id.
         *
         * @param id the new id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof IgniteCacheIntegTestEntity)
                    && ((IgniteCacheIntegTestEntity) obj).getId().equals(getId());
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param value the new value
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the order.
         *
         * @return the order
         */
        public int getOrder() {
            return order;
        }

        /**
         * Sets the order.
         *
         * @param order the new order
         */
        public void setOrder(int order) {
            this.order = order;
        }

        /**
         * Compare to.
         *
         * @param o the o
         * @return the int
         */
        @Override
        public int compareTo(IgniteCacheIntegTestEntity o) {
            if (o.getOrder() == order) {
                return 0;
            } else {
                return o.getOrder() > order ? MINUS_ONE.getValue() : 1;
            }
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("");
            stringBuilder.append("IgniteCacheIntegTestEntity [id=" + id + ",").append(" value=")
                     .append(value).append(", order=").append(order).append(", schemaVersion=")
                     .append(schemaVersion).append("]");
            return stringBuilder.toString();
        }
    }
}
