package org.meta.api.storage;

/**
 *
 * Interface describing memory cache functionalities over MetaStorage.
 *
 * @author dyslesiq
 */
public interface MetaCache extends MetaStorage {

    /**
     * Store the given value under key in the backing database.
     *
     * The entry is also associated with a timeout value, after which it will be considered invalid.
     *
     * @param key the key under which the data will be stored
     * @param value the value
     * @param timeout the number of ms after which the entry will timeout
     * @return true on success, false if unable to store
     */
    boolean store(final byte[] key, final byte[] value, final long timeout);

    /**
     * Store multiple values under the given keys in the backing database.
     *
     * Entries are also associated with a timeout value, after which they will be considered invalid.
     *
     * @param keys the keys under which the values will be stored
     * @param values the values
     * @param timeout the number of ms after which the entries will timeout
     * @return the number of entries actually stored
     */
    //long storeBulk(final byte[][] keys, final byte[][] values, final long timeout);
    /**
     * Clear the content of this cache.
     */
    void clear();

    /**
     * Remove expired entries from database and in-memory cache.
     */
    void removeExpiredEntries();

    /**
     * Force the in-memory data (if any) to be written to the backing storage.
     */
    void flushToStorage();

}
