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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.cache.AddScoredEntityRequest;
import org.eclipse.ecsp.cache.AddScoredStringRequest;
import org.eclipse.ecsp.cache.DeleteEntryRequest;
import org.eclipse.ecsp.cache.DeleteMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.GetEntityRequest;
import org.eclipse.ecsp.cache.GetMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.GetScoredEntitiesRequest;
import org.eclipse.ecsp.cache.GetScoredStringsRequest;
import org.eclipse.ecsp.cache.GetStringRequest;
import org.eclipse.ecsp.cache.IgniteCache;
import org.eclipse.ecsp.cache.PutEntityRequest;
import org.eclipse.ecsp.cache.PutMapOfEntitiesRequest;
import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.cache.exception.DecodeException;
import org.eclipse.ecsp.cache.exception.FileNotFoundException;
import org.eclipse.ecsp.cache.exception.IgniteCacheException;
import org.eclipse.ecsp.cache.exception.JacksonCodecException;
import org.eclipse.ecsp.cache.exception.RedisBatchProcessingException;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.healthcheck.HealthMonitor;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.eclipse.ecsp.cache.redis.RedisConstants.TEN;
import static org.eclipse.ecsp.cache.redis.RedisConstants.TWO;
import static org.eclipse.ecsp.cache.redis.RedisProperty.REDIS_KEY_NAMESPACE_DELIMETER;

/**
 * Implementation of IgniteCache for Redis backend.<br>
 * Supports pipelined batch executions through its .*Async() methods.<br>
 * Clients of this class are encouraged to use Async methods to improve throughput.<br>
 * This may not be possible in all cases, for ex if the same value has to be read back immediately from Redis.<br>
 * But for typical store and forget or store and retrieve slightly later use cases,
 * Async methods will bring about quite a bit of throughput improvement.<br>
 * Operations support 2 data types:<br>
 * <ul>
 *     <li>String</li>
 *     <li>IgniteEntity</li>
 * </ul>
 *
 * @author ssasidharan
 */
@Repository
public class IgniteCacheRedisImpl implements IgniteCache, HealthMonitor {
    
    /** The Constant NUM_BATCH_RETRIES. */
    private static final int NUM_BATCH_RETRIES = 5;
    
    /** The Constant MINUS_ONE_LONG. */
    public static final long MINUS_ONE_LONG = -1L;
    
    /** The Constant REDIS_HEALTH_GUAGE. */
    public static final String REDIS_HEALTH_GUAGE = "REDIS_HEALTH_GUAGE";
    
    /** The Constant REDIS_HEALTH_MONITOR. */
    public static final String REDIS_HEALTH_MONITOR = "REDIS_HEALTH_MONITOR";
    
    /** The Constant LOGGER. */
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(IgniteCacheRedisImpl.class);
    
    /** The scan limit. */
    @Value("${redis.scan.limit:100}")
    private int scanLimit;
    
    /** The regex scan file name. */
    @Value("${redis.regex.scan.filename:scanregex.txt}")
    private String regexScanFileName;
    
    /** The string codec. */
    private StringCodec stringCodec = new StringCodec();
    
    /** The decoder. */
    private Decoder<Object> decoder;
    
    /** The redisson client. */
    @Autowired
    private RedissonClient redissonClient;
    // RTC-156940 - Loading custom Ignite Json Jackson codec specific to DMF /
    // ADA flow to encode and decode the RetryRecords without using @class
    /** The ignite codec class. */
    // parameter.
    @Value("${ignite.codec.class:}")
    private String igniteCodecClass;
    
    /** The retry record id pattern. */
    @Value("${retry.record.id.pattern}")
    private String retryRecordIdPattern;
    
    /** The object mapper. */
    @Autowired
    private ObjectMapper objectMapper;
    
    /** The scan regex script. */
    private String scanRegexScript;
    /**
     * Pipelining batch size. See redis pipelining for more details.
     */
    @Value("${redis.pipeline.size:1000}")
    private int batchSize = 1000;
    // One thread could be committing the batch when another thread could be
    // adding to the batch. To avoid this, using a volatile and assigning a new
    // batch so other threads that are about to execute a batch op, will use the
    // new batch. And existing threads that might be adding to the batch when
    // the batch is currently about to be committed are left to fail-retry
    // semantics. The only error would be that the batch may not always contain
    /** The current batch. */
    // batchSize entries; there can be more but not less. That is ok.
    private volatile RBatch currentBatch = null;
    
    /** The batch count. */
    /*
     * used for tracking the current size of the batch and to trigger execution
     * when size equals batch size
     */
    private AtomicInteger batchCount = new AtomicInteger(0);
    
    /** The last batch exec timestamp. */
    private AtomicLong lastBatchExecTimestamp = new AtomicLong(System.currentTimeMillis());

    /** The Constant MANDATORY_VALUE. */
    public static final String MANDATORY_VALUE = "value is mandatory";
    
    /** The Constant MANDATORY_KEY. */
    public static final String MANDATORY_KEY = "key is mandatory";

    /** The redis health monitor enabled. */
    @Value("${" + RedisProperty.REDIS_HEALTH_MONITOR_ENABLED + ":false}")
    private boolean redisHealthMonitorEnabled;

    /** The needs restart on failure. */
    @Value("${" + RedisProperty.REDIS_NEEDS_RESTART_ON_FAILURE + ":false}")
    private boolean needsRestartOnFailure;

    /** The redis key namespace. */
    @Value("#{'${redis.key.namespace:}'.trim()}")
    private String redisKeyNamespace;

    /** The healthy. */
    private volatile boolean healthy = true;

    /**
     * Instantiates a new ignite cache redis impl.
     */
    public IgniteCacheRedisImpl() {
        //default constructor
    }

    /**
     * Gets the string.
     *
     * @param key the key
     * @return the string
     */
    @Override
    public String getString(String key) {
        key = addNamespace(key, true);
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * Retrieves a string value from Redis based on the provided request.
     *
     * @param request the request containing the key and namespace information
     * @return the string value associated with the key
     */
    @Override
    public String getString(GetStringRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        return (String) redissonClient.getBucket(request.getKey()).get();
    }

    /**
     * Stores a string value in Redis based on the provided request.
     *
     * @param putRequest the request containing the key, value, and other parameters
     */
    @Override
    public void putString(PutStringRequest putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        RBucket<String> bucket = redissonClient.getBucket(putRequest.getKey());
        if (putRequest.getExpectedValue() == null) {
            if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                bucket.set(putRequest.getValue());
            } else {
                bucket.set(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS);
            }
        } else {
            bucket.compareAndSet(putRequest.getExpectedValue(), putRequest.getValue());
        }
    }

    /**
     * Retrieves an entity from Redis based on the provided key.
     *
     * @param <T> the type of the entity
     * @param key the key to retrieve the entity
     * @return the entity associated with the key
     */
    @Override
    public <T extends IgniteEntity> T getEntity(String key) {
        key = addNamespace(key, true);
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * Retrieves an entity from Redis based on the provided request.
     *
     * @param <T> the type of the entity
     * @param request the request containing the key and namespace information
     * @return the entity associated with the key
     */
    @Override
    public <T extends IgniteEntity> T getEntity(GetEntityRequest request) {
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        return (T) redissonClient.getBucket(request.getKey()).get();
    }

    /**
     * Stores an entity in Redis based on the provided request.
     *
     * @param <T> the type of the entity
     * @param putRequest the request containing the key, value, and other parameters
     */
    @Override
    public <T extends IgniteEntity> void putEntity(PutEntityRequest<T> putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        RBucket<T> bucket = redissonClient.getBucket(putRequest.getKey());
        if (putRequest.getExpectedValue() == null) {
            if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                bucket.set(putRequest.getValue());
            } else {
                bucket.set(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS);
            }
        } else {
            bucket.compareAndSet(putRequest.getExpectedValue(), putRequest.getValue());
        }
    }

    /**
     * Adds a string to a scored sorted set in Redis.
     *
     * @param request the request containing the key, value, and score
     */
    @Override
    public void addStringToScoredSortedSet(AddScoredStringRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<String> sset = redissonClient.getScoredSortedSet(request.getKey());
        sset.add(request.getScore(), request.getValue());
    }

    /**
     * Gets the strings from scored sorted set.
     *
     * @param request the request
     * @return the strings from scored sorted set
     */
    @Override
    public List<String> getStringsFromScoredSortedSet(GetScoredStringsRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<String> sset = redissonClient.getScoredSortedSet(request.getKey());
        if (request.isReversed()) {
            return sset.entryRangeReversed(request.getStartIndex(), request.getEndIndex()).stream()
                    .map(ScoredEntry::getValue).collect(Collectors.toList());
        } else {
            return sset.entryRange(request.getStartIndex(), request.getEndIndex())
                    .stream()
                    .map(ScoredEntry::getValue)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Adds an entity to a scored sorted set in Redis.
     *
     * @param <T> the type of the entity
     * @param request the request containing the key, value, and score
     */
    @Override
    public <T extends IgniteEntity> void addEntityToScoredSortedSet(AddScoredEntityRequest<T> request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<T> sset = redissonClient.getScoredSortedSet(request.getKey());
        sset.add(request.getScore(), request.getValue());
    }

    /**
     * Gets the entities from scored sorted set.
     *
     * @param <T> the generic type
     * @param request the request
     * @return the entities from scored sorted set
     */
    @Override
    public <T extends IgniteEntity> List<T> getEntitiesFromScoredSortedSet(GetScoredEntitiesRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        RScoredSortedSet<T> sset = redissonClient.getScoredSortedSet(request.getKey());
        if (request.isReversed()) {
            return sset.entryRangeReversed(request.getStartIndex(), request.getEndIndex())
                    .stream()
                    .map(ScoredEntry::getValue)
                    .toList();
        } else {
            return sset.entryRange(request.getStartIndex(), request.getEndIndex())
                    .stream()
                    .map(ScoredEntry::getValue)
                    .toList();
        }
    }

    /**
     * Asynchronously stores a string value in Redis based on the provided request.
     *
     * @param putRequest the request containing the key, value, and other parameters
     * @return a Future representing the result of the asynchronous operation
     */
    @Override
    public Future<String> putStringAsync(PutStringRequest putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RBucketAsync<String> bucket = currentBatch.getBucket(putRequest.getKey());
            final String mutationId = putRequest.getMutationId();
            if (putRequest.getExpectedValue() == null) {
                if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                    bucket.setAsync(putRequest.getValue())
                            .thenAccept(s -> f.complete(mutationId));
                } else {
                    bucket.setAsync(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS)
                            .thenAccept(s -> f.complete(mutationId));
                }
            } else {
                bucket.compareAndSetAsync(putRequest.getExpectedValue(), putRequest.getValue())
                        .thenAccept(s -> completeFuture(s, f, mutationId));
            }
        });
        return f;
    }

    /**
     * Asynchronously stores an entity in Redis based on the provided request.
     *
     * @param <T> the type of the entity
     * @param putRequest the request containing the key, value, and other parameters
     * @return a Future representing the result of the asynchronous operation
     */
    @Override
    public <T extends IgniteEntity> Future<String> putEntityAsync(PutEntityRequest<T> putRequest) {
        validate(putRequest);
        putRequest.withKey(addNamespace(putRequest.getKey(), putRequest.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RBucketAsync<T> bucket = currentBatch.getBucket(putRequest.getKey());
            final String mutationId = putRequest.getMutationId();
            if (putRequest.getExpectedValue() == null) {
                if (putRequest.getTtlMs() == MINUS_ONE_LONG) {
                    bucket.setAsync(putRequest.getValue())
                            .thenAccept(s -> f.complete(mutationId));
                } else {
                    bucket.setAsync(putRequest.getValue(), putRequest.getTtlMs(), TimeUnit.MILLISECONDS)
                            .thenAccept(s -> f.complete(mutationId));
                }
            } else {
                bucket.compareAndSetAsync(putRequest.getExpectedValue(), putRequest.getValue())
                        .thenAccept(s -> completeFuture(s, f, mutationId));
            }
        });
        return f;
    }

    /**
     * Adds a string to a scored sorted set in Redis asynchronously.
     *
     * @param request the request containing the key, value, and score
     * @return a Future representing the result of the asynchronous operation
     */
    @Override
    public Future<String> addStringToScoredSortedSetAsync(AddScoredStringRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RScoredSortedSetAsync<String> sset = currentBatch.getScoredSortedSet(request.getKey());
            final String mutationId = request.getMutationId();
            sset.addAsync(request.getScore(), request.getValue())
                    .thenAccept(s -> completeFuture(s, f, mutationId));
        });
        return f;
    }

    /**
     * Adds the entity to a scored sorted set asynchronously.
     *
     * @param <T> the generic type of the entity
     * @param request the request containing the entity and its score
     * @return a Future representing the result of the asynchronous operation
     */
    @Override
    public <T extends IgniteEntity> Future<String> addEntityToScoredSortedSetAsync(AddScoredEntityRequest<T> request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RScoredSortedSetAsync<T> sset = currentBatch.getScoredSortedSet(request.getKey());
            final String mutationId = request.getMutationId();
            RFuture<Boolean> rf = sset.addAsync(request.getScore(), request.getValue());
            rf.thenAccept(s -> completeFuture(s, f, mutationId));
        });
        return f;
    }

    /**
     * Deletes the entry associated with the given key from Redis.
     *
     * @param key the key of the entry to be deleted
     */
    @Override
    public void delete(String key) {
        key = addNamespace(key, true);
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.delete();
    }

    /**
     * Deletes the entry associated with the given key from Redis.
     *
     * @param deleteRequest the delete request containing the key and namespace information
     */
    @Override
    public void delete(DeleteEntryRequest deleteRequest) {
        validate(deleteRequest);
        deleteRequest.withKey(addNamespace(deleteRequest.getKey(), deleteRequest.getNamespaceEnabled()));
        redissonClient.getBucket(deleteRequest.getKey()).delete();
    }

    /**
     * Asynchronously deletes the entry associated with the given key from Redis.
     *
     * @param deleteRequest the delete request containing the key and namespace information
     * @return a Future representing the result of the asynchronous operation
     */
    @Override
    public Future<String> deleteAsync(DeleteEntryRequest deleteRequest) {
        validate(deleteRequest);
        deleteRequest.withKey(addNamespace(deleteRequest.getKey(), deleteRequest.getNamespaceEnabled()));
        CompletableFuture<String> f = new CompletableFuture<>();
        performBatchOperation(v -> {
            RBucketAsync<String> bucket = currentBatch.getBucket(deleteRequest.getKey());
            final String mutationId = deleteRequest.getMutationId();
            bucket.deleteAsync().thenAccept(s -> f.complete(mutationId));

        });
        return f;
    }

    /**
     * Sets the batch size.
     *
     * @param batchSize the new batch size
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * This methods tries to scan redis keys with the regex provided and returns key value pairs.
     *
     * @param <T> the generic type
     * @param keyRegex the key regex
     * @param namespaceEnabled the namespace enabled
     * @return the key value pairs for regex
     */
    @Override
    public <T extends IgniteEntity> Map<String, T> getKeyValuePairsForRegex(
            String keyRegex, Optional<Boolean> namespaceEnabled) {
        Map<String, T> keyValuePairs = new HashMap<>();
        if ((namespaceEnabled.isPresent() && Boolean.TRUE.equals(namespaceEnabled.get()))
                || namespaceEnabled.isEmpty()) {
            keyRegex = addNamespace(keyRegex, true);
        }
        if (scanLimit < TEN.getValue()) {
            scanLimit = (int) TEN.getValue();
            LOGGER.warn("Scan limit for redis cache should be at least 10. Changing scan limit to 10");
        }
        LOGGER.debug("Scanning Redis with ScanLimit {} and keyRegex {}", scanLimit, keyRegex);
        long cursor = 0L;
        do {
            List<Object> matches = redissonClient.getScript(stringCodec).eval(RScript.Mode.READ_ONLY,
                    scanRegexScript,
                    RScript.ReturnType.MULTI, Collections.emptyList(), cursor, scanLimit, keyRegex);
            if (!matches.isEmpty()) {
                cursor = (long) matches.get(0);
                LOGGER.debug("Received cursor value {}", cursor);
            } else {
                cursor = 0L;
                LOGGER.error("No more result found for regex scan. Exiting !!!");
            }
            int size = matches.size();
            for (int index = 1; index < size; index = (int) (index + TWO.getValue())) {
                String key = String.valueOf(matches.get(index));
                String value = String.valueOf(matches.get(index + 1));
                T entity;
                try {
                    entity = (T) decoder.decode(Unpooled.wrappedBuffer(value.getBytes()), null);
                    LOGGER.debug("Decoded entity for key {} is {}", key, entity);
                    keyValuePairs.put(key, entity);
                } catch (IOException e) {
                    LOGGER.error("Unable to decode value {} from cache for key {}", value, key, e);
                    throw new DecodeException(
                            String.format("Unable to decode value %s from cache for key %s", value, key), e);
                }
            }
        } while (cursor > 0);
        LOGGER.debug("Key Value Pairs for regex {} of total size {} being returned are as follows {}",
                keyRegex,
                keyValuePairs.size(),
                keyValuePairs);
        return keyValuePairs;
    }

    /**
     * Stores a map of entities in Redis based on the provided request.
     *
     * @param <T> the type of the entities
     * @param mapRequest the request containing the key, value, and other parameters
     */
    @Override
    public <T extends IgniteEntity> void putMapOfEntities(PutMapOfEntitiesRequest<T> mapRequest) {
        validate(mapRequest);
        mapRequest.withKey(addNamespace(mapRequest.getKey(), mapRequest.getNamespaceEnabled()));
        String key = mapRequest.getKey();
        Map<String, T> value = mapRequest.getValue();

        RMap<String, T> rmap = redissonClient.getMap(key);
        rmap.putAll(value);
        LOGGER.debug("Put map {} to Redis for key {}", value, key);
    }

    /**
     * Retrieves a map of entities from Redis based on the provided request.
     *
     * @param <T> the type of the entities
     * @param mapRequest the request containing the key and namespace information
     * @return the map of entities associated with the key
     */
    @Override
    public <T extends IgniteEntity> Map<String, T> getMapOfEntities(GetMapOfEntitiesRequest mapRequest) {
        validate(mapRequest);
        mapRequest.withKey(addNamespace(mapRequest.getKey(), mapRequest.getNamespaceEnabled()));
        String key = mapRequest.getKey();

        RMap<String, T> rmap = redissonClient.getMap(key);
        Set<String> fields = mapRequest.getFields();
        if (fields != null && !fields.isEmpty()) {
            LOGGER.debug("Attempting to get key value pairs from Redis for subkeys {} with key {}", fields, key);
            return rmap.getAll(fields);
        } else {
            LOGGER.debug("Attempting to get all key value pairs from Redis with parent key {}", key);
            return rmap.readAllMap();
        }
    }

    /**
     * Deletes a map of entities from Redis based on the provided request.
     *
     * @param request the request containing the key and namespace information
     */
    @Override
    public void deleteMapOfEntities(DeleteMapOfEntitiesRequest request) {
        validate(request);
        request.withKey(addNamespace(request.getKey(), request.getNamespaceEnabled()));
        String key = request.getKey();
        Set<String> fields = request.getFields();
        if (fields != null && !fields.isEmpty()) {
            LOGGER.debug("Attempting to remove key value pairs from Redis for subkeys {} with key {}", fields, key);
            redissonClient.getMap(key).fastRemove(fields.toArray());
        } else {
            LOGGER.debug("Attempting to remove all key value pairs from Redis with parent key {}", key);
            redissonClient.getMap(key).delete();
        }

    }

    /**
     * Adds the namespace.
     *
     * @param key the key
     * @param namespaceEnabled the namespace enabled
     * @return the string
     */
    private String addNamespace(String key, boolean namespaceEnabled) {
        if (StringUtils.isNotEmpty(redisKeyNamespace) && namespaceEnabled) {
            LOGGER.debug("Namespace enabled: {}, Namespace value for redis: {}, for key: {}",
                    namespaceEnabled, redisKeyNamespace, key);
            key = redisKeyNamespace + REDIS_KEY_NAMESPACE_DELIMETER + key;
            LOGGER.debug("Ignite cache key with namespace {}", key);
        }
        return key;
    }

    /**
     * Executes the batch operation consumer in a reliable way. <br>
     * If a thread was performing a batch operation and another thread performed RBatch.execute() at the same time,
     * then first thread will fail with IllegalStateException("Batch already has been executed").
     * This method performs a retry for such scenarios. Also advances the batch state.
     *
     * @param c the batch operation consumer
     * @throws RuntimeException
     *         if NUM_BATCH_RETRIES exhausted, and it still fails
     */
    private void performBatchOperation(Consumer<Void> c) {
        for (int i = 1; i <= NUM_BATCH_RETRIES; i++) {
            try {
                c.accept(null);
                break;
            } catch (IllegalStateException ise) {
                if (!ise.getMessage().contains("Batch already has been executed")) {
                    throw ise;
                }
                if (i == NUM_BATCH_RETRIES) {
                    throw new RedisBatchProcessingException(
                            "Batch operation failed despite trying " + NUM_BATCH_RETRIES + " times");
                }
            } catch (NullPointerException npe) {
                // This has been introduced because it was observed in
                // integration test case
                // IgniteCacheRedisImplIntegrationTest.testBatchConcurrentExecutionException()
                if (i == NUM_BATCH_RETRIES) {
                    LOGGER.warn("SetAsync invoked before a new batch was created", npe);
                    throw new RedisBatchProcessingException(
                            "Batch operation failed despite trying " + NUM_BATCH_RETRIES + " times");
                }
            }
        }
        advanceBatchState();
    }

    /**
     * Advance batch state.
     */
    private void advanceBatchState() {
        int size = batchCount.incrementAndGet();
        if (size % batchSize == 0) {
            // before executing the batch we will keep a reference and then swap
            // the main reference to a new instance of batch so clients can
            // continue adding to batch
            RBatch existingBatch = currentBatch;
            // now assign new batch to the same reference so that other threads
            // see the new batch and not the old one (currentBatch is volatile)
            startBatch();
            LOGGER.debug("Executing batch asynchronously");
            existingBatch.executeAsync().thenAccept(r -> {
                LOGGER.debug("Executed batch asynchronously");
                LOGGER.trace("Responses of last batch operation: {}",
                        r.getResponses());
            });
        }
    }

    /**
     * Completes the given CompletableFuture based on the success flag.
     *
     * @param success     indicates if the operation was successful
     * @param f           the CompletableFuture to complete
     * @param mutationId  the mutation ID to complete the future with if successful
     */
    private void completeFuture(boolean success, CompletableFuture<String> f, final String mutationId) {
        if (success) {
            f.complete(mutationId);
        } else {
            f.completeExceptionally(new RedisBatchProcessingException("Redis batch update failed"));
        }
    }

    /**
     * Sets the redisson client.
     * Added to support test cases.
     *
     * @param redissonClient the new redisson client
     */
    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Sets the r batch.
     * Added to support test cases.
     *
     * @param batch the new r batch
     */
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    void setRBatch(RBatch batch) {
        this.currentBatch = batch;
    }

    /**
     * Initializes the `IgniteCacheRedisImpl` instance after construction.
     * This method reads the scan regex script from the specified file and sets up the decoder.
     * It also starts the initial batch for Redis operations.
     *
     * @throws IgniteCacheException if there is an error reading the scan regex file
     */
    @PostConstruct
    private void postConstruct() {
        try {
            scanRegexScript = readFile(regexScanFileName);
            LOGGER.info("Scan Regex file contents : {}", scanRegexScript);
        } catch (IOException e) {
            throw new IgniteCacheException(String.format("Unable to read from file : %s", regexScanFileName), e);
        }

        if (StringUtils.isBlank(igniteCodecClass)) {
            LOGGER.info("Loading decoder from default JsonJacksonCodec class....");
            decoder = JsonJacksonCodec.INSTANCE.getValueDecoder();
        } else {
            try {
                // RTC-156940 - Redis issue when the component is not able
                // to send to device, and we restart the component.
                // In order to fix the above issue, a custom codec is needed to
                // be loaded for DMF/ADA flow which will be used to encode and
                // decode the data to the Redis without including @class
                // parameter.
                LOGGER.info("Loading decoder from ignite codec  ....");
                Class<Codec> clazz = (Class<Codec>) Class.forName(igniteCodecClass);

                decoder = clazz.getConstructor(ObjectMapper.class, String.class)
                        .newInstance(objectMapper, retryRecordIdPattern)
                        .getValueDecoder();
            } catch (InstantiationException
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException
                    | NoSuchMethodException
                    | SecurityException
                    | ClassNotFoundException e) {
                LOGGER.error("Unable to load ignite json jackson codec : {}", igniteCodecClass);
                throw new JacksonCodecException(
                        String.format("Unable to load ignite json jackson codec : %s", igniteCodecClass), e);
            }
        }
        startBatch();
    }

    /**
     * Starts a new batch for Redis operations.
     * This method initializes the `currentBatch` with a new instance of `RBatch`.
     * It also resets the `batchCount` to 0 if it reaches the `batchSize` and updates the `lastBatchExecTimestamp`.
     */
    private void startBatch() {
        currentBatch = redissonClient.createBatch();
        boolean updated = batchCount.compareAndSet(batchSize, 0);
        if (updated) {
            lastBatchExecTimestamp.set(System.currentTimeMillis());
        }
    }

    /**
     * Validates the `PutMapOfEntitiesRequest` to ensure that the key and value are not null.
     *
     * @param request the request containing the key and value to be validated
     * @throws NullPointerException if the key or value is null
     */
    private void validate(PutMapOfEntitiesRequest<?> request) {
        Objects.requireNonNull(
                request.getKey(), "Received null/empty key in put map request.Aborting the request.");
        Objects.requireNonNull(
                request.getValue(), "Received null/empty value in put map request.Aborting the request.");
    }

    /**
     * Validates the `GetMapOfEntitiesRequest` to ensure that the key is not null.
     *
     * @param request the request containing the key to be validated
     * @throws NullPointerException if the key is null
     */
    private void validate(GetMapOfEntitiesRequest request) {
        Objects.requireNonNull(request.getKey(), "Received null/empty key in get map request.Aborting the request.");
    }

    /**
     * Validates the `DeleteMapOfEntitiesRequest` to ensure that the key is not null.
     *
     * @param request the request containing the key to be validated
     * @throws NullPointerException if the key is null
     */
    private void validate(DeleteMapOfEntitiesRequest request) {
        Objects.requireNonNull(request.getKey(), "Received null/empty key in delete map request.Aborting the request.");
    }

    /**
     * Validates the `DeleteEntryRequest` to ensure that the key is not null.
     *
     * @param request the request containing the key to be validated
     * @throws NullPointerException if the key is null
     */
    private void validate(DeleteEntryRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    /**
     * Validates the `GetStringRequest` to ensure that the key is not null.
     *
     * @param request the request containing the key to be validated
     * @throws NullPointerException if the key is null
     */
    private void validate(GetStringRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    /**
     * Validates the `AddScoredStringRequest` to ensure that the key and value are not null.
     *
     * @param request the request containing the key and value to be validated
     * @throws NullPointerException if the key or value is null
     */
    private void validate(AddScoredStringRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(request.getValue(), MANDATORY_VALUE);
    }

    /**
     * Validates the `GetScoredStringsRequest` to ensure that the key is not null.
     *
     * @param request the request containing the key to be validated
     * @throws NullPointerException if the key is null
     */
    private void validate(GetScoredStringsRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    /**
     * Validates the `AddScoredEntityRequest` to ensure that the key and value are not null.
     *
     * @param request the request containing the key and value to be validated
     * @throws NullPointerException if the key or value is null
     */
    private void validate(AddScoredEntityRequest<?> request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(request.getValue(), MANDATORY_VALUE);
    }

    /**
     * Validates the `GetScoredEntitiesRequest` to ensure that the key and value are not null.
     *
     * @param request the request containing the key and value to be validated
     * @throws NullPointerException if the key or value is null
     */
    private void validate(GetScoredEntitiesRequest request) {
        Objects.requireNonNull(request.getKey(), MANDATORY_KEY);
    }

    /**
     * Validates the `PutStringRequest` to ensure that the key and value are not null.
     *
     * @param request the request containing the key and value to be validated
     * @throws NullPointerException if the key or value is null
     */
    private void validate(PutStringRequest putRequest) {
        Objects.requireNonNull(putRequest.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(putRequest.getValue(), MANDATORY_VALUE);
    }

    /**
     * Validates the `PutEntityRequest` to ensure that the key and value are not null.
     *
     * @param request the request containing the key and value to be validated
     * @throws NullPointerException if the key or value is null
     */
    private void validate(PutEntityRequest<?> putRequest) {
        Objects.requireNonNull(putRequest.getKey(), MANDATORY_KEY);
        Objects.requireNonNull(putRequest.getValue(), MANDATORY_VALUE);
    }

    /**
     * Read file.
     *
     * @param fileName the file name
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    String readFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new FileNotFoundException(fileName + " not found !!!");
        }
        return IOUtils.toString(inputStream, Charset.defaultCharset());
    }

    /**
     * Sets the healthy.
     *
     * @param isHealthy the new healthy
     */
    public void setHealthy(boolean isHealthy) {
        this.healthy = isHealthy;
    }

    /**
     * Checks if is healthy.
     *
     * @param forceHealthCheck the force health check
     * @return true, if is healthy
     */
    @Override
    public boolean isHealthy(boolean forceHealthCheck) {
        if (forceHealthCheck) {
            healthy = forceHealthCheck();
        }
        return healthy;
    }

    /**
     * Force health check.
     *
     * @return true, if successful
     */
    private boolean forceHealthCheck() {
        try {
            putString(new PutStringRequest().withKey("hello").withValue("world").withNamespaceEnabled(false));
            delete(new DeleteEntryRequest().withKey("hello").withNamespaceEnabled(false));
            healthy = true;
        } catch (Exception ex) {
            healthy = false;
            LOGGER.error("Error occured during Redis forceHealthCheck : {}", ex.getCause());
        }
        return healthy;
    }

    /**
     * Monitor name.
     *
     * @return the string
     */
    @Override
    public String monitorName() {
        return REDIS_HEALTH_MONITOR;
    }

    /**
     * Needs restart on failure.
     *
     * @return true, if successful
     */
    @Override
    public boolean needsRestartOnFailure() {
        return needsRestartOnFailure;
    }

    /**
     * Metric name.
     *
     * @return the string
     */
    @Override
    public String metricName() {
        return REDIS_HEALTH_GUAGE;
    }

    /**
     * Checks if is enabled.
     *
     * @return true, if is enabled
     */
    @Override
    public boolean isEnabled() {
        return redisHealthMonitorEnabled;
    }

}