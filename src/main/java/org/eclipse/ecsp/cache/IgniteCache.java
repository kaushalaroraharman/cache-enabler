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

package org.eclipse.ecsp.cache;

import org.eclipse.ecsp.entities.IgniteEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Base contract for cache in Ignite.
 * Support for optimized implementations that can perform batched updates through its .*Async() methods.
 * Clients of this interface are encouraged to use Async methods to improve throughput.
 * This may not be possible in all cases, for ex if the same value has to be read back immediately from Redis.
 * But for typical store and forget or store and retrieve slightly later use cases,
 * Async methods will bring about quite a bit of throughput improvement.
 * <br> <br>
 * Operations support 2 data types:
 * <ul>
 * <li>String</li>
 * <li>IgniteEntity</li>
 * </ul>
 *
 * @author ssasidharan
 */
public interface IgniteCache {
    /**
     * Retrieves a string value associated with the given key.
     *
     * @param key the key to retrieve the string value for
     * @return the string value associated with the key
     */
    String getString(String key);

    /**
     * Retrieves a string value based on the provided request.
     *
     * @param request the request containing the parameters for retrieving the string
     * @return the string value based on the request
     */
    String getString(GetStringRequest request);

    /**
     * Stores a string value based on the provided request.
     *
     * @param request the request containing the parameters for storing the string
     */
    void putString(PutStringRequest request);

    /**
     * Adds the put string mutation operation to a batch and completes the future when the batch is committed.
     *
     * @param request the put string request
     * @return future that returns the mutationId from the original request
     */
    Future<String> putStringAsync(PutStringRequest request);

    /**
     * Retrieves an entity based on the provided request.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param getRequest the request containing the parameters for retrieving the entity
     * @return the entity based on the request
     */
    <T extends IgniteEntity> T getEntity(GetEntityRequest getRequest);

    /**
     * Retrieves an entity associated with the given key.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param key the key to retrieve the entity for
     * @return the entity associated with the key
     */
    <T extends IgniteEntity> T getEntity(String key);

    /**
     * Stores an entity based on the provided request.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param putRequest the request containing the parameters for storing the entity
     */
    <T extends IgniteEntity> void putEntity(PutEntityRequest<T> putRequest);

    /**
     * Adds the put entity mutation operation to a batch and completes the future when the batch is committed.
     *
     * @param putRequest the put entity request
     * @param <T> the type of the entity extending IgniteEntity
     * @return future that returns the mutationId from the original request
     */
    <T extends IgniteEntity> Future<String> putEntityAsync(PutEntityRequest<T> putRequest);

    /**
     * Adds a string to a scored sorted set based on the provided request.
     *
     * @param request the request containing the parameters for adding the string to the scored sorted set
     */
    void addStringToScoredSortedSet(AddScoredStringRequest request);

    /**
     * Adds the scored set string append mutation to a batch and completes the future when the batch is committed.
     *
     * @param request the add scored string request
     * @return future that returns the mutationId from the original request
     */
    Future<String> addStringToScoredSortedSetAsync(AddScoredStringRequest request);

    /**
     * Retrieves a list of strings from a scored sorted set based on the provided request.
     *
     * @param request the request containing the parameters for retrieving the strings
     * @return the list of strings from the scored sorted set
     */
    List<String> getStringsFromScoredSortedSet(GetScoredStringsRequest request);

    /**
     * Adds an entity to a scored sorted set based on the provided request.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param request the request containing the parameters for adding the entity to the scored sorted set
     */
    <T extends IgniteEntity> void addEntityToScoredSortedSet(AddScoredEntityRequest<T> request);

    /**
     * Adds the scored set entity append mutation to a batch and completes the future when the batch is committed.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param request the add scored entity request
     * @return future that returns the mutationId from the original request
     */
    <T extends IgniteEntity> Future<String> addEntityToScoredSortedSetAsync(AddScoredEntityRequest<T> request);

    /**
     * Retrieves a list of entities from a scored sorted set based on the provided request.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param request the request containing the parameters for retrieving the entities
     * @return the list of entities from the scored sorted set
     */
    <T extends IgniteEntity> List<T> getEntitiesFromScoredSortedSet(GetScoredEntitiesRequest request);

    /**
     * Retrieves a map of key-value pairs for entities matching the given key regex.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param keyRegex the regex pattern to match keys
     * @param namespaceEnabled optional flag to enable namespace
     * @return the map of key-value pairs for entities matching the key regex
     */
    <T extends IgniteEntity> Map<String, T> getKeyValuePairsForRegex(String keyRegex,
            Optional<Boolean> namespaceEnabled);

    /**
     * Deletes the entry associated with the given key.
     *
     * @param key the key to delete the entry for
     */
    void delete(String key);

    /**
     * Deletes the entry based on the provided request.
     *
     * @param deleteRequest the request containing the parameters for deleting the entry
     */
    void delete(DeleteEntryRequest deleteRequest);

    /**
     * Adds the delete entry mutation operation to a batch and completes the future when the batch is committed.
     *
     * @param deleteRequest the delete entry request
     * @return future that returns the mutationId from the original request
     */
    Future<String> deleteAsync(DeleteEntryRequest deleteRequest);

    /**
     * Stores a map of entities based on the provided request.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param request the request containing the parameters for storing the map of entities
     */
    <T extends IgniteEntity> void putMapOfEntities(PutMapOfEntitiesRequest<T> request);

    /**
     * Retrieves a map of entities based on the provided request.
     *
     * @param <T> the type of the entity extending IgniteEntity
     * @param request the request containing the parameters for retrieving the map of entities
     * @return the map of entities based on the request
     */
    <T extends IgniteEntity> Map<String, T> getMapOfEntities(GetMapOfEntitiesRequest request);

    /**
     * Deletes a map of entities based on the provided request.
     *
     * @param request the request containing the parameters for deleting the map of entities
     */
    void deleteMapOfEntities(DeleteMapOfEntitiesRequest request);
}