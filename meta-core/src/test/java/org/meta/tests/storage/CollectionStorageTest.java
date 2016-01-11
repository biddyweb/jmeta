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
import java.util.Iterator;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.api.storage.MetaTx;
import org.meta.api.storage.Serializer;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;
import org.meta.tests.TestUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class CollectionStorageTest extends MetaBaseTests {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CollectionStorageTest.class);

    private static MetaDatabase db;

    private static CollectionStorage<String> collectionStorage;

    private static CollectionStorageTestSerializer serializer;

    @BeforeClass
    public static void setup() {
        try {
            db = getDatabase(CollectionStorageTest.class.getSimpleName());
            serializer = new CollectionStorageTestSerializer();
            collectionStorage = db.getCollection(CollectionStorageTest.class.getSimpleName(), serializer);
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
    public void simpleAddGetTest() {
        String testString = "plop";
        //Assert.assertTrue(this.collectionStorage.begin());
        MetaTx tx = this.collectionStorage.begin();
        this.collectionStorage.clear();
        this.collectionStorage.add(testString);
        Assert.assertTrue(this.collectionStorage.commit(tx));
        Assert.assertEquals(collectionStorage.size(), 1);
        for (String str : collectionStorage) {
            Assert.assertEquals(str, testString);
        }
    }

    @Test
    public void simpleAddGetRollbackTest() {
        String testString = "plop";
        this.collectionStorage.clear();
        MetaTx tx = this.collectionStorage.begin();
        this.collectionStorage.add(testString);
        Assert.assertEquals(1, collectionStorage.size());
        Assert.assertTrue(this.collectionStorage.rollback(tx));
        Assert.assertEquals(0, collectionStorage.size());
    }

    @Test
    public void simpleAddGetAutoCommitTest() {
        String testString = "plop";
        this.collectionStorage.clear();
        this.collectionStorage.add(testString);
        Assert.assertEquals(1, collectionStorage.size());
        for (String str : collectionStorage) {
            Assert.assertEquals(str, testString);
        }
    }

    @Test
    public void simpleDeleteTest() {
        String testString = "plop";
        //Assert.assertTrue(this.collectionStorage.begin());
        MetaTx tx = this.collectionStorage.begin();
        this.collectionStorage.clear();
        this.collectionStorage.add(testString);
        Assert.assertTrue(this.collectionStorage.commit(tx));
        //Assert.assertTrue(this.collectionStorage.begin());
        tx = this.collectionStorage.begin();
        Assert.assertTrue(this.collectionStorage.remove(testString));
        Assert.assertTrue(this.collectionStorage.commit(tx));
        Assert.assertEquals(0, collectionStorage.size());
    }

    @Test
    public void orderingTest() {
        String str1 = "abc"; //First in lexicographic order
        String str2 = "abd"; //Second
        String str3 = "bcd"; //Third

        this.collectionStorage.clear();
        MetaTx tx = this.collectionStorage.begin();
        this.collectionStorage.add(str2);
        this.collectionStorage.add(str1);
        this.collectionStorage.add(str3);
        Assert.assertTrue(this.collectionStorage.commit(tx));
        Assert.assertEquals(3, collectionStorage.size());
        Iterator<String> it = collectionStorage.iterator();

        String tmp = it.next();
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.equals(str1));
        tmp = it.next();
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.equals(str2));
        tmp = it.next();
        Assert.assertNotNull(tmp);
        Assert.assertTrue(tmp.equals(str3));
    }

    @Test
    public void tailCeilSubTest() {
        String str1 = "abc"; //First in lexicographic order
        String str2 = "bcd"; //Second
        String str3 = "def"; //Third
        String cmpStr = "cde"; //Between second and third

        this.collectionStorage.clear();
        MetaTx tx = this.collectionStorage.begin();
        this.collectionStorage.add(str2);
        this.collectionStorage.add(str1);
        this.collectionStorage.add(str3);
        Assert.assertTrue(this.collectionStorage.commit(tx));
        SortedSet<String> headSet = this.collectionStorage.headSet(cmpStr);
        Assert.assertNotNull(headSet);
        Assert.assertEquals(2, headSet.size());

        SortedSet<String> tailSet = this.collectionStorage.tailSet(cmpStr);
        Assert.assertNotNull(tailSet);
        Assert.assertEquals(1, tailSet.size());
        headSet.clear();
        Assert.assertEquals(1, collectionStorage.size());
        tailSet.clear();
        Assert.assertTrue(this.collectionStorage.isEmpty());
    }

    @Test
    public void perfTest() {
        int NB_IT = 10000;
        int DATA_SIZE = 100;

        Long startTime = System.currentTimeMillis();
        //Assert.assertTrue(this.collectionStorage.begin());
        MetaTx tx = this.collectionStorage.begin();
        for (int i = 0; i < NB_IT; i++) {
            this.collectionStorage.add(TestUtils.getRandomString(DATA_SIZE));
        }
        Assert.assertTrue(this.collectionStorage.commit(tx));
        Long endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to create and store" + NB_IT + " String of size " + DATA_SIZE + " inside CollectionStorage");

        startTime = System.currentTimeMillis();
        for (String str : collectionStorage) {
            ;// do nothing, just iteration for db retrieval.
        }
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to read " + NB_IT + " String of size " + DATA_SIZE + " from CollectionStorage");

        startTime = System.currentTimeMillis();
        //Assert.assertTrue(this.collectionStorage.begin());
        tx = this.collectionStorage.begin();
        this.collectionStorage.clear();
        Assert.assertTrue(this.collectionStorage.commit(tx));
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to delete " + NB_IT + " String of size " + DATA_SIZE + " from CollectionStorage");

    }

    /**
     * Dummy serialization of String for tests.
     */
    private static class CollectionStorageTestSerializer implements Serializer<String> {

        @Override
        public byte[] serialize(String object) {
            return object.getBytes();
        }

        @Override
        public String deserialize(byte[] data) {
            return new String(data);
        }

    }

}
