/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.tests.storage;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.storage.MetaStorage;
import org.meta.configuration.ModelConfigurationImpl;
import org.meta.storage.KyotoCabinetStorage;
import org.meta.storage.MetaCacheStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.utils.SerializationUtils;

/**
 *
 * @author dyslesiq
 */
public class CacheBenchmarkTest {

    private static MetaStorage kyotoStorage;
    private static MetaStorage backingStorage;
    private static MetaCacheStorage cacheStorage;

    private static int MAX_ENTRIES = 1000000;

    private static MetaStorage getKyotoStorage() throws IOException, StorageException {
        ModelConfiguration config = new ModelConfigurationImpl();
        config.setDatabasePath(File.createTempFile(Long.toString(System.currentTimeMillis())
                + "-CacheBenchmarkTest", ".kch").getAbsolutePath());
        return new KyotoCabinetStorage(config);
    }

    public static void setupCache() {
        try {
            backingStorage = getKyotoStorage();
            cacheStorage = new MetaCacheStorage(backingStorage, MAX_ENTRIES);
        } catch (IOException ex) {
            Assert.fail("Failed to create temporary file for kyoto cabinet (for cache) storage tests.");
        } catch (StorageException ex) {
            Assert.fail("Failed to create kyoto cabinet (for cache) storage.");
        }
    }

    /**
     * Just inserts records in the given storage.
     *
     * @param storage
     * @param records
     */
    static void insertRecords(MetaStorage storage, int records) {
        for (int i = 0; i < records; ++i) {
            byte[] data = SerializationUtils.intToBytes(i);
            Assert.assertTrue(storage.store(data, data));
        }
    }

    static void randomRead(MetaStorage storage, int records) {
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
    @Test
    public void kindCacheBenchmark() throws IOException, StorageException {
        kyotoStorage = getKyotoStorage();
        Long startTime = System.currentTimeMillis();
        insertRecords(kyotoStorage, MAX_ENTRIES);
        randomRead(kyotoStorage, MAX_ENTRIES);
        Long kyotoTime = System.currentTimeMillis() - startTime;
        System.out.println("Took " + kyotoTime + "ms to store and read values to kyoto storage.");
        kyotoStorage.close();
        setupCache();
        startTime = System.currentTimeMillis();
        insertRecords(cacheStorage, MAX_ENTRIES);
        randomRead(cacheStorage, MAX_ENTRIES);
        cacheStorage.flushToStorage();
        Long cacheTime = System.currentTimeMillis() - startTime;
        System.out.println("Took " + cacheTime + "ms to store and read values to cache storage.");
    }

}
