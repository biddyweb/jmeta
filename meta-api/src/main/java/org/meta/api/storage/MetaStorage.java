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
 * Base interface for storage routines. A bucket of timeout entries has a 10 minutes span. (in milli-seconds)
 * Implementations should be thread-safe and respect ACID as much as possible.
 *
 * Only raw keys and values are handled here, no application-specific behavior should appear.
 *
 * @author dyslesiq
 */
public interface MetaStorage {

    /**
     * Retrieve the value associated to the given key in the backing database.
     *
     * @param key the key to get
     *
     * @return data associated to the key or null if not found.
     */
    byte[] get(final byte[] key);

    /**
     * Retrieve multiple values associated to the given key.
     *
     * Invalid keys are returned as null in the values array.
     *
     * @param keys th keys to get
     * @return the array of values
     */
    //byte[][] getBulk(final byte[]... keys);
    /**
     * Store the given value under the key in the backing database.
     *
     * @param key the key under which the data will be stored
     * @param value the value
     * @return true on success, false if unable to store
     */
    boolean store(final byte[] key, final byte[] value);

    /**
     * Store multiple values under the given keys in the backing database.
     *
     * @param keys the keys under which the values will be stored
     * @param values the values
     * @return the number of entries actually stored
     */
    //long storeBulk(final byte[][] keys, final byte[][] values);
    /**
     * Remove the given key from the backing database.
     *
     * @param key the key to remove
     * @return true on success, false if unable to remove
     */
    boolean remove(final byte[] key);

    /**
     * Remove multiple keys from the backing database.
     *
     * @param keys the keys to remove
     * @return the number of entries actually removed
     */
    long removeBulk(final byte[]... keys);

    /**
     * Retrieve and Remove the given key from the backing database.
     *
     * @param key the key to get and remove
     * @return the associated data, or null if not found
     */
    byte[] pop(final byte[] key);

    /**
     *
     * @return the number of entries stored in this storage.
     */
    long count();

    /**
     * Begin a transaction.
     *
     * @return true if the transaction was successfully started, false otherwise
     */
    boolean begin();

    /**
     * Commit the current transaction.
     *
     * @return true on success, false if there is no pending transaction or if failed to commit
     */
    boolean commit();

    /**
     * Rollback the current transaction.
     *
     * @return true on success, false if there is no pending transaction or if failed to rollback
     */
    boolean rollback();

    /**
     * Close properly the underlying database.
     */
    void close();

}
