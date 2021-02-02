/**
 * Copyright 2018-2020 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.core.caching;

import java.util.Observer;
import java.util.Set;

/**
 * Beacon Cache used to cache the Beacons generated by all sessions, actions, ...
 */
public interface BeaconCache {

    /**
     * Add an {@link Observer} which gets notified after a new event data or action data got inserted.
     *
     * @param o Observer to add.
     */
    void addObserver(Observer o);

    /**
     * Add event data for a given {@code key} to this cache.
     *
     * <p>
     * All registered observers are notified, after the event data has been added.
     * </p>
     *
     * @param key The key of the beacon (aka Session ID and Session seq. no.) for which to add event data.
     * @param timestamp The data's timestamp.
     * @param data serialized event data to add.
     */
    void addEventData(BeaconKey key, long timestamp, String data);

    /**
     * Add action data for a given {@code beaconID} to this cache.
     *
     * @param key The key of the beacon (aka Session ID and Session seq. no.) for which to add action data.
     * @param timestamp The data's timestamp.
     * @param data serialized action data to add.
     */
    void addActionData(BeaconKey key, long timestamp, String data);

    /**
     * Delete a cache entry for a given {@code key}.
     *
     * @param key The beacon's ID (aka Session ID and Session seq. no.) which to delete.
     */
    void deleteCacheEntry(BeaconKey key);

    /**
     * Prepare all data, that has been recorded so far, for sending.
     *
     * <p>
     * Note: This method must only be invoked from the beacon sending thread.
     * </p>
     *
     * @param key The beacon's ID (aka Session ID and Session seq. no.) for which to copy the collected data.
     */
    void prepareDataForSending(BeaconKey key);

    /**
     * Test if there is more data to send.
     *
     * @param key key The beacon's ID (aka Session ID and Session seq. no.) for which to copy the collected data.
     * @return {@code true} if there is data for sending,
     *         {@code false} if {@link BeaconKey} does not exist or there is no data for sending.
     */
    boolean hasDataForSending(BeaconKey key);

    /**
     * Get the next chunk for sending to the backend.
     *
     * <p>
     * Note: This method must only be invoked from the beacon sending thread.
     * </p>
     *
     * @param key The key of the beacon for which to get the next chunk.
     * @param chunkPrefix Prefix to append to the beginning of the chunk.
     * @param maxSize Maximum chunk size. As soon as chunk's size is greater than or equal to maxSize result is returned.
     * @param delimiter Delimiter between consecutive chunks.
     *
     * @return {@code null} if given {@code key} does not exist, an empty string, if there is no more data to send
     * or the next chunk to send.
     */
    String getNextBeaconChunk(BeaconKey key, String chunkPrefix, int maxSize, char delimiter);

    /**
     * Remove all data that was previously included in chunks.
     *
     * <p>
     * This method must be called, when data retrieved via {@link #getNextBeaconChunk(BeaconKey, String, int, char)}
     * was successfully sent to the backend, otherwise subsequent calls to {@link #getNextBeaconChunk(BeaconKey, String, int, char)}
     * will retrieve the same data again and again.
     * </p>
     *
     * <p>
     * Note: This method must only be invoked from the beacon sending thread.
     * </p>
     *
     * @param key The key of the beacon for which to remove already chunked data.
     */
    void removeChunkedData(BeaconKey key);

    /**
     * Reset all data that was previously included in chunks.
     *
     * <p>
     * Note: This method must only be invoked from the beacon sending thread.
     * </p>
     *
     * @param key The key of the beacon for which to remove already chunked data.
     */
    void resetChunkedData(BeaconKey key);

    /**
     * Get a Set of currently inserted {@link BeaconKey}s.
     *
     * <p>
     * The return value is a snapshot of currently inserted beacon keys.
     * All changes made after this call are not reflected in the returned Set.
     * </p>
     *
     * @return Snapshot of all beacon keys in the cache.
     */
    Set<BeaconKey> getBeaconKeys();

    /**
     * Evict {@link BeaconCacheRecord beacon cache records} by age for a given beacon.
     *
     * @param key      The key identifying a beacon.
     * @param minTimestamp  The minimum timestamp allowed.
     *
     * @return Returns the number of evicted cache records.
     */
    int evictRecordsByAge(BeaconKey key, long minTimestamp);

    /**
     * Evict {@link BeaconCacheRecord beacon cache records} by number for given beacon.
     *
     * @param key   The key identifying a beacon beacon.
     * @param numRecords The maximum number of records to evict.
     *
     * @return Returns the number of evicted cache records.
     */
    int evictRecordsByNumber(BeaconKey key, int numRecords);

    /**
     * Get number of bytes currently stored in cache.
     *
     * @return Number of bytes currently stored in cache.
     */
    long getNumBytesInCache();

    /**
     * Tests if an cached entry for {@code key} is empty.
     *
     * @param key The key identifying a beacon.
     * @return {@code true} if the cached entry is empty, {@code false} otherwise.
     */
    boolean isEmpty(BeaconKey key);
}
