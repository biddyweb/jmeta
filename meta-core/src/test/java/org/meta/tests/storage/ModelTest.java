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
import java.util.Date;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.SearchCriteria;
import org.meta.api.storage.MetaStorage;
import org.meta.configuration.MetaConfiguration;
import org.meta.model.MetaSearch;
import org.meta.storage.KyotoCabinetStorage;
import org.meta.storage.MetaModelStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class ModelTest extends MetaBaseTests {

    /**
     *
     */
    protected static MetaModelStorage model;

    /**
     *
     */
    protected static Long startTime;

    /**
     *
     */
    protected static Long endTime;
    private static final Logger logger = LoggerFactory.getLogger(ModelTest.class);

    private MetHash hash;

    /**
     *
     */
    @BeforeClass
    public static void setUpModel() {
        startTime = new Date().getTime();
        try {
            MetaStorage storage = new KyotoCabinetStorage(MetaConfiguration.getModelConfiguration());
            model = new MetaModelStorage(storage);
        } catch (StorageException ex) {
            logger.error(null, ex);
            Assert.fail();
        }
        endTime = new Date().getTime();
        logger.info("Took : " + (endTime - startTime) + "ms to instanciate model");
    }

    private static File getTmpFile() throws IOException {
        return File.createTempFile(Long.toString(System.currentTimeMillis())
                + "-TEMP", ".metaTmp");
    }

    /**
     *
     */
    @Test
    public void basicDataFileTest() {
        try {
            DataFile data = model.getFactory().getDataFile(getTmpFile());
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
            //TODO #1 : change files to something cleaner...
            DataFile data = model.getFactory().getDataFile(new File("/etc/hosts"));

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
            Assert.assertEquals("hosts", dataFromDb.getFile().getName());
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
    public void searchDataFileTest() {
        /**
         * *****************************************************************
         *
         * Create a new search
         *
         *****************************************************************
         */
        DataFile data = model.getFactory().getDataFile(new File("/etc/hosts"));

        SearchCriteria metaData = model.getFactory().createCriteria(new MetaData("st", "fr"));

        DataFile data2 = model.getFactory().getDataFile(new File("/etc/hostname"));

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

    //@Test
    /**
     *
     */
    public void perfTest() {
        MetHash hash;
        int NB_IT = 100000;
        startTime = new Date().getTime();
        File file = new File("/etc/hosts");
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            DataFile data = model.getFactory().getDataFile(file);
            Assert.assertTrue("perf set" + hash, model.set(data));
        }
        endTime = new Date().getTime();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Serializations");
        startTime = new Date().getTime();
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            DataFile data = model.getFactory().getDataFile(file);
            Assert.assertTrue("perf set" + hash, model.set(data));
        }
        endTime = new Date().getTime();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Serializations (updates)");
        startTime = new Date().getTime();
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            Assert.assertNotNull("perf get " + hash, model.getDataFile(hash));
        }
        endTime = new Date().getTime();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Deserializations");
        startTime = new Date().getTime();
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            Assert.assertTrue("perf set" + hash, model.remove(hash));
        }
        endTime = new Date().getTime();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " deletions");
    }

    @Test
    /**
     *
     * @throws InterruptedException
     */
    public void threadTest() throws InterruptedException {
        final int NB_THREADS = 10;
        final int NB_IT = 100;

        class testThread extends Thread {

            private Data data;
            private Integer value;
            public boolean res;

            testThread(Integer id) {
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
