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
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.storage.KVMapStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.api.storage.MetaTx;
import org.meta.storage.exceptions.StorageException;
import org.meta.storage.serializers.Serializers;
import org.meta.tests.MetaBaseTests;

/**
 * Tests for Map-like storage {@link KVMapStorage}.
 *
 * @author dyslesiq
 */
public class KVMapStorageTest extends MetaBaseTests {

    private static MetaDatabase db;

    private static KVMapStorage<MetHash, String> storage;

    @BeforeClass
    public static void setup() {
        try {
            db = getDatabase(KVStorageTest.class.getSimpleName());
            storage = db.getKVMapStorage(KVStorageTest.class.getSimpleName(), Serializers.METHASH,
                    Serializers.STRING);
        } catch (StorageException | IOException ex) {
            Logger.getLogger(KVStorageTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Failed to initialize storage");
        }
    }

    @AfterClass
    public static void tearDown() {
        db.close();

    }

    @Test
    public void basicAddGetTest() {
        MetHash hash = MetamphetUtils.createRandomHash();
        String str = "basicAddGetTest";

        Assert.assertTrue(storage.put(null, hash, str));
        String dbStr = storage.get(hash);
        Assert.assertEquals(str, dbStr);
    }

    /**
     *
     */
    @Test
    public void removalTest() {
        MetHash hash = MetamphetUtils.createRandomHash();
        String value = "removalTest";

        Assert.assertTrue(storage.put(null, hash, value));
        Assert.assertTrue(storage.remove(null, hash));
        Assert.assertNull(storage.get(hash));
    }

    @Test
    public void basicTxTest() {
        MetHash hash = MetamphetUtils.createRandomHash();
        String str = "basicTxTest";

        MetaTx tx = storage.begin();
        Assert.assertTrue(storage.put(tx, hash, str));
        Assert.assertTrue(storage.commit(tx));
        Assert.assertEquals(str, storage.get(hash));
        tx = storage.begin();
        Assert.assertTrue(storage.remove(tx, hash));
        Assert.assertTrue(storage.commit(tx));
        Assert.assertNull(storage.get(hash));
    }

    @Test
    public void basicRollbackTest() {
        MetHash hash = MetamphetUtils.createRandomHash();
        String str = "basicRollbackTest";

        MetaTx tx = storage.begin();
        Assert.assertTrue(storage.put(tx, hash, str));
        Assert.assertEquals(str, storage.get(hash)); //This should be true before rollback
        Assert.assertTrue(storage.rollback(tx));
        Assert.assertNull(storage.get(hash)); //This should be null after rollback
    }

    @Test
    public void updateTest() {
        MetHash hash = MetamphetUtils.createRandomHash();
        String value = "updateTest";
        String newVal = "updateTestNewVal";

        Assert.assertTrue(storage.put(null, hash, value));
        Assert.assertTrue(storage.put(null, hash, newVal));
        Assert.assertEquals(newVal, storage.get(hash));
    }

}
