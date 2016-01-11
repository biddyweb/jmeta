/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.tests.storage;

import java.io.IOException;
import java.util.Random;
import org.junit.Assert;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.storage.KVStorage;
import org.meta.api.storage.MetaCache;
import org.meta.storage.MetaCacheStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;
import org.meta.utils.SerializationUtils;

/**
 *
 * @author dyslesiq
 */
public class CacheBenchmarkTestDisabled extends MetaBaseTests {

    private static KVStorage backingStorage;
    private static MetaCacheStorage cacheStorage;

    private static int MAX_ENTRIES = 1000000;

    public static void setupCache(final int maxEntries) {
//        try {
//            backingStorage = getDatabase(CacheBenchmarkTestDisabled.class.getName());
//            cacheStorage = new MetaCacheStorage(backingStorage, maxEntries);
//        } catch (IOException ex) {
//            Assert.fail("Failed to create temporary file for cabinet (for cache) storage tests.");
//        } catch (StorageException ex) {
//            Assert.fail("Failed to create kyoto cabinet (for cache) storage.");
//        }
    }

    /**
     * Just inserts records in the given storage.
     *
     * @param storage
     * @param records
     */
    static void insertRecords(KVStorage storage, int records) {
        for (int i = 0; i < records; ++i) {
            byte[] data = SerializationUtils.intToBytes(i);
            Assert.assertTrue(storage.store(null, data, data));
        }
    }

    static void insertHashes(KVStorage storage, int nbHashes) {
        for (int i = 0; i < nbHashes; ++i) {
            byte[] data = MetamphetUtils.createRandomHash().toByteArray();
            Assert.assertTrue(storage.store(null, data, data));
        }
    }

    static void insertHashesTimeout(MetaCache cache, int nbHashes, int timeout) {
        for (int i = 0; i < nbHashes; ++i) {
            byte[] data = MetamphetUtils.createRandomHash().toByteArray();
            Assert.assertTrue(cache.store(data, data, timeout));
        }
    }

    static void randomRead(KVStorage storage, int records) {
        Random random = new Random();

        for (int i = 0; i < 2 * records; ++i) {
            int k = random.nextInt(records + 1);
            byte[] data = SerializationUtils.intToBytes(k);
            storage.get(data);
        }
    }

    /**
     * Inserts records to the maximum capacity of the cache.
     *
     * This way the cache should provide maximum performance.
     */
    //@Test
    public void kindCacheBenchmark() throws IOException, StorageException {
        //backingStorage = getDatabase(CacheBenchmarkTestDisabled.class.getName());
        Long startTime = System.currentTimeMillis();
        insertRecords(backingStorage, MAX_ENTRIES);
        randomRead(backingStorage, MAX_ENTRIES);
        Long kyotoTime = System.currentTimeMillis() - startTime;
        System.out.println("Took " + kyotoTime + "ms to store and read " + MAX_ENTRIES + " incremental values to kyoto storage.");
        System.out.println("Number of acutally stored entries: " + backingStorage.count());
        backingStorage.close();
        setupCache(MAX_ENTRIES);
        startTime = System.currentTimeMillis();
        insertRecords(cacheStorage, MAX_ENTRIES);
        randomRead(cacheStorage, MAX_ENTRIES);

        Long cacheTime = System.currentTimeMillis() - startTime;
        System.out.println("Took " + cacheTime + "ms to store and read " + MAX_ENTRIES + " incremental values to cache storage.");
        System.out.println("Number of acutally stored entries (before sync): " + cacheStorage.count());
        cacheStorage.flushToStorage();
        System.out.println("Number of acutally stored entries (after sync): " + cacheStorage.count());
    }

    /**
     * Inserts twice as much hashes as the cache maximum entries.
     *
     * @throws IOException
     *
     * @throws StorageException
     */
    //@Test
    public void benchWriteTwiceHashes() throws IOException, StorageException {
        int nbEntries = 100000;
        //backingStorage = getDatabase(CacheBenchmarkTestDisabled.class.getName());
        Long startTime = System.currentTimeMillis();
        insertHashes(backingStorage, nbEntries);
        Long kyotoTime = System.currentTimeMillis() - startTime;
        System.out.println("Took " + kyotoTime + "ms to store and read " + nbEntries + " hash values to storage.");
        System.out.println("Number of acutally stored entries: " + backingStorage.count());
        backingStorage.close();
        setupCache(nbEntries / 2);
        startTime = System.currentTimeMillis();
        insertHashes(cacheStorage, nbEntries);
        Long cacheTime = System.currentTimeMillis() - startTime;
        System.out.println("Took " + cacheTime + "ms to store and read " + nbEntries + " hash values to cache storage.");
        System.out.println("benchWriteTwiceHashesTimeout Number of acutally stored entries (before sync): " + cacheStorage.count());
        cacheStorage.flushToStorage();
        System.out.println("benchWriteTwiceHashesTimeout Number of acutally stored entries (after sync): " + cacheStorage.count());
    }

    /**
     * Inserts twice as much hashes as the cache maximum entries with timeout.
     *
     * @throws IOException
     *
     * @throws StorageException
     */
    //@Test
    public void benchWriteTwiceHashesTimeout() throws IOException, StorageException {
        int nbEntries = 100000;
        //backingStorage = getDatabase(CacheBenchmarkTestDisabled.class.getName());
        Long startTime = System.currentTimeMillis();
        insertHashes(backingStorage, nbEntries);
        Long kyotoTime = System.currentTimeMillis() - startTime;
        System.out.println("benchWriteTwiceHashesTimeout Took " + kyotoTime + "ms to store " + nbEntries + " hash values to storage.");
        System.out.println("benchWriteTwiceHashesTimeout Number of acutally stored hashes: " + backingStorage.count());
        backingStorage.close();
        setupCache(nbEntries / 2);
        startTime = System.currentTimeMillis();
        insertHashesTimeout(cacheStorage, nbEntries, 1000);
        //cacheStorage.flushToStorage();
        Long cacheTime = System.currentTimeMillis() - startTime;
        System.out.println("benchWriteTwiceHashesTimeout Took " + cacheTime + "ms to store and read " + nbEntries + " hash values to cache storage.");
        System.out.println("benchWriteTwiceHashesTimeout Number of acutally stored entries (before sync): " + cacheStorage.count());
        Long flushTime = System.currentTimeMillis();
        cacheStorage.flushToStorage();
        flushTime = System.currentTimeMillis() - flushTime;
        System.out.println("flush time = " + flushTime + "ms");
        System.out.println("benchWriteTwiceHashesTimeout Number of acutally stored entries (after sync): " + cacheStorage.count());
    }

}
