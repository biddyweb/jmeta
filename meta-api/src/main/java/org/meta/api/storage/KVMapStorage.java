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
package org.meta.api.storage;

/**
 * Convenience interface that provides Map-like operations of Key and Value objects based on
 * {@link KVStorage}.
 *
 * @author dyslesiq
 * @param <K> the key type
 * @param <V> the value type
 * @version $Id: $
 */
public interface KVMapStorage<K, V> {

    /**
     * <p>
     * getKVStorage.</p>
     *
     * @return the backing byte[] Key/Value storage
     */
    KVStorage getKVStorage();

    /**
     * <p>
     * getKeySerializer.</p>
     *
     * @return the key type serializer
     */
    Serializer<K> getKeySerializer();

    /**
     * <p>
     * getValueSerializer.</p>
     *
     * @return the value type serializer
     */
    Serializer<V> getValueSerializer();

    /**
     * Get the value associated to the given key from the storage.
     *
     * @param key the key to retrieve
     * @return the value, or null if not found
     */
    V get(K key);

    /**
     * Put a new or existing record in this storage.
     *
     * @param tx the transaction to use. Can be null, in which case the operation will be performed using
     * auto-commit.
     * @param key the key to put
     * @param value the value
     * @return true on success, false otherwise
     */
    boolean put(MetaTx tx, K key, V value);

    /**
     * Removes the key from this storage.
     *
     * @param tx the transaction to use. Can be null, in which case the operation will be performed using
     * auto-commit.
     * @param key the key to remove
     * @return true if deleted or not found, false on error
     */
    boolean remove(MetaTx tx, K key);

    /**
     * Begins a transaction.
     *
     * Once a transaction is opened, it MUST be closed either by calling {@link #commit(MetaTx)} or
     * {@link #rollback(MetaTx)}, otherwise the behavior of following transactions is undefined.
     *
     * Calling {@link #begin()} twice without closing the transaction first will result in a RuntimeException.
     *
     * @return true if the transaction was created successfully, false otherwise
     */
    MetaTx begin();

    /**
     * Commit pending changes to the underlying storage.
     *
     * The behavior is provider-specific but implementations MUST ensure ACID.
     *
     * @param tx the transaction to commit
     * @return true if the transaction has commit successfully, false otherwise
     */
    boolean commit(MetaTx tx);

    /**
     * Rollback pending changes.
     *
     * The behavior is provider-specific but implementations MUST ensure ACID.
     *
     * @param tx the transaction to rollback
     * @return true if the transaction has rollback successfully, false otherwise
     */
    boolean rollback(MetaTx tx);

}
