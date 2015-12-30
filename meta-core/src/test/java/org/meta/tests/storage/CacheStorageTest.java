/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.tests.storage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.storage.MetaCache;
import org.meta.api.storage.MetaStorage;
import org.meta.configuration.ModelConfigurationImpl;
import org.meta.storage.KyotoCabinetStorage;
import org.meta.storage.MapDbStorage;
import org.meta.storage.MetaCacheStorage;
import org.meta.storage.exceptions.StorageException;

/**
 *
 * @author dyslesiq
 */
public class CacheStorageTest {

    private static MetaStorage backingStorage;

    private static MetaCacheStorage storage;

    private static MetaStorage getStorage(final String dbFile) throws IOException, StorageException {
        ModelConfiguration config = new ModelConfigurationImpl();
        config.setDatabasePath(dbFile);
        return new MapDbStorage(config);
    }

    private static MetaStorage getStorage() throws IOException, StorageException {
        return getStorage(File.createTempFile(Long.toString(System.currentTimeMillis())
                + "-CacheBenchmarkTest", ".kch").getAbsolutePath());
    }

    /**
     *
     */
    @BeforeClass
    public static void setup() {
        try {
            ModelConfiguration config = new ModelConfigurationImpl();
            config.setDatabasePath(File.createTempFile(Long.toString(System.currentTimeMillis())
                    + "-CacheStorageTests", ".kch").getAbsolutePath());
            backingStorage = new KyotoCabinetStorage(config);
            storage = new MetaCacheStorage(backingStorage, 500);
        } catch (StorageException ex) {
            Logger.getLogger(BaseStorageTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Failed to initialize kyoto cabinet for cache storage tests");
        } catch (IOException ex) {
            Assert.fail("Failed to create temporary file for kyoto cabinet (for cache) storage tests.");
        }
    }

    /**
     *
     */
    @Test
    public void basicTest() {
        byte[] key = "basicTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key, value));
        Assert.assertArrayEquals(value, storage.get(key));
    }

    /**
     *
     */
    @Test
    public void updateTest() {
        byte[] key = "updateTest".getBytes();
        byte[] value = "value".getBytes();
        byte[] newVal = "newVal".getBytes();

        Assert.assertTrue(storage.store(key, value));
        Assert.assertTrue(storage.store(key, newVal));
        Assert.assertArrayEquals(newVal, storage.get(key));
    }

    /**
     *
     */
    @Test
    public void removalTest() {
        byte[] key = "removalTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key, value));
        Assert.assertTrue(storage.remove(key));
        Assert.assertNull(storage.get(key));
    }

    /**
     *
     */
//    @Test
//    public void bulkTest() {
//        byte[][] keys = {"1".getBytes(), "2".getBytes(), "3".getBytes()};
//        byte[][] values = {"1".getBytes(), "2".getBytes(), "3".getBytes()};
//
//        Assert.assertEquals(keys.length, storage.storeBulk(keys, values));
//        byte[][] storageValues = storage.getBulk(keys);
//        Assert.assertNotNull(storageValues);
//        Assert.assertEquals(keys.length, storageValues.length);
//        for (int i = 0; i < keys.length; ++i) {
//            Assert.assertArrayEquals(values[i], storageValues[i]);
//        }
//    }
    /**
     *
     * @throws InterruptedException t
     */
    @Test
    public void expirationTest() throws InterruptedException {
        byte[] key = "expirationTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key, value, 100L));
        Thread.sleep(120L);
        Assert.assertNull(storage.get(key));
    }

    /**
     *
     * @throws InterruptedException t
     */
    @Test
    public void expirationChangeTest() throws InterruptedException {
        byte[] key = "expirationChangeTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key, value, 100L));
        Assert.assertTrue(storage.store(key, value, 500L));
        Thread.sleep(120L);
        Assert.assertNotNull(storage.get(key));
        Thread.sleep(500L);
        Assert.assertNull(storage.get(key));
    }

    /**
     *
     * @throws InterruptedException t
     */
    @Test
    public void expirationCancellationTest() throws InterruptedException {
        byte[] key = "expirationCancellationTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key, value, 100L));
        Assert.assertTrue(storage.store(key, value));
        Thread.sleep(120L);
        Assert.assertNotNull(storage.get(key));
    }

    /**
     *
     * @throws InterruptedException t
     */
    @Test
    public void expirationSyncTest() throws InterruptedException {
        byte[] key = "expirationSyncTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key, value, 100L));
        storage.flushToStorage();
        Thread.sleep(120L);
        Assert.assertNull(storage.get(key));

        Assert.assertTrue(storage.store(key, value, 100L));
        storage.flushToStorage();
        Assert.assertNotNull(storage.get(key));
    }

    /**
     *
     * @throws InterruptedException
     */
    //@Test
    public void removeExpiredEntriesTest() throws InterruptedException {
        byte[] key1 = "removeExpiredEntriesTest1".getBytes();
        byte[] key2 = "removeExpiredEntriesTest2".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key1, value, 100L));
        Assert.assertTrue(storage.store(key2, value, 100L));
        Thread.sleep(120L);
        storage.removeExpiredEntries();
        Assert.assertNull(storage.get(key1));
        Assert.assertNull(storage.get(key2));
    }

    /**
     *
     */
    @Test
    public void syncToStorageTest() throws IOException, StorageException, InterruptedException {
        String dbPath = File.createTempFile("syncToStorageTest", ".kch")
                .getAbsolutePath();
        MetaStorage testSyncStorage = getStorage(dbPath);
        MetaCache cacheStorage = new MetaCacheStorage(testSyncStorage, 500);
        byte[] key = "syncToStorageTest".getBytes();
        byte[] key1 = "syncToStorageTestExpire".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(cacheStorage.store(key, value));
        Assert.assertTrue(cacheStorage.store(key1, value, 1000));
        Assert.assertArrayEquals(value, cacheStorage.get(key));
        cacheStorage.flushToStorage();
        cacheStorage.close();
        //Re-create storage layers
        testSyncStorage = getStorage(dbPath);
        cacheStorage = new MetaCacheStorage(testSyncStorage, 500);
        Assert.assertArrayEquals(value, cacheStorage.get(key));
        Assert.assertArrayEquals(value, cacheStorage.get(key1));
        Thread.sleep(1000L);
        Assert.assertNull(cacheStorage.get(key1));
    }

}
