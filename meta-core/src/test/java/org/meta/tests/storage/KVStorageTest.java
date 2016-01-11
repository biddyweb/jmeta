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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.storage.KVStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.api.storage.MetaTx;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;

/**
 * Tests for the simple byte[] storage {@link KVStorage}.
 *
 * @author dyslesiq
 */
public class KVStorageTest extends MetaBaseTests {

    private static MetaDatabase db;

    private static KVStorage storage;

    @BeforeClass
    public static void setup() {
        try {
            db = getDatabase(KVStorageTest.class.getSimpleName());
            storage = db.getKVStorage(KVStorageTest.class.getSimpleName());
        } catch (StorageException | IOException ex) {
            Logger.getLogger(KVStorageTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Failed to initialize storage");
        }
    }

    @AfterClass
    public static void tearDown() {
        storage.close();
    }

    /**
     *
     */
    @Test
    public void basicTest() {
        byte[] key = "basicTest".getBytes();
        byte[] value = "basicTestValue".getBytes();

        Assert.assertTrue(storage.store(null, key, value));
        Assert.assertArrayEquals(value, storage.get(key));
    }

    /**
     *
     */
    @Test
    public void basicTxTest() {
        byte[] key = "basicTxTest".getBytes();
        byte[] value = "basicTxTestValue".getBytes();

        MetaTx tx = storage.begin();
        Assert.assertTrue(storage.store(tx, key, value));
        Assert.assertTrue(storage.commit(tx));
        Assert.assertArrayEquals(value, storage.get(key));
        tx = storage.begin();
        Assert.assertTrue(storage.remove(tx, key));
        storage.commit(tx);
        Assert.assertNull(storage.get(key));
    }

    @Test
    public void rollbackTest() {
        byte[] key = "rollbackTest".getBytes();
        byte[] value = "rollbackTestValue".getBytes();

        Assert.assertTrue(storage.remove(null, key));
        MetaTx tx = storage.begin();
        Assert.assertTrue(storage.store(tx, key, value));
        Assert.assertNotNull(storage.get(key)); //This should be not null before rollback()
        Assert.assertTrue(storage.rollback(tx));
        Assert.assertNull(storage.get(key)); //This should be still null after rollback()
    }

    /**
     *
     */
    @Test
    public void updateTest() {
        byte[] key = "updateTest".getBytes();
        byte[] value = "updateTestValue".getBytes();
        byte[] newVal = "updateTestNewVal".getBytes();

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
        byte[] value = "removalTestValue".getBytes();

        Assert.assertTrue(storage.store(null, key, value));
        Assert.assertTrue(storage.remove(null, key));
        Assert.assertNull(storage.get(key));
    }

}
