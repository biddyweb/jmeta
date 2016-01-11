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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.SearchCriteria;
import org.meta.api.storage.MetaDatabase;
import org.meta.model.MetaSearch;
import org.meta.storage.MetaModelStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;
import org.meta.tests.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class ModelTest extends MetaBaseTests {

    protected static MetaModelStorage model;

    protected static Long startTime;

    protected static Long endTime;
    private static final Logger logger = LoggerFactory.getLogger(ModelTest.class);

    private MetHash hash;

    /**
     *
     */
    @BeforeClass
    public static void setUpModel() throws IOException {
        startTime = System.currentTimeMillis();
        try {
            MetaDatabase db = getDatabase(ModelTest.class.getSimpleName());
            model = new MetaModelStorage(db);
        } catch (StorageException ex) {
            logger.error(null, ex);
            Assert.fail("Failed to initialize backing storage");
        }
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to instanciate model");
    }

    /**
     *
     */
    @Test
    public void basicDataFileTest() {
        try {
            DataFile data = model.getFactory().getDataFile(TestUtils.createRandomTempFile("basicDataFileTest", 100));
            hash = data.getHash();
            Assert.assertTrue(model.set(data));
            DataFile extracted = model.getDataFile(hash);
            Assert.assertNotNull(extracted);
            Assert.assertEquals(data.getFile().getAbsolutePath(), extracted.getFile().getAbsolutePath());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
            logger.error(null, ex);
        }
    }

    /**
     *
     */
    @Test
    public void testDataStringUpdate() {
        try {
            //update basicDataFileTest written data
            Data data = model.getFactory().getData("Data");
            MetaData metaTitle = new MetaData("titre", "toto");
            data.addMetaData(metaTitle);

            Assert.assertTrue(model.set(data));
            //get new hash
            hash = data.getHash();
            //lookup in db
            Data dataFromDb = model.getData(hash);
            Assert.assertNotNull("extracted data should not be null!", dataFromDb);
            Assert.assertEquals("Meta-data size list should be = 1", 1, dataFromDb.getMetaDataMap().size());
            for (MetaData desc : dataFromDb.getMetaDataMap()) {
                Assert.assertEquals(metaTitle.getKey(), desc.getKey());
                Assert.assertEquals(metaTitle.getValue(), desc.getValue());
            }
            Assert.assertEquals("Data", dataFromDb.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            Assert.fail(ex.getMessage());
        }
    }

    /**
     *
     */
    @Test
    public void testDataFileUpdate() {
        try {
            File file = TestUtils.createRandomTempFile("modelTest-testDataFileUpdate", 100);
            DataFile data = model.getFactory().getDataFile(file);

            MetaData titre = new MetaData("titre", "toto");
            data.addMetaData(titre);

            Assert.assertTrue(model.set(data));
            //get new hash
            hash = data.getHash();
            //lookup in db
            DataFile dataFromDb = model.getDataFile(hash);
            Assert.assertNotNull(dataFromDb);
            Assert.assertEquals(1, dataFromDb.getMetaDataMap().size());
            for (MetaData desc : dataFromDb.getMetaDataMap()) {
                Assert.assertEquals(titre.getKey(), desc.getKey());
                Assert.assertEquals(titre.getValue(), desc.getValue());
            }
            Assert.assertEquals(true, dataFromDb.getFile().exists());
            //TODO #1
            Assert.assertEquals(file.getName(), dataFromDb.getFile().getName());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
            logger.error(null, ex);
        }
    }

    /**
     *
     */
    @Test
    public void testSearchUpdate() {
        try {
            Data source = model.getFactory().getData("data");
            Data dataResult = model.getFactory().getData("dataTest");
            MetaData prop = new MetaData("prop", "value");
            TreeSet<MetaData> props = new TreeSet<>();
            props.add(prop);
            SearchCriteria criteria = model.getFactory().createCriteria(props);
            MetaSearch search = model.getFactory().createSearch(source, criteria, Collections.singletonList((Data) dataResult));
            Assert.assertTrue("1 model.set should be true!", model.set(search));
            MetaSearch fromDb = model.getSearch(search.getHash());
            Assert.assertNotNull("object from db should be not null!", fromDb);
            Assert.assertEquals("Source data should be the same!!", "data", ((Data) fromDb.getSource()).toString());
            Assert.assertEquals("Results size should be 1!", 1, fromDb.getResults().size());
            //Add a result to the search
            Data newResult = model.getFactory().getData("newData");
            fromDb.addResult(newResult);
            Assert.assertTrue("2 model.set should be true!", model.set(fromDb));
            fromDb = model.getSearch(search.getHash());
            Assert.assertNotNull("object from db should be not null!", fromDb);
            Assert.assertEquals("Results size should now be 2!", 2, fromDb.getResults().size());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("ERROR IN testSearchUpdate");
        }
    }

    @Test
    public void searchDataFileTest() throws IOException {
        /**
         * *****************************************************************
         *
         * Create a new search
         *
         *****************************************************************
         */
        DataFile data = model.getFactory().getDataFile(TestUtils.createRandomTempFile("modelTest-searchDataFileTest", 100));

        SearchCriteria metaData = model.getFactory().createCriteria(new MetaData("st", "fr"));

        DataFile data2 = model.getFactory().getDataFile(TestUtils.createRandomTempFile("modelTest-searchDataFileTest2", 100));

        // -- MetaSearch
        MetaSearch search = model.getFactory().createSearch(data2, metaData, data);

        /**
         * *****************************************************************
         *
         *
         * Write in base
         *
         *
         *****************************************************************
         */
        Assert.assertTrue("Set search", model.set(search));
        Assert.assertTrue("Set data2", model.set(data2));
        Assert.assertTrue("Set data", model.set(data));

        logger.debug("DATA 2 : HASH = " + data2.getHash());

        /**
         * *****************************************************************
         *
         *
         * read in base
         *
         *
         *****************************************************************
         */
        MetaSearch readSearch = model.getSearch(search.getHash());
        Assert.assertNotNull("readsearch", readSearch);

        Data readData = model.getDataFile(data.getHash());
        Assert.assertNotNull("readData should not be null!", readData);

        Data readData2 = model.getDataFile(data2.getHash());
        Assert.assertNotNull("readData2 should not be null!", readData2);
        logger.debug("READ DATA 2 : HASH = " + readData2.getHash());

        /**
         * *****************************************************************
         *
         *
         * Delete in base
         *
         *
         *****************************************************************
         */
        Assert.assertNotNull("readData hash null!!", readData.getHash());
        Assert.assertTrue(model.remove(readData));
        Assert.assertTrue(model.remove(readData2));
    }

    /**
     *
     */
    @Test
    public void perfTest() throws IOException {
        int NB_IT = 10000;
        int DATA_SIZE = 100;
        Data[] datas = new Data[NB_IT];

        startTime = System.currentTimeMillis();
        for (int i = 0; i < NB_IT; i++) {
            datas[i] = model.getFactory().getData(TestUtils.getRandomString(DATA_SIZE));
        }
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to create " + NB_IT + " Data of size " + DATA_SIZE);

        //*--------------------------------------------------------------
        startTime = System.currentTimeMillis();
        for (int i = 0; i < NB_IT; i++) {
            Assert.assertTrue("perf set serialize", model.set(datas[i]));
        }
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Serializations");

        //*--------------------------------------------------------------
        startTime = System.currentTimeMillis();
        for (int i = 0; i < NB_IT; i++) {
            Assert.assertTrue("perf set updates", model.set(datas[i]));
        }
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Serializations (updates)");

        //*--------------------------------------------------------------
        startTime = System.currentTimeMillis();
        for (int i = 0; i < NB_IT; i++) {
            Assert.assertNotNull("perf get deserialize", model.getData(datas[i].getHash()));
        }
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Deserializations");

        //*--------------------------------------------------------------
        startTime = System.currentTimeMillis();
        for (int i = 0; i < NB_IT; i++) {
            Assert.assertTrue("perf set removal", model.remove(datas[i]));
        }
        endTime = System.currentTimeMillis();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " deletions");
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void threadTest() throws InterruptedException {
        final int NB_THREADS = 10;
        final int NB_IT = 100;

        class testThread extends Thread {

            private Data data;
            private Integer value;
            public boolean res;

            testThread(Integer id) {
                if (id % 2 == 0) {
                    id += 1;
                }
                value = id;
                data = model.getFactory().getData(value.toString());
                res = true;
            }

            public void run() {
                Data tmp;

                for (int i = 0; i < NB_IT && res; ++i) {
                    res = model.set(data);
                    tmp = model.getData(data.getHash());
                    res = res && tmp != null;
                    res = res && value == Integer.valueOf(tmp.toString());
                }
            }
        }
        testThread threads[] = new testThread[NB_THREADS];
        try {
            for (int i = 0; i < NB_THREADS; ++i) {
                threads[i] = new testThread(i);
                threads[i].start();
            }
            for (int i = 0; i < NB_THREADS; ++i) {
                threads[i].join();
            }
            for (int i = 0; i < NB_THREADS; ++i) {
                Assert.assertTrue(threads[i].res);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }
}
