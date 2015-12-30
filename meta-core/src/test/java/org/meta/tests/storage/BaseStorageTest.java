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
package org.meta.tests.storage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.storage.MetaStorage;
import org.meta.configuration.ModelConfigurationImpl;
import org.meta.storage.MapDbStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;

/**
 *
 * @author dyslesiq
 */
public class BaseStorageTest extends MetaBaseTests {

    private static MetaStorage storage;

    @BeforeClass
    public static void setup() {
        try {
            ModelConfiguration config = new ModelConfigurationImpl();
            config.setDatabasePath(createTempFile("-KyotoStorageTests", 0).getAbsolutePath());
            //storage = new KyotoCabinetStorage(config);
            storage = new MapDbStorage(config);
        } catch (StorageException ex) {
            Logger.getLogger(BaseStorageTest.class.getName()).log(Level.SEVERE, null, ex);
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
