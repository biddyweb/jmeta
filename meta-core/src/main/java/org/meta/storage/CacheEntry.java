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

/**
 * Class representing raw data with associated expiration timestamp.
 *
 * Used by the MetaCacheStorage.
 */
public class CacheEntry {

    /**
     * Constant value for storage entries with no expiry.
     */
    public static final Long ZERO_TIMEOUT = 0L;

    private final byte[] key;
    private byte[] applicationData;
    private byte[] data;
    private long timeout;
    private CacheEntry previous;
    private CacheEntry next;

    /**
     * Creates a DataEntry for insertion in database, appData not including timeout value.
     *
     * @param dataKey the data key
     * @param appData the raw data
     * @param ttl the time to live of the entry (in milli-seconds)
     */
    public CacheEntry(final byte[] dataKey, final byte[] appData, final long ttl) {
        this.key = dataKey;
        this.applicationData = appData;
        if (ttl != ZERO_TIMEOUT) {
            this.timeout = System.currentTimeMillis() + ttl;
        } else {
            this.timeout = ZERO_TIMEOUT;
        }
        this.serialize();
    }

    /**
     * Creates a DataEntry from data from storage layer, data including timeout value.
     *
     * @param dataKey the entry's key
     * @param entryData the data including timeout value
     */
    CacheEntry(final byte[] dataKey, final byte[] entryData) {
        this.data = entryData;
        this.key = dataKey;
        this.deSerialize();
    }

    /**
     * Empty constructor.
     */
    CacheEntry() {
        key = null;
    }

    /**
     * Extract timeout and application data from data.
     */
    private void deSerialize() {
        ByteBuffer timeoutBuf = ByteBuffer.wrap(this.data, 0, Long.BYTES);
        this.timeout = timeoutBuf.getLong();
        this.applicationData = new byte[this.data.length - Long.BYTES];
        for (int i = Long.BYTES; i < this.data.length; ++i) {
            this.applicationData[i - Long.BYTES] = this.data[i];
        }
    }

    /**
     * Serialize timeout and application data to data.
     *
     * @return
     */
    private void serialize() {
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES + this.applicationData.length);
        buf.putLong(this.timeout);
        buf.put(this.applicationData);
        this.data = new byte[buf.limit()];
        buf.rewind();
        buf.get(data);
    }

    /**
     * @return true if the entry has expired, false otherwise
     */
    public boolean hasExpired() {
        return hasTimeout() && timeout < System.currentTimeMillis();
    }

    /**
     *
     * @return the data of this entry (the raw application data with timeout value)
     */
    public byte[] getData() {
        return data;
    }

    /**
     *
     * @return the expiration timestamp of this entry
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     *
     * @return the raw application data (without timeout value)
     */
    public byte[] getApplicationData() {
        return this.applicationData;
    }

    /**
     *
     * @return the data key
     */
    public byte[] getKey() {
        return key;
    }

    /**
     *
     * @return true if this entry has a timeout information (ttl != 0)
     */
    public boolean hasTimeout() {
        return timeout != ZERO_TIMEOUT;
    }

    /**
     *
     * @return the previous item in the list
     */
    public CacheEntry previous() {
        return previous;
    }

    /**
     *
     * @param prev the previous item in the list
     */
    public void previous(final CacheEntry prev) {
        this.previous = prev;
    }

    /**
     *
     * @return the next item in the list
     */
    public CacheEntry next() {
        return next;
    }

    /**
     *
     * @param n the next item in the list
     */
    public void next(final CacheEntry n) {
        this.next = n;
    }
}
