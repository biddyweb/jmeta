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
package org.meta.dht.tomp2p.storage;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import net.tomp2p.dht.Storage;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.KVMapStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.api.storage.Serializer;
import org.meta.storage.comparators.Comparators;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TomP2P specific storage implementation.
 *
 * We support only a partial set of operation provided by Tomp2p.
 *
 * Expiration of entries is improved: we support passive expiration of entries on retrieval.
 *
 * (if get() is called for an expired entry, it is discarded directly instead of waiting for the expiration
 * checker to come).
 *
 * @author dyslesiq
 */
public class TomP2pStorage implements Storage {

    private final Logger logger = LoggerFactory.getLogger(TomP2pStorage.class);

    /**
     * The maximum number of mili-seconds a record is considered valid on the DHT.
     */
    private static final int MAX_TTL = 3600000;

    private static final String DATA_STORE_NAME = "tomp2Data";

    private static final String EXPIRATION_NAME = "tomp2pExpiration";

    private final MetaDatabase db;

    private final Number640Comparator keyComparator = new Number640Comparator();

    private final TomP2pDataSerializer dataSerializer = new TomP2pDataSerializer();

    private final TimeoutEntrySerializer timeoutSerializer = new TimeoutEntrySerializer();

    private final CollectionStorage<Number640> sortedKeys;

    private final KVMapStorage<Number640, Data> dataMap;

    private final CollectionStorage<TimeoutEntry> sortedTimeout;

    /**
     * @param dataBase the database to get storage facilities from
     */
    public TomP2pStorage(final MetaDatabase dataBase) {
        this.db = dataBase;
        this.dataMap = this.db.getKVMapStorage(DATA_STORE_NAME, Number640Serializer.INSTANCE, dataSerializer,
                keyComparator);
        this.sortedKeys = this.db.getCollection(DATA_STORE_NAME, Number640Serializer.INSTANCE, keyComparator);
        this.sortedTimeout = this.db.getCollection(EXPIRATION_NAME, timeoutSerializer, Comparators.LONG);
    }

    @Override
    public Data put(final Number640 key, final Data value) {
        logger.info("PUT " + key + ":" + value);
        //Data oldData = get(key);
        if (value.expirationMillis() <= System.currentTimeMillis() + MAX_TTL) {
            //Store only entries with valid expiration
            if (!this.dataMap.put(null, key, value)) {
                logger.error("FAILED TO PUT " + key);
            }
        }
        return null;
    }

    /**
     * Check that the given is not expired.
     *
     * If it is, the key is removed and null is returned. Otherwise, the associated Data is returned.
     *
     * Used in case of passive expiration check.
     *
     * @param key the key to check
     * @return the non-expired Data or null
     */
    private Data checkExpiration(final Number640 key) {
        Data d = this.dataMap.get(key);

        if (d == null) {
            return null;
        }
        if (d.expirationMillis() < System.currentTimeMillis()) {
            return remove(key, false);
        }
        return d;
    }

    @Override
    public Data get(final Number640 key) {
        return checkExpiration(key);
    }

    @Override
    public boolean contains(final Number640 key) {
        return get(key) != null;
    }

    @Override
    public int contains(final Number640 from, final Number640 to) {
        int count = 0;
        SortedSet<Number640> submap = this.sortedKeys.subSet(from, to);
        for (Number640 k : submap) {
            //We call contains for every single keys to check expiration
            if (contains(k)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Data remove(final Number640 key, final boolean returnData) {
        Data data = null;
        if (returnData) {
            data = get(key);
        }
        this.dataMap.remove(null, key);
        return data;
    }

    @Override
    public NavigableMap<Number640, Data> remove(final Number640 from, final Number640 to) {
        NavigableMap<Number640, Data> res = new TreeMap<>();
        SortedSet<Number640> toRemove = this.sortedKeys.subSet(from, to);

        for (Number640 key : toRemove) {
            Data d = get(key);
            if (d != null) {
                res.put(key, d);
            }
        }
        toRemove.clear();
        return res;
    }

    @Override
    public NavigableMap<Number640, Data> subMap(final Number640 from, final Number640 to, final int limit,
            final boolean ascending) {
        NavigableMap<Number640, Data> subMap = new TreeMap<>();
        int count = 0;
        SortedSet<Number640> subKeys = this.sortedKeys.subSet(from, to);

        for (Number640 key : subKeys) {
            Data d = get(key);
            if (d != null) {
                subMap.put(key, d);
                if (++count == limit) {
                    break;
                }
            }
        }
        if (!ascending) {
            return subMap.descendingMap();
        }
        return subMap;
    }

    @Override
    public NavigableMap<Number640, Data> map() {
        throw new UnsupportedOperationException("MAP SHOULD NEVER BE CALLED!");
        //I don't want to implement it because for us it means reading the entire database and put it
        //in memory. Apparently Tomp2p never uses it too...
    }

    @Override
    public void close() {
        logger.debug("CLOSE");
    }

    @Override
    public void addTimeout(final Number640 key, final long expiration) {
        this.sortedTimeout.add(new TimeoutEntry(key, expiration));
    }

    @Override
    public void removeTimeout(final Number640 key) {
        //We don't support removing expiration. We do it implicitely with subMapTimeout() and remove()
    }

    @Override
    public Collection<Number640> subMapTimeout(final long to) {
        TimeoutEntry toEntry = new TimeoutEntry(Number640.ZERO, to);
        SortedSet<TimeoutEntry> expiredEntries = this.sortedTimeout.headSet(toEntry);
        Collection<Number640> expiredKeys = new ArrayList<>(expiredEntries.size());

        for (TimeoutEntry entry : expiredEntries) {
            expiredKeys.add(entry.getKey());
        }
        return expiredKeys;
    }

    @Override
    public int storageCheckIntervalMillis() {
        return 10000;//MAX_TTL; //Check every hour TODO this should match the global parameter of DHT entries TTL
    }

    @Override
    public boolean protectDomain(final Number320 key, final PublicKey publicKey) {
        return false;
    }

    @Override
    public boolean isDomainProtectedByOthers(final Number320 key, final PublicKey publicKey) {
        return false;
    }

    @Override
    public boolean protectEntry(final Number480 key, final PublicKey publicKey) {
        return false;
    }

    @Override
    public boolean isEntryProtectedByOthers(final Number480 key, final PublicKey publicKey) {
        return false;
    }

    @Override
    public Number160 findPeerIDsForResponsibleContent(final Number160 locationKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Number160> findContentForResponsiblePeerID(final Number160 peerID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateResponsibilities(final Number160 locationKey, final Number160 peerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeResponsibility(final Number160 locationKey) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    private class TimeoutEntry {

        private final Long expiration;
        private final Number640 key;

        TimeoutEntry(final Number640 entryKey, final Long exp) {
            this.key = entryKey;
            this.expiration = exp;
        }

        public Long getExpiration() {
            return expiration;
        }

        public Number640 getKey() {
            return key;
        }
    }

    /**
     *
     */
    private class TimeoutEntrySerializer implements Serializer<TimeoutEntry> {

        @Override
        public byte[] serialize(final TimeoutEntry entry) {
            byte[] data = new byte[Long.BYTES + Number640.BYTE_ARRAY_SIZE];

            SerializationUtils.longToBytes(entry.getExpiration(), data, 0);
            Number640Serializer.INSTANCE.serialize(entry.getKey(), data, Long.BYTES);
            return data;
        }

        @Override
        public TimeoutEntry deserialize(final byte[] data) {
            long l = SerializationUtils.bytesToLong(data, 0);
            Number640 nb = Number640Serializer.INSTANCE.deserialize(data, Long.BYTES);

            return new TimeoutEntry(nb, l);
        }

    }

}
