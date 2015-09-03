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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.DataString;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaProperty;
import org.meta.api.model.Search;
import org.meta.api.storage.MetaStorage;
import org.meta.configuration.MetaConfiguration;
import org.meta.storage.KyotoCabinetStorage;
import org.meta.storage.MetaObjectModel;
import org.meta.storage.exceptions.ModelException;
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
    protected static MetaObjectModel model;

    /**
     *
     */
    protected static Long startTime;

    /**
     *
     */
    protected static Long endTime;
    private static final Logger logger = LoggerFactory.getLogger(ModelTest.class);

    /**
     *
     */
    @BeforeClass
    public static void setUpModel() {
        startTime = new Date().getTime();
        try {
            MetaStorage storage = new KyotoCabinetStorage(MetaConfiguration.getModelConfiguration());
            model = new MetaObjectModel(storage);
        } catch (StorageException ex) {
            logger.error(null, ex);
            Assert.fail();
        }
        endTime = new Date().getTime();
        logger.info("Took : " + (endTime - startTime) + "ms to instanciate model");
    }

    private MetHash hash;

    /**
     *
     */
    @Test
    public void basicTest() {
        try {
            DataFile data = model.getFactory().createDataFile(new File("/etc/hosts"));
            hash = data.getHash();
            Assert.assertTrue(model.set(data));
            Assert.assertNotNull(model.get(hash));
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
            //update basicTest writed data
            DataString data = model.getFactory().createDataString("Data");

            MetaProperty titre = new MetaProperty("titre", "toto");
            ArrayList<MetaProperty> description = new ArrayList<MetaProperty>();
            description.add(titre);
            data.setDescription(description);

            Assert.assertTrue(model.set(data));
            //get new hash
            hash = data.getHash();
            //lookup in db
            DataString dataFromDb = model.getDataString(hash);
            Assert.assertEquals(1, dataFromDb.getDescription().size());
            for (MetaProperty desc : dataFromDb.getDescription()) {
                Assert.assertEquals(titre.getName(), desc.getName());
                Assert.assertEquals(titre.getValue(), desc.getValue());
            }
            Assert.assertNotNull(dataFromDb);
            Assert.assertEquals("Data", dataFromDb.getString());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
            logger.error(null, ex);
        }
    }

    /**
     *
     */
    @Test
    public void testDataFileUpdate() {
        try {
            //TODO #1 : change files to something cleaner...
            DataFile data = model.getFactory().createDataFile(new File("/etc/hosts"));

            MetaProperty titre = new MetaProperty("titre", "toto");
            ArrayList<MetaProperty> description = new ArrayList<MetaProperty>();
            description.add(titre);
            data.setDescription(description);

            Assert.assertTrue(model.set(data));
            //get new hash
            hash = data.getHash();
            //lookup in db
            DataFile dataFromDb = model.getDataFile(hash);
            Assert.assertEquals(1, dataFromDb.getDescription().size());
            for (MetaProperty desc : dataFromDb.getDescription()) {
                Assert.assertEquals(titre.getName(), desc.getName());
                Assert.assertEquals(titre.getValue(), desc.getValue());
            }
            Assert.assertNotNull(dataFromDb);
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
    public void testMetaDataUpdate() {
        //create a strin data
        DataString data = model.getFactory().createDataString("data");
        //create a metaData
        MetaProperty prop = new MetaProperty("prop", "value");
        TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
        properties.add(prop);
        ArrayList<Data> a = new ArrayList<Data>();
        a.add(data);
        MetaData metaData = model.getFactory().createMetaData(properties);
        Assert.assertTrue(model.set(metaData));

        Assert.assertTrue(model.set(metaData));

        MetaData fromDb = model.getMetaData(metaData.getHash());
        Assert.assertNotNull(fromDb);
        Assert.assertEquals("value", ((MetaProperty) fromDb.getProperties().toArray()[0]).getValue());
        //Assert.assertEquals("data", ((DataString) fromDb.getLinkedData().get(0)).getStringCollections.singletonList((Data) dataMetaData),
    }

    /**
     *
     */
    @Test
    public void testSearchUpdate() {
        try {
            DataString source = model.getFactory().createDataString("data");
            DataString dataMetaData = model.getFactory().createDataString("dataTest");
            MetaProperty prop = new MetaProperty("prop", "value");
            TreeSet<MetaProperty> props = new TreeSet<MetaProperty>();
            props.addAll(Collections.singletonList(prop));
            MetaData metaData = model.getFactory().createMetaData(props);
            Search search = model.getFactory().createSearch(source, metaData, Collections.singletonList((Data) dataMetaData));
            Assert.assertTrue("1 model.set should be true!", model.set(search));

            Search fromDb = model.getSearch(search.getHash());
            Assert.assertNotNull("object from db should be not null!", fromDb);
            Assert.assertEquals("Source data should be the same!!", "data", ((DataString) fromDb.getSource()).getString());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("ERROR IN testSearchUpdate");
        }

    }

    /**
     *
     * @throws ModelException
     */
    @Test
    public void fullTest() throws ModelException {
        try {
            /**
             * *****************************************************************
             *
             * Create a new search
             *
             *****************************************************************
             */
            // -- Data
            DataFile data = model.getFactory().createDataFile(new File("/etc/hosts"));
            List<Data> linkedData = new ArrayList<Data>();
            linkedData.add(data);

            // -- MetaProperty
            MetaProperty property = new MetaProperty("st", "fr");
            TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
            properties.add(property);

            // -- MetaData answer
            MetaData metaData = model.getFactory().createMetaData(properties);

            // -- MetaData source
            DataFile data2 = model.getFactory().createDataFile(new File("/etc/hostname"));

            // -- Search
            Search search = model.getFactory().createSearch(data2, metaData, linkedData);

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
            Assert.assertTrue("Set metaData", model.set(metaData));
            Assert.assertTrue("Set data", model.set(data));

            /**
             * *****************************************************************
             *
             *
             * read in base
             *
             *
             *****************************************************************
             */
            Search readSearch = model.getSearch(search.getHash());
            Assert.assertNotNull("readsearch", readSearch);

            Data readData = model.getDataFile(data.getHash());
            Assert.assertNotNull("readData", readData);

            MetaData readMetaData = model.getMetaData(metaData.getHash());
            Assert.assertNotNull("readMetaData", readMetaData);

            Data readData2 = model.getDataFile(data2.getHash());
            Assert.assertNotNull("readData2", readData2);

            /**
             * *****************************************************************
             *
             *
             * Delete in base
             *
             *
             *****************************************************************
             */
            Assert.assertTrue(model.remove(readData));
            Assert.assertTrue(model.remove(readData2));
            Assert.assertTrue(model.remove(readMetaData));
        } catch (Exception ex) {
            logger.error(null, ex);
        }
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
            DataFile data = model.getFactory().createDataFile(file);
            Assert.assertTrue("perf set" + hash, model.set(data));
        }
        endTime = new Date().getTime();
        logger.info("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Serializations");
        startTime = new Date().getTime();
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            DataFile data = model.getFactory().createDataFile(file);
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

            private DataString data;
            private Integer value;
            public boolean res;

            testThread(Integer id) {
                value = id;
                data = model.getFactory().createDataString(value.toString());
                res = true;
            }

            public void run() {
                DataString tmp;

                for (int i = 0; i < NB_IT && res; ++i) {
                    res = model.set(data);
                    tmp = model.getDataString(data.getHash());
                    res = res && tmp != null;
                    res = res && value == Integer.valueOf(tmp.getString());
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