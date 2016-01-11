/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.tests.storage;

import java.io.IOException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.storage.MetaCacheStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;
import org.meta.api.storage.KVStorage;

/**
 *
 * @author dyslesiq
 */
public class CacheStorageTestDisabled extends MetaBaseTests {

    private static KVStorage backingStorage;

    private static MetaCacheStorage storage;

    /**
     *
     */
    @BeforeClass
    public static void setup() {
//        try {
//            backingStorage = new BerkeleyKVStorage(MetaConfiguration.getModelConfiguration());
//            storage = new MetaCacheStorage(backingStorage, 500);
//        } catch (StorageException ex) {
//            Assert.fail("Failed to initialize kyoto cabinet for cache storage tests");
//        }
    }

    /**
     *
     */
    @Test
    public void basicTest() {
        byte[] key = "basicTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(null, key, value));
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

        Assert.assertTrue(storage.store(null, key, value));
        Assert.assertTrue(storage.store(null, key, newVal));
        Assert.assertArrayEquals(newVal, storage.get(key));
    }

    /**
     *
     */
    @Test
    public void removalTest() {
        byte[] key = "removalTest".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(null, key, value));
        Assert.assertTrue(storage.remove(null, key));
        Assert.assertNull(storage.get(key));
    }

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
        Assert.assertTrue(storage.store(null, key, value));
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
//        KVStorage testSyncStorage = getDatabase(CacheStorageTestDisabled.class.getSimpleName());
//        MetaCache cacheStorage = new MetaCacheStorage(testSyncStorage, 500);
//        byte[] key = "syncToStorageTest".getBytes();
//        byte[] key1 = "syncToStorageTestExpire".getBytes();
//        byte[] value = "value".getBytes();
//
//        Assert.assertTrue(cacheStorage.store(null, key, value));
//        Assert.assertTrue(cacheStorage.store(key1, value, 1000));
//        Assert.assertArrayEquals(value, cacheStorage.get(key));
//        cacheStorage.flushToStorage();
//        cacheStorage.close();
//        //Re-create storage layers
//        testSyncStorage = getDatabase(CacheStorageTestDisabled.class.getSimpleName());
//        cacheStorage = new MetaCacheStorage(testSyncStorage, 500);
//        Assert.assertArrayEquals(value, cacheStorage.get(key));
//        Assert.assertArrayEquals(value, cacheStorage.get(key1));
//        Thread.sleep(1000L);
//        Assert.assertNull(cacheStorage.get(key1));
    }

}
