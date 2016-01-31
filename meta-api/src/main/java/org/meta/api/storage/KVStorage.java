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
 * Base interface for Key/Value storage routines.
 *
 * Implementations should be thread-safe and respect ACID.
 *
 * Only raw keys and values are handled here, no application-specific behavior should appear.
 *
 * Note about transactions: they are used to ensure consistency when concurrently accessing the same records.
 *
 * Explicit transactions should also be used to maximize performances. By default, all methods that modifies
 * records should use auto-commit. Meaning that each time a record is modified, the changes are reflected
 * directly on the disk. Explicitely calling {@link KVStorage#begin()} and {@link KVStorage#commit(MetaTx)}
 * allows to control the volume of data that would be written to disk on commit().
 *
 * @author dyslesiq
 * @version $Id: $
 */
public interface KVStorage {

    /**
     * <p>getDatabaseName</p>
     *
     * @return the name of the database this CollectionStorage is bond to
     */
    String getDatabaseName();

    /**
     * Retrieves the value associated to the given key in the backing database.
     *
     * @param key the key to get
     * @return data associated to the key or null if not found.
     */
    byte[] get(final byte[] key);

    /**
     * Stores the given value under the key in the backing database.
     *
     * Existing value will be overwritten.
     *
     * @param tx the transaction to use. Can be null, in which case the operation will be performed using
     * auto-commit.
     * @param key the key under which the data will be stored
     * @param value the value. Cannot be null.
     * @return true on success, false if unable to store
     */
    boolean store(final MetaTx tx, final byte[] key, final byte[] value);

    /**
     * Removes the given key from the backing database.
     *
     * @param tx the transaction to use. Can be null, in which case the operation will be performed using
     * auto-commit.
     * @param key the key to remove
     * @return true on success, false if unable to remove
     */
    boolean remove(final MetaTx tx, final byte[] key);

    /**
     * Retrieves and removes the given key from the backing database.
     *
     * @param tx the transaction to use. Can be null, in which case the operation will be performed using
     * auto-commit.
     * @param key the key to get and remove
     * @return the associated data, or null if not found
     */
    byte[] pop(final MetaTx tx, final byte[] key);

    /**
     * <p>count</p>
     *
     * @return the number of entries stored in this storage. Can be a very costly operation!
     */
    long count();

    /**
     * Begins a transaction.
     *
     * @return the new MetaTx object if the transaction was successfully started, null otherwise
     */
    MetaTx begin();

    /**
     * Commit the given transaction.
     *
     * @param tx the transaction to commit. Can be null, in which case the operation will have no effect.
     * @return true on success, false if the transaction is invalid or if failed to commit
     */
    boolean commit(final MetaTx tx);

    /**
     * Rollback the given transaction.
     *
     * @param tx the transaction to rollback. Can be null, in which case the operation will have no effect.
     * @return true on success, false if the transaction is invalid or if failed to rollback
     */
    boolean rollback(final MetaTx tx);

    /**
     * Close properly the underlying database.
     */
    void close();

}
