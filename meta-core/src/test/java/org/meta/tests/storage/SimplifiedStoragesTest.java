
package org.meta.tests.storage;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.KVMapStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;

/**
 *
 * Tests for simplified storages classes that use Java Serialization.
 *
 * @author dyslesiq
 */
public class SimplifiedStoragesTest extends MetaBaseTests {

    private static MetaDatabase db;

    @BeforeClass
    public static void setup() {
        try {
            db = getDatabase(SimplifiedStoragesTest.class.getSimpleName());
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
    public void kvMapStoreTest() {
        KVMapStorage<String, MetHash> storage = db.getMapStorage("kvMapStoreTest");

        MetHash random = MetamphetUtils.createRandomHash();
        Assert.assertTrue(storage.put(null, "key", random));
        MetHash randomFromDb = storage.get("key");
        Assert.assertNotNull(randomFromDb);
        Assert.assertTrue(random.equals(randomFromDb));
    }

    @Test
    public void kvMapDeleteTest() {
        KVMapStorage<String, MetHash> storage = db.getMapStorage("kvMapDeleteTest");

        MetHash random = MetamphetUtils.createRandomHash();
        Assert.assertTrue(storage.put(null, "key", random));
        Assert.assertTrue(storage.remove(null, "key"));
        MetHash randomFromDb = storage.get("key");
        Assert.assertNull(randomFromDb);
    }

    @Test
    public void collectionStoreTest() {
        CollectionStorage<MetHash> storage = db.getCollection("collectionStoreTest");

        MetHash random = MetamphetUtils.createRandomHash();
        Assert.assertTrue(storage.add(random));
        Assert.assertEquals(1, storage.size());
        Assert.assertTrue(random.equals(storage.iterator().next()));
    }

    @Test
    public void collectionDeleteTest() {
        CollectionStorage<MetHash> storage = db.getCollection("collectionDeleteTest");

        MetHash random = MetamphetUtils.createRandomHash();
        Assert.assertTrue(storage.add(random));
        Assert.assertTrue(storage.remove(random));
        Assert.assertEquals(0, storage.size());
    }

    @Test
    public void testCustomObject() {
        CollectionStorage<CustomSerializable> storage = db.getCollection("testCustomObject");

        CustomSerializable obj = new CustomSerializable();
        Assert.assertTrue(storage.add(obj));
        Assert.assertEquals(1, storage.size());
        Assert.assertTrue(obj.equals(storage.iterator().next()));
    }

    private static class CustomSerializable implements Serializable {

        private static MetHash staticHash = MetamphetUtils.createRandomHash();

        private String testString;
        private MetHash testMetHash;
        private List<String> testList;

        public CustomSerializable() {
            testString = "test";
            testMetHash = staticHash;
            testList = new ArrayList<>();
            testList.add("testList");
            System.out.println("org.meta.tests.storage.SimplifiedStoragesTest.CustomSerializable.<init>()");
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CustomSerializable)) {
                return false;
            }
            CustomSerializable s = (CustomSerializable) obj;
            if (testList.size() != s.testList.size()) {
                return false;
            }
            if (!testList.iterator().next().equals(s.testList.iterator().next())) {
                return false;
            }
            return testString.equals(s.testString) && testMetHash.equals(s.testMetHash);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.testString);
            hash = 97 * hash + Objects.hashCode(this.testMetHash);
            hash = 97 * hash + Objects.hashCode(this.testList);
            return hash;
        }

    }

}
