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
import org.meta.configuration.ModelConfigurationImpl;
import org.meta.storage.KyotoCabinetStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;

/**
 *
 * @author dyslesiq
 */
public class KyotoStorageTest extends MetaBaseTests {

    private static KyotoCabinetStorage storage;

    @BeforeClass
    public static void setup() {
        try {
            ModelConfiguration config = new ModelConfigurationImpl();
            config.setDatabasePath(File.createTempFile(Long.toString(System.currentTimeMillis())
                    + "-KyotoStorageTests", ".kch").getAbsolutePath());
            storage = new KyotoCabinetStorage(config);
        } catch (StorageException ex) {
            Logger.getLogger(KyotoStorageTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Failed to initialize kyoto cabinet storage");
        } catch (IOException ex) {
            Assert.fail("Failed to create temporary file for kyoto cabinet storage.");
        }
    }

    /**
     *
     */
    @Test
    public void basicTest() {
        byte[] key = "key".getBytes();
        byte[] value = "value".getBytes();

        Assert.assertTrue(storage.store(key, value));
        Assert.assertArrayEquals(value, storage.get(key));
    }

    /**
     *
     */
    @Test
    public void updateTest() {
        byte[] key = "key".getBytes();
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
        byte[] key = "key".getBytes();
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
//        byte[][] keys = {{0, 1, 2}, {0, 2, 4}, {0, 3, 6}};
//        byte[][] values = {"11".getBytes(), "12".getBytes(), "13".getBytes()};
//
//        //Assert.assertEquals(keys.length, storage.storeBulk(keys, values));
//        byte[][] storageValues = storage.getBulk(keys);
//        Assert.assertNotNull(storageValues);
//        Assert.assertEquals(keys.length, storageValues.length);
//        for (int i = 0; i < keys.length; ++i) {
//            Assert.assertArrayEquals(values[i], storageValues[i]);
//        }
//    }
}
