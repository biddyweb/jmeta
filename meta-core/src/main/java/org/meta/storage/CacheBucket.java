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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import org.meta.utils.SerializationUtils;

/**
 * Class representing a list of timeout entries for a given expiration timestamp.
 *
 * Can be serialized and de-serialized.
 *
 * Used by the MetaCacheStorage.
 *
 * @author dyslesiq
 */
public class CacheBucket {

    private final Set<byte[]> keys = new HashSet<>();

    /**
     * Creates a bucket from the given serialized data.
     *
     * @param bucketData the serialized form
     */
    public CacheBucket(final byte[] bucketData) {
        this.deserialize(bucketData);
    }

    /**
     * Creates an empty bucket.
     *
     */
    public CacheBucket() {
    }

    /**
     * Creates a new bucket with the given initial entry.
     *
     * @param entry the initial entry
     */
    CacheBucket(final CacheEntry entry) {
        this.keys.add(entry.getKey());
    }

    /**
     * Serialize this bucket for storage in database.
     *
     * @return the serialized data
     */
    public byte[] serialize() {
        //Magical best effort prediction of final serialized size
        ByteArrayOutputStream buf = new ByteArrayOutputStream(Long.BYTES * 2 + keys.size() * 24);

        buf.write(SerializationUtils.intToBytes(keys.size()), 0, Integer.BYTES);
        for (byte[] entry : keys) {
            buf.write(SerializationUtils.intToBytes(entry.length), 0, Integer.BYTES);
            buf.write(entry, 0, entry.length);
        }
        return buf.toByteArray();
    }

    private void deserialize(final byte[] bucketData) {
        ByteBuffer buf = ByteBuffer.wrap(bucketData);
        int nbEntries = buf.getInt();
        for (int i = 0; i < nbEntries; ++i) {
            int keySize = buf.getInt();
            byte[] entryKey = new byte[keySize];
            buf.get(entryKey);
            keys.add(entryKey);
        }
    }

    /**
     *
     * @param entry the entry to check for
     * @return true if this bucket contains the given entry, false otherwise
     */
    public boolean contains(final CacheEntry entry) {
        return this.keys.contains(entry.getKey());
    }

    /**
     *
     * @param entry the new CacheEntry to add
     */
    public synchronized void addEntry(final CacheEntry entry) {
        keys.add(entry.getKey());
    }

    /**
     *
     * @param entry the CacheEntry to remove
     */
    public synchronized void removeEntry(final CacheEntry entry) {
        this.keys.remove(entry.getData());
    }

    /**
     *
     * @return the set of keys
     */
    public Set<byte[]> getKeys() {
        return this.keys;
    }

}
