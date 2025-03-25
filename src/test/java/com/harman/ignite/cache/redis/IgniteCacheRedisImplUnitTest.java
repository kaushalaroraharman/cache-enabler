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

import com.harman.ignite.cache.AddScoredEntityRequest;
import com.harman.ignite.cache.AddScoredStringRequest;
import com.harman.ignite.cache.DeleteEntryRequest;
import com.harman.ignite.cache.DeleteMapOfEntitiesRequest;
import com.harman.ignite.cache.GetEntityRequest;
import com.harman.ignite.cache.GetMapOfEntitiesRequest;
import com.harman.ignite.cache.GetScoredEntitiesRequest;
import com.harman.ignite.cache.GetScoredStringsRequest;
import com.harman.ignite.cache.GetStringRequest;
import com.harman.ignite.cache.PutEntityRequest;
import com.harman.ignite.cache.PutMapOfEntitiesRequest;
import com.harman.ignite.cache.PutStringRequest;
import com.harman.ignite.domain.Version;
import com.harman.ignite.entities.IgniteEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RFuture;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.misc.CompletableFutureWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.harman.ignite.cache.redis.RedisConstants.FIVE;

/**
 * Unit test class for IgniteCacheRedisImpl.
 */
public class IgniteCacheRedisImplUnitTest {

    /** The Constant THOUSAND_LONG. */
    private static final long THOUSAND_LONG = 1000L;
    
    /** The Constant TWO_DOUBLE. */
    private static final double TWO_DOUBLE = 2.0D;

    /**
     * Test get string with namespace not provided.
     */
    @Test
    public void testGetStringWithNamespaceNotProvided() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        redisCache.getString("hello");
        Mockito.verify(rbucket).get();
    }

    /**
     * Test delete entry with namespace not provided.
     */
    @Test
    public void testDeleteEntryWithNamespaceNotProvided() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        redisCache.delete("hello");
        Mockito.verify(rbucket).delete();
    }

    /**
     * Test delete entry request with namespace disabled.
     */
    @Test
    public void testDeleteEntryRequestWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        redisCache.delete(new DeleteEntryRequest().withKey("hello").withNamespaceEnabled(false));
        Mockito.verify(rbucket).delete();
    }

    /**
     * Test delete entry async request with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testDeleteEntryAsyncRequestWithNamespaceDisabled() throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(rbucket.deleteAsync()).thenReturn(new CompletableFutureWrapper(true));
        redisCache.setRedissonClient(redisson);
        Future<String> mutationId = redisCache.deleteAsync(
                new DeleteEntryRequest().withKey("hello").withMutationId("mut001").withNamespaceEnabled(false));
        Assert.assertTrue(mutationId.isDone());
        Assert.assertEquals("mut001", mutationId.get());
        Mockito.verify(rbucket).deleteAsync();
    }

    /**
     * Test get string key value with namespace disabled.
     */
    @Test
    public void testGetStringKeyValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(rbucket.get()).thenReturn("world");
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        String world = redisCache.getString(new GetStringRequest().withKey("hello").withNamespaceEnabled(false));
        Assert.assertEquals("world", world);
    }

    /**
     * Test get string key value with null key with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testGetStringKeyValueWithNullKeyWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.getString(new GetStringRequest().withNamespaceEnabled(false));
    }

    /**
     * Test put string key value with namespace disabled.
     */
    @Test
    public void testPutStringKeyValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        redisCache.putString(new PutStringRequest().withKey("hello").withValue("world").withNamespaceEnabled(false));
        Mockito.verify(rbucket).set("world");
    }

    /**
     * Test put string null key value with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testPutStringNullKeyValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.putString(new PutStringRequest().withNamespaceEnabled(false));
    }

    /**
     * Test put string key value with ttl with namespace disabled.
     */
    @Test
    public void testPutStringKeyValueWithTtlWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        redisCache.putString(
                new PutStringRequest()
                        .withKey("hello")
                        .withValue("world")
                        .withTtlMs(THOUSAND_LONG)
                        .withNamespaceEnabled(false));
        Mockito.verify(rbucket).set("world", THOUSAND_LONG, TimeUnit.MILLISECONDS);
    }

    /**
     * Test put string key value if with namespace disabled.
     */
    @Test
    public void testPutStringKeyValueIfWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        redisCache.putString(
                new PutStringRequest()
                        .withKey("hello")
                        .ifCurrentMatches("")
                        .withValue("world")
                        .withNamespaceEnabled(false));
        Mockito.verify(rbucket).compareAndSet("", "world");
    }

    /**
     * Test get entity with namespace not provided.
     */
    @Test
    public void testGetEntityWithNamespaceNotProvided() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        Mockito.when(rbucket.get()).thenReturn(entity);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        IgniteCacheTestEntity entityRead = redisCache.getEntity("hello");
        Assert.assertEquals(entity, entityRead);
        Mockito.verify(rbucket).get();
    }

    /**
     * Test get entity request with namespace disabled.
     */
    @Test
    public void testGetEntityRequestWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        Mockito.when(rbucket.get()).thenReturn(entity);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        IgniteCacheTestEntity entityRead = redisCache.getEntity(
                new GetEntityRequest()
                        .withKey("hello")
                        .withNamespaceEnabled(false));
        Assert.assertEquals(entity, entityRead);
    }

    /**
     * Test get entity with null key with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testGetEntityWithNullKeyWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.getEntity(new GetEntityRequest().withNamespaceEnabled(false));
    }

    /**
     * Test put entity with namespace disabled.
     */
    @Test
    public void testPutEntityWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<IgniteCacheTestEntity>();
        req.withKey("hello").withValue(entity).withNamespaceEnabled(false);
        redisCache.putEntity(req);
        Mockito.verify(rbucket).set(entity);
    }

    /**
     * Test put entity with null key value with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testPutEntityWithNullKeyValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.putEntity(new PutEntityRequest<IgniteCacheTestEntity>());
    }

    /**
     * Test put entity with ttl with namespace disabled.
     */
    @Test
    public void testPutEntityWithTtlWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<IgniteCacheTestEntity>();
        req.withKey("hello").withValue(entity).withNamespaceEnabled(false);
        req.withTtlMs(THOUSAND_LONG);
        redisCache.putEntity(req);
        Mockito.verify(rbucket).set(entity, THOUSAND_LONG, TimeUnit.MILLISECONDS);
    }

    /**
     * Test put entity if with namespace disabled.
     */
    @Test
    public void testPutEntityIfWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RBucket<Object> rbucket = (RBucket<Object>) Mockito.mock(RBucket.class);
        Mockito.when(redisson.getBucket("hello")).thenReturn(rbucket);
        redisCache.setRedissonClient(redisson);
        IgniteCacheTestEntity newEntity = new IgniteCacheTestEntity();
        IgniteCacheTestEntity oldEntity = new IgniteCacheTestEntity();
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<IgniteCacheTestEntity>();
        req.withKey("hello").withValue(newEntity).withNamespaceEnabled(false);
        req.ifCurrentMatches(oldEntity);
        redisCache.putEntity(req);
        Mockito.verify(rbucket).compareAndSet(oldEntity, newEntity);
    }

    /**
     * Test add string to scored sorted set with namespace disabled.
     */
    @Test
    public void testAddStringToScoredSortedSetWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RScoredSortedSet<Object> rsss = (RScoredSortedSet<Object>) Mockito.mock(RScoredSortedSet.class);
        Mockito.when(redisson.getScoredSortedSet("presidents")).thenReturn(rsss);
        redisCache.setRedissonClient(redisson);
        redisCache.addStringToScoredSortedSet(
                new AddScoredStringRequest()
                        .withKey("presidents")
                        .withScore(1D)
                        .withValue("Abdul Kalam")
                        .withNamespaceEnabled(false));
        Mockito.verify(rsss).add(1D, "Abdul Kalam");
    }

    /**
     * Test add string to scored sorted set with null key with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testAddStringToScoredSortedSetWithNullKeyWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.addStringToScoredSortedSet(new AddScoredStringRequest().withNamespaceEnabled(false));
    }

    /**
     * Test add string to scored sorted set with null value with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testAddStringToScoredSortedSetWithNullValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.addStringToScoredSortedSet(
                new AddScoredStringRequest()
                        .withKey("presidents")
                        .withNamespaceEnabled(false));
    }

    /**
     * Test add string to scored sorted set with null key value with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testAddStringToScoredSortedSetWithNullKeyValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.addStringToScoredSortedSet(new AddScoredStringRequest().withNamespaceEnabled(false));
    }

    /**
     * Test get strings from scored sorted set with namespace disabled.
     */
    @Test
    public void testGetStringsFromScoredSortedSetWithNamespaceDisabled() {
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RScoredSortedSet<Object> rsss = (RScoredSortedSet<Object>) Mockito.mock(RScoredSortedSet.class);
        Mockito.when(redisson.getScoredSortedSet("presidents")).thenReturn(rsss);
        List<ScoredEntry<Object>> presidents = new ArrayList<>();
        String prezi1 = "Abdul Kalam";
        String prezi2 = "S. Radhakrishnan";
        presidents.add(new ScoredEntry<Object>(1D, prezi1));
        presidents.add(new ScoredEntry<Object>(TWO_DOUBLE, prezi2));
        Mockito.when(rsss.entryRange(1, FIVE.getValue())).thenReturn(presidents);
        List<String> expectedPresidentNames = Arrays.asList(prezi1, prezi2);
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setRedissonClient(redisson);

        List<String> presidentNames = redisCache
                .getStringsFromScoredSortedSet(
                        new GetScoredStringsRequest()
                                .withKey("presidents")
                                .withStartIndex(1)
                                .withEndIndex(FIVE.getValue())
                                .withNamespaceEnabled(false));
        Assert.assertEquals(expectedPresidentNames, presidentNames);

    }

    /**
     * Test get strings from scored sorted set with null key with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testGetStringsFromScoredSortedSetWithNullKeyWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        List<String> presidentNames = redisCache
                .getStringsFromScoredSortedSet(new GetScoredStringsRequest().withNamespaceEnabled(false));

    }

    /**
     * Test get strings from scored sorted set reversed with namespace disabled.
     */
    @Test
    public void testGetStringsFromScoredSortedSetReversedWithNamespaceDisabled() {
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RScoredSortedSet<Object> rsss = (RScoredSortedSet<Object>) Mockito.mock(RScoredSortedSet.class);
        Mockito.when(redisson.getScoredSortedSet("presidents")).thenReturn(rsss);
        List<ScoredEntry<Object>> presidents = new ArrayList<>();
        String prezi1 = "Abdul Kalam";
        String prezi2 = "S. Radhakrishnan";
        presidents.add(new ScoredEntry<Object>(1D, prezi2));
        presidents.add(new ScoredEntry<Object>(TWO_DOUBLE, prezi1));
        Mockito.when(rsss.entryRangeReversed(1, FIVE.getValue())).thenReturn(presidents);
        List<String> expectedPresidentNames = Arrays.asList(prezi2, prezi1);
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setRedissonClient(redisson);

        List<String> presidentNames = redisCache
                .getStringsFromScoredSortedSet(
                        new GetScoredStringsRequest()
                                .withKey("presidents")
                                .withStartIndex(1)
                                .withEndIndex(FIVE.getValue())
                                .fromReverseIndex()
                                .withNamespaceEnabled(false));
        Assert.assertEquals(expectedPresidentNames, presidentNames);

    }

    /**
     * Test add entity to scored sorted set with namespace disabled.
     */
    @Test
    public void testAddEntityToScoredSortedSetWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RScoredSortedSet<Object> rsss = (RScoredSortedSet<Object>) Mockito.mock(RScoredSortedSet.class);
        Mockito.when(redisson.getScoredSortedSet("entities")).thenReturn(rsss);
        redisCache.setRedissonClient(redisson);
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        redisCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheTestEntity>().withKey("entities").withScore(1D).withValue(entity)
                        .withNamespaceEnabled(false));
        Mockito.verify(rsss).add(1D, entity);
    }

    /**
     * Test add entity to scored sorted set with null key with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testAddEntityToScoredSortedSetWithNullKeyWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        redisCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheTestEntity>()
                        .withScore(1D)
                        .withValue(entity)
                        .withNamespaceEnabled(false));
    }

    /**
     * Test add entity to scored sorted set with null value with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testAddEntityToScoredSortedSetWithNullValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheTestEntity>()
                        .withKey("entities")
                        .withScore(1D)
                        .withNamespaceEnabled(false));
    }

    /**
     * Test add entity to scored sorted set with null key value with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testAddEntityToScoredSortedSetWithNullKeyValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.addEntityToScoredSortedSet(
                new AddScoredEntityRequest<IgniteCacheTestEntity>()
                        .withScore(1D)
                        .withNamespaceEnabled(false));
    }

    /**
     * Test get entities from scored sorted set with namespace disabled.
     */
    @Test
    public void testGetEntitiesFromScoredSortedSetWithNamespaceDisabled() {
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RScoredSortedSet<Object> rsss = (RScoredSortedSet<Object>) Mockito.mock(RScoredSortedSet.class);
        Mockito.when(redisson.getScoredSortedSet("entities")).thenReturn(rsss);
        List<ScoredEntry<Object>> entities = new ArrayList<>();
        IgniteEntity entity1 = new IgniteCacheTestEntity();
        IgniteEntity entity2 = new IgniteCacheTestEntity();
        entities.add(new ScoredEntry<Object>(1D, entity1));
        entities.add(new ScoredEntry<Object>(TWO_DOUBLE, entity2));
        Mockito.when(rsss.entryRange(1, FIVE.getValue())).thenReturn(entities);
        List<IgniteEntity> expectedEntities = Arrays.asList(entity1, entity2);
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setRedissonClient(redisson);

        List<IgniteCacheTestEntity> actualEntities = redisCache
                .getEntitiesFromScoredSortedSet(
                        new GetScoredEntitiesRequest()
                                .withKey("entities")
                                .withStartIndex(1)
                                .withEndIndex(FIVE.getValue())
                                .withNamespaceEnabled(false));
        Assert.assertEquals(expectedEntities, actualEntities);

    }

    /**
     * Test get entities from scored sorted set with null key with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testGetEntitiesFromScoredSortedSetWithNullKeyWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache
                .getEntitiesFromScoredSortedSet(
                        new GetScoredEntitiesRequest()
                                .withStartIndex(1)
                                .withEndIndex(FIVE.getValue())
                                .withNamespaceEnabled(false));

    }

    /**
     * Test get entities from scored sorted set reversed with namespace disabled.
     */
    @Test
    public void testGetEntitiesFromScoredSortedSetReversedWithNamespaceDisabled() {
        RedissonClient redisson = Mockito.mock(RedissonClient.class);
        RScoredSortedSet<Object> rsss = (RScoredSortedSet<Object>) Mockito.mock(RScoredSortedSet.class);
        Mockito.when(redisson.getScoredSortedSet("entities")).thenReturn(rsss);
        List<ScoredEntry<Object>> entities = new ArrayList<>();
        IgniteEntity entity1 = new IgniteCacheTestEntity();
        IgniteEntity entity2 = new IgniteCacheTestEntity();
        entities.add(new ScoredEntry<Object>(1D, entity2));
        entities.add(new ScoredEntry<Object>(TWO_DOUBLE, entity1));
        Mockito.when(rsss.entryRangeReversed(1, FIVE.getValue())).thenReturn(entities);
        List<IgniteEntity> expectedEntities = Arrays.asList(entity2, entity1);
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setRedissonClient(redisson);

        List<IgniteCacheTestEntity> actualEntities = redisCache.getEntitiesFromScoredSortedSet(
                new GetScoredEntitiesRequest().withKey("entities")
                        .withStartIndex(1)
                        .withEndIndex(FIVE.getValue())
                        .fromReverseIndex()
                        .withNamespaceEnabled(false));
        Assert.assertEquals(expectedEntities, actualEntities);

    }

    /**
     * Test put string key value async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testPutStringKeyValueAsyncWithNamespaceDisabled() throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(rbucket.setAsync("world")).thenReturn(new CompletableFutureWrapper<Void>((Void) null));
        Future<String> ret = redisCache.putStringAsync(
                new PutStringRequest()
                        .withKey("hello")
                        .withValue("world")
                        .withMutationId("mut001")
                        .withNamespaceEnabled(false));
        Assert.assertTrue(ret.isDone());
        Assert.assertEquals("mut001", ret.get());
        Mockito.verify(rbucket).setAsync("world");
    }

    /**
     * Test put string key value async with null key with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test(expected = NullPointerException.class)
    public void testPutStringKeyValueAsyncWithNullKeyWithNamespaceDisabled()
            throws
            InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        Future<String> ret = redisCache.putStringAsync(
                new PutStringRequest()
                        .withValue("world")
                        .withMutationId("mut001")
                        .withNamespaceEnabled(false));
    }

    /**
     * Test put string key value async with null value with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test(expected = NullPointerException.class)
    public void testPutStringKeyValueAsyncWithNullValueWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        Future<String> ret = redisCache.putStringAsync(
                new PutStringRequest()
                        .withKey("hello")
                        .withMutationId("mut001")
                        .withNamespaceEnabled(false));
    }

    /**
     * Test put string key value async with null mutation id with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testPutStringKeyValueAsyncWithNullMutationIdWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(rbucket.setAsync("world")).thenReturn(new CompletableFutureWrapper<Void>((Void) null));
        Future<String> ret = redisCache.putStringAsync(
                new PutStringRequest()
                        .withKey("hello")
                        .withValue("world")
                        .withMutationId(null)
                        .withNamespaceEnabled(false));
        Assert.assertTrue(ret.isDone());
        Assert.assertNull(ret.get());
        Mockito.verify(rbucket).setAsync("world");
    }

    /**
     * Test put string key value with ttl async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testPutStringKeyValueWithTtlAsyncWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(rbucket.setAsync("world", THOUSAND_LONG, TimeUnit.MILLISECONDS))
                .thenReturn(new CompletableFutureWrapper<Void>((Void) null));
        Future<String> ret = redisCache
                .putStringAsync(new PutStringRequest()
                        .withKey("hello")
                        .withValue("world")
                        .withTtlMs(THOUSAND_LONG)
                        .withMutationId("mut001")
                        .withNamespaceEnabled(false));
        Assert.assertTrue(ret.isDone());
        Assert.assertEquals("mut001", ret.get());
        Mockito.verify(rbucket).setAsync("world", THOUSAND_LONG, TimeUnit.MILLISECONDS);
    }

    /**
     * Test put string key value if async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testPutStringKeyValueIfAsyncWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(rbucket.compareAndSetAsync("", "world")).thenReturn(new CompletableFutureWrapper(true));
        Future<String> ret = redisCache
                .putStringAsync(new PutStringRequest()
                        .withKey("hello")
                        .ifCurrentMatches("")
                        .withValue("world")
                        .withMutationId("mut001"));
        Assert.assertTrue(ret.isDone());
        Assert.assertEquals("mut001", ret.get());
        Mockito.verify(rbucket).compareAndSetAsync("", "world");
    }

    /**
     * Test put entity async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testPutEntityAsyncWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(rbucket.setAsync(entity)).thenReturn(new CompletableFutureWrapper<Void>((Void) null));
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<>();
        req.withKey("hello").withValue(entity).withMutationId("mut001").withNamespaceEnabled(false);
        Future<String> ret = redisCache.putEntityAsync(req);
        Assert.assertTrue(ret.isDone());
        Assert.assertEquals("mut001", ret.get());
        Mockito.verify(rbucket).setAsync(entity);
    }

    /**
     * Test put entity async with null key with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test(expected = NullPointerException.class)
    public void testPutEntityAsyncWithNullKeyWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<IgniteCacheTestEntity>();
        req.withValue(entity).withMutationId("mut001").withNamespaceEnabled(false);
        redisCache.putEntityAsync(req);
    }

    /**
     * Test put entity async with null value with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test(expected = NullPointerException.class)
    public void testPutEntityAsyncWithNullValueWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<IgniteCacheTestEntity>();
        req.withKey("hello").withMutationId("mut001").withNamespaceEnabled(false);
        redisCache.putEntityAsync(req);
    }

    /**
     * Test put entity async with null key value with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test(expected = NullPointerException.class)
    public void testPutEntityAsyncWithNullKeyValueWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache
                .putEntityAsync(new PutEntityRequest<IgniteCacheTestEntity>());
    }

    /**
     * Test put entity with ttl async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testPutEntityWithTtlAsyncWithNamespaceDisabled() throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(
                rbucket.setAsync(entity, THOUSAND_LONG, TimeUnit.MILLISECONDS))
                .thenReturn(new CompletableFutureWrapper<Void>((Void) null));
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<>();
        req.withKey("hello").withValue(entity).withMutationId("mut001").withNamespaceEnabled(false);
        req.withTtlMs(THOUSAND_LONG);
        Future<String> ret = redisCache.putEntityAsync(req);
        Assert.assertTrue(ret.isDone());
        Assert.assertEquals("mut001", ret.get());
        Mockito.verify(rbucket).setAsync(entity, THOUSAND_LONG, TimeUnit.MILLISECONDS);
    }

    /**
     * Test put entity if async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testPutEntityIfAsyncWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        IgniteCacheTestEntity newEntity = new IgniteCacheTestEntity();
        IgniteCacheTestEntity oldEntity = new IgniteCacheTestEntity();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RBucketAsync<Object> rbucket = (RBucketAsync<Object>) Mockito.mock(RBucketAsync.class);
        Mockito.when(rbatch.getBucket("hello")).thenReturn(rbucket);
        Mockito.when(
                rbucket.compareAndSetAsync(oldEntity, newEntity))
                .thenReturn(new CompletableFutureWrapper(true));
        PutEntityRequest<IgniteCacheTestEntity> req = new PutEntityRequest<>();
        req.withKey("hello").withValue(newEntity).withMutationId("mut001").withNamespaceEnabled(false);
        req.ifCurrentMatches(oldEntity);
        Future<String> ret = redisCache
                .putEntityAsync(req);
        Assert.assertTrue(ret.isDone());
        Assert.assertEquals("mut001", ret.get());
        Mockito.verify(rbucket).compareAndSetAsync(oldEntity, newEntity);
    }

    /**
     * Test add string to scored sorted set async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testAddStringToScoredSortedSetAsyncWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RScoredSortedSetAsync<Object> rsss = (RScoredSortedSetAsync<Object>) Mockito.mock(RScoredSortedSetAsync.class);
        Mockito.when(rbatch.getScoredSortedSet("presidents")).thenReturn(rsss);
        Mockito.when(rsss.addAsync(1D, "Abdul Kalam")).thenReturn(new CompletableFutureWrapper(true));
        Future<String> ret = redisCache.addStringToScoredSortedSetAsync(
                new AddScoredStringRequest()
                        .withKey("presidents")
                        .withScore(1D)
                        .withValue("Abdul Kalam")
                        .withMutationId("8undu7")
                        .withNamespaceEnabled(false));
        Assert.assertTrue(ret.isDone());
        Assert.assertEquals("8undu7", ret.get());
        Mockito.verify(rsss).addAsync(1D, "Abdul Kalam");
    }

    /**
     * Test add entity to scored sorted set async with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testAddEntityToScoredSortedSetAsyncWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setBatchSize(FIVE.getValue());
        RBatch rbatch = Mockito.mock(RBatch.class);
        redisCache.setRBatch(rbatch);
        RScoredSortedSetAsync<Object> rsss = (RScoredSortedSetAsync<Object>) Mockito.mock(RScoredSortedSetAsync.class);
        Mockito.when(rbatch.getScoredSortedSet("entities")).thenReturn(rsss);
        RFuture<Boolean> rfuture = (RFuture<Boolean>) Mockito.mock(RFuture.class);
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        Mockito.when(rsss.addAsync(1D, entity)).thenReturn(rfuture);
        Future<String> ret = redisCache.addEntityToScoredSortedSetAsync(
                new AddScoredEntityRequest<IgniteCacheTestEntity>()
                        .withKey("entities")
                        .withScore(1D)
                        .withValue(entity)
                        .withMutationId("8undu7")
                        .withNamespaceEnabled(false));
        Assert.assertFalse(ret.isDone());
        Mockito.verify(rsss).addAsync(1D, entity);
    }

    /**
     * Test async completed exceptionally with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testAsyncCompletedExceptionallyWithNamespaceDisabled() throws InterruptedException, ExecutionException {
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setBatchSize(FIVE.getValue());
        RBatch rbatch = Mockito.mock(RBatch.class);
        RScoredSortedSetAsync<Object> rsss = (RScoredSortedSetAsync<Object>) Mockito.mock(RScoredSortedSetAsync.class);
        Mockito.when(rbatch.getScoredSortedSet("entities")).thenReturn(rsss);
        Mockito.when(rsss.addAsync(1D, entity)).thenReturn(new CompletableFutureWrapper(false));
        redisCache.setRBatch(rbatch);
        Future<String> ret = redisCache.addEntityToScoredSortedSetAsync(
                new AddScoredEntityRequest<IgniteCacheTestEntity>()
                        .withKey("entities")
                        .withScore(1D)
                        .withValue(entity)
                        .withMutationId("8undu7")
                        .withNamespaceEnabled(false));
        // Assert.assertTrue(ret.isDone());
        try {
            ret.get();
            Assert.fail("Expecting RuntimeException when testing completedExceptionally");
        } catch (Throwable re) {
            Assert.assertEquals(
                    "com.harman.ignite.cache.exception.RedisBatchProcessingException: Redis batch update failed",
                    re.getMessage());
        }
        Mockito.verify(rsss).addAsync(1D, entity);
    }

    /**
     * Test async batch already executed with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testAsyncBatchAlreadyExecutedWithNamespaceDisabled()
            throws
            InterruptedException,
            ExecutionException {
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setBatchSize(FIVE.getValue());
        RBatch rbatch = Mockito.mock(RBatch.class);
        RScoredSortedSetAsync<Object> rsss = (RScoredSortedSetAsync<Object>) Mockito.mock(RScoredSortedSetAsync.class);
        Mockito.when(rbatch.getScoredSortedSet("entities")).thenReturn(rsss);
        Mockito.when(rsss.addAsync(1D, entity))
                .thenThrow(new IllegalStateException("Batch already has been executed!"));
        redisCache.setRBatch(rbatch);
        try {
            Future<String> ret = redisCache.addEntityToScoredSortedSetAsync(
                    new AddScoredEntityRequest<IgniteCacheTestEntity>()
                            .withKey("entities")
                            .withScore(1D)
                            .withValue(entity)
                            .withMutationId("8undu7")
                            .withNamespaceEnabled(false));
            Assert.fail("Expecting RuntimeException when testing completedExceptionally");
        } catch (Throwable re) {
            Assert.assertEquals("Batch operation failed despite trying 5 times",
                    re.getMessage());
        }
        Mockito.verify(rsss, Mockito.times(FIVE.getValue())).addAsync(1D, entity);
    }

    /**
     * Test async batch unexpected illegal state exception with namespace disabled.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    @Test
    public void testAsyncBatchUnexpectedIllegalStateExceptionWithNamespaceDisabled()
            throws InterruptedException, ExecutionException {
        IgniteCacheTestEntity entity = new IgniteCacheTestEntity();
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.setBatchSize(FIVE.getValue());
        RBatch rbatch = Mockito.mock(RBatch.class);
        RScoredSortedSetAsync<Object> rsss = (RScoredSortedSetAsync<Object>) Mockito.mock(RScoredSortedSetAsync.class);
        Mockito.when(rbatch.getScoredSortedSet("entities")).thenReturn(rsss);
        Mockito.when(rsss.addAsync(1D, entity))
                .thenThrow(new IllegalStateException("This is something we are not expecting"));
        redisCache.setRBatch(rbatch);
        try {
            Future<String> ret = redisCache.addEntityToScoredSortedSetAsync(
                    new AddScoredEntityRequest<IgniteCacheTestEntity>()
                            .withKey("entities")
                            .withScore(1D)
                            .withValue(entity)
                            .withMutationId("8undu7")
                            .withNamespaceEnabled(false));
            Assert.fail("Expecting RuntimeException when testing completedExceptionally");
        } catch (Throwable re) {
            Assert.assertEquals("This is something we are not expecting",
                    re.getMessage());
        }
        Mockito.verify(rsss).addAsync(1D, entity);
    }

    /**
     * Test file read.
     */
    @Test
    public void testFileRead() {
        String str = null;
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        try {
            str = redisCache.readFile("read-file-test.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertEquals("read-file-test", str);
    }

    /**
     * Test file read fail.
     */
    @Test(expected = RuntimeException.class)
    public void testFileReadFail() {
        String str = null;
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        try {
            str = redisCache.readFile("read-file-test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test get map with null key.
     */
    @Test(expected = NullPointerException.class)
    public void testGetMapWithNullKey() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.getMapOfEntities(new GetMapOfEntitiesRequest());
    }

    /**
     * Test put map with null key.
     */
    @Test(expected = NullPointerException.class)
    public void testPutMapWithNullKey() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.putMapOfEntities(new PutMapOfEntitiesRequest<IgniteCacheTestEntity>());
    }

    /**
     * Test put map with null value with namespace disabled.
     */
    @Test(expected = NullPointerException.class)
    public void testPutMapWithNullValueWithNamespaceDisabled() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        PutMapOfEntitiesRequest req = new PutMapOfEntitiesRequest<IgniteCacheTestEntity>();
        req.withKey("abc").withNamespaceEnabled(false);
        redisCache.putMapOfEntities(req);
    }

    /**
     * Test delete map with null value.
     */
    @Test(expected = NullPointerException.class)
    public void testDeleteMapWithNullValue() {
        IgniteCacheRedisImpl redisCache = new IgniteCacheRedisImpl();
        redisCache.deleteMapOfEntities(new DeleteMapOfEntitiesRequest());
    }

    /**
     * Test entity for testing.
     */
    public class IgniteCacheTestEntity implements IgniteEntity {

        /** The schema version. */
        private Version schemaVersion;

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

    }
}
