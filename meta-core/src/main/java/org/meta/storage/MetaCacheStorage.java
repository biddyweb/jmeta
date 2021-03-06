/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.storage;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.KVStorage;
import org.meta.api.storage.MetaCache;
import org.meta.api.storage.MetaTx;
import org.meta.api.storage.Serializer;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Cache implementation of {@link KVStorage} providing LRU eviction and expiration of entries.
 *
 * It relies on a second KVStorage implementation to actually store values.
 *
 * It is assumed that all operations of the backing KVStorage are thread-safe.
 *
 * There is an overhead in the backing storage of 8 bytes per entry for the TTL value.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class MetaCacheStorage implements MetaCache {

    /**
     * The key under which the timeout manager structures will be stored in database.
     */
    public static final byte[] KEY = "metaCache".getBytes();

    private final Logger logger = LoggerFactory.getLogger(MetaCacheStorage.class);

    /**
     * The underlying storage.
     */
    private final KVStorage storage;

    /**
     * Sorted map of keys with expiration timestamp.
     */
    private final TreeMap<Long, CacheBucket> expiryKeys;

    /**
     * The cache data.
     */
    private final Map<byte[], CacheEntry> entries;

    /**
     * The maximum number of entries retained in memory.
     */
    private final int maxSize;

    /**
     * Lock object for synchronization.
     */
    private final Object lock = new Object();

    private final CacheEntry start = new CacheEntry();
    private final CacheEntry end = new CacheEntry();

    /**
     * Constructor with backing storage.
     *
     * @param dbStorage the backing storage unit to use.
     * @param maxEntries the maximum number of entries this cache will retain in memory
     */
    public MetaCacheStorage(final KVStorage dbStorage, final int maxEntries) {
        this.storage = dbStorage;
        this.maxSize = maxEntries;
        this.expiryKeys = new TreeMap<>();
        this.entries = new HashMap<>(this.maxSize);
        this.start.next(end);
        this.end.previous(start);
        deserialize();
    }

    /**
     * Get the timeout descriptors from underlying storage.
     */
    private void deserialize() {
        byte[] timeoutStorageData = this.storage.get(KEY);
        if (timeoutStorageData == null) {
            return;
        }
        ByteBuffer buf = ByteBuffer.wrap(timeoutStorageData);
        byte[] longKey = new byte[Long.BYTES];

        while (buf.remaining() > 0) {
            buf.get(longKey);
            long timestamp = SerializationUtils.bytesToLong(longKey);
            byte[] bucketData = this.storage.get(longKey);
            if (bucketData != null) {
                this.expiryKeys.put(timestamp, new CacheBucket(bucketData));
            }
        }
    }

    private void insertHead(final CacheEntry entry) {
        synchronized (lock) {
            entry.previous(start);
            entry.next(start.next());
            start.next().previous(entry);
            start.next(entry);
        }
    }

    /**
     * Get a cache entry from the underlying storage.
     *
     * @param key the key to fetch
     * @return the cache entry, or null if not found
     */
    private CacheEntry getEntryFromStorage(final byte[] key) {
        byte[] data = this.storage.get(key);

        if (data == null) {
            return null;
        }
        CacheEntry entry = new CacheEntry(key, data);
        synchronized (lock) {
            this.insertHead(entry);
        }
        return entry;
    }

    /**
     * Get the entry associated to the given key in cache.
     *
     * @param key the key to fetch
     * @return the found CacheEntry or null if not found in cache
     */
    private CacheEntry getCacheEntry(final byte[] key) {
        synchronized (lock) {
            return entries.get(key);
        }
    }

    /**
     * Get the entry associated to the given key, starting by seeking the cache, then the underlying storage
     * if not found.
     *
     * @param key the key to fetch
     * @return the found CacheEntry or null if not found
     */
    private CacheEntry getEntry(final byte[] key) {
        CacheEntry entry;

        synchronized (lock) {
            entry = entries.get(key);
        }
        if (entry == null) {
            entry = this.getEntryFromStorage(key);
        }
        return entry;
    }

    /**
     * Add a data entry to appropriate bucket.
     *
     * @param toAdd the data entry to add
     */
    private void addEntry(final CacheEntry toAdd) {
        synchronized (lock) {
            CacheEntry existingEntry = this.getCacheEntry(toAdd.getKey());

            if (existingEntry != null) {
                this.removeEntry(existingEntry, false);
            }
            if (this.entries.size() >= this.maxSize) {
                //Delete the least recently used entry.
                this.removeEntry(this.end.previous(), false);
            }
            if (toAdd.hasTimeout()) {
                CacheBucket bucket = this.expiryKeys.get(toAdd.getTimeout());

                if (bucket == null) {
                    bucket = new CacheBucket(toAdd);
                    this.expiryKeys.put(toAdd.getTimeout(), bucket);
                } else {
                    bucket.addEntry(toAdd);
                }
            }
            this.insertHead(toAdd);
            this.entries.put(toAdd.getKey(), toAdd);
        }
    }

    /**
     * Removes the given CacheEntry from cache and from storage if removeFromStorage is true.
     *
     * Removal needs to be sync directly to storage for consistency unless it is just LRU eviction.
     *
     * @param entry the entry to remove
     */
    private boolean removeEntry(final CacheEntry entry, final boolean removeFromStorage) {
        synchronized (lock) {
            if (entry.hasTimeout()) {
                CacheBucket bucket = this.expiryKeys.get(entry.getTimeout());
                if (bucket != null) {
                    bucket.removeEntry(entry);
                }
            }
            this.entries.remove(entry.getKey());
            entry.previous().next(entry.next());
            entry.next().previous(entry.previous());
            if (removeFromStorage) {
                this.storage.remove(null, entry.getKey());
                //We can't rely on the return value of storage.remove() here because the stored value
                //Might not by in sync with the backing storage...
            }
            return true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * To be consistent, expiration of entries should also happen passively on data retrieval.
     */
    @Override
    public byte[] get(final byte[] key) {
        CacheEntry entry = this.getEntry(key);

        if (entry == null) {
            return null;
        }
        if (entry.hasExpired()) {
            removeEntry(entry, true);
            return null;
        }
        return entry.getApplicationData();
    }

//    @Override
//    public byte[][] getBulk(final byte[]... keys) {
//        byte[][] values = new byte[keys.length][];
//
//        for (int i = 0; i < keys.length; ++i) {
//            values[i] = this.get(keys[i]);
//        }
//        return values;
//    }
    /** {@inheritDoc} */
    @Override
    public boolean store(final MetaTx tx, final byte[] key, final byte[] value) {
        return this.store(key, value, CacheEntry.ZERO_TIMEOUT);
    }

    /** {@inheritDoc} */
    @Override
    public boolean store(final byte[] key, final byte[] value, final long ttl) {
        CacheEntry entry = new CacheEntry(key, value, ttl);

        this.addEntry(entry);
        return true;
    }

//    @Override
//    public long storeBulk(final byte[][] keys, final byte[][] values) {
//        for (int i = 0; i < keys.length; ++i) {
//            this.store(keys[i], values[i]);
//        }
//        return keys.length;
//    }
//    public long storeBulk(final byte[][] keys, final byte[][] values, final long timeout) {
//        for (int i = 0; i < keys.length; ++i) {
//            this.store(keys[i], values[i], timeout);
//        }
//        return keys.length;
//    }
    /** {@inheritDoc} */
    @Override
    public boolean remove(final MetaTx tx, final byte[] key) {
        CacheEntry entry = this.getEntry(key);

        if (entry == null) {
            return true;
        }
        return this.removeEntry(entry, true);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] pop(final MetaTx tx, final byte[] key) {
        CacheEntry entry = this.getEntry(key);

        if (entry == null) {
            return null;
        }
        this.removeEntry(entry, true);
        return entry.getApplicationData();
    }

    /** {@inheritDoc} */
    @Override
    public MetaTx begin() {
        return this.storage.begin();
    }

    /** {@inheritDoc} */
    @Override
    public boolean commit(final MetaTx tx) {
        return this.storage.commit(tx);
    }

    /** {@inheritDoc} */
    @Override
    public boolean rollback(final MetaTx tx) {
        return this.storage.rollback(tx);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.flushToStorage();
        this.clear();
        this.storage.close();
    }

    /** {@inheritDoc} */
    @Override
    public void removeExpiredEntries() {
        synchronized (lock) {
            Map<Long, CacheBucket> toRemove = this.expiryKeys.headMap(System.currentTimeMillis());

            for (Iterator<CacheBucket> it = toRemove.values().iterator(); it.hasNext();) {
                CacheBucket bucket = it.next();
                for (byte[] key : bucket.getKeys()) {
                    this.remove(null, key);
                }
                it.remove();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void flushToStorage() {
        synchronized (lock) {
            ByteBuffer buf = ByteBuffer.allocate(this.expiryKeys.size() * Long.BYTES);

            for (Map.Entry<Long, CacheBucket> mapEntry : this.expiryKeys.entrySet()) {
                byte[] longKey = SerializationUtils.longToBytes(mapEntry.getKey());
                buf.put(longKey);
                this.storage.store(null, longKey, mapEntry.getValue().serialize());
            }
            for (CacheEntry entry : this.entries.values()) {
                this.storage.store(null, entry.getKey(), entry.getData());
            }
            this.storage.store(null, KEY, buf.array());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        synchronized (lock) {
            this.flushToStorage();
            this.entries.clear();
            this.expiryKeys.clear();
            this.start.next(end);
            this.end.previous(start);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        return this.storage.count();
    }

    /** {@inheritDoc} */
    @Override
    public KVStorage getStorage() {
        return this.storage;
    }

    /**
     * <p>getCollection</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param serializer a {@link org.meta.api.storage.Serializer} object.
     * @param <T> a T object.
     * @return a {@link org.meta.api.storage.CollectionStorage} object.
     */
    public <T> CollectionStorage<T> getCollection(String name, Serializer<T> serializer) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseName() {
        return null;
    }
}
