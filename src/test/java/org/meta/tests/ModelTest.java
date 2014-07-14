package org.meta.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.Model;
import org.meta.model.Search;
import org.meta.model.exceptions.ModelException;

public class ModelTest {

    protected static Model model;
    protected static Long startTime;
    protected static Long endTime;
    private static final Logger LOGGER = Logger.getLogger(ModelTest.class.getName());

    @BeforeClass
    public static void setUp() {
        startTime = new Date().getTime();
        model = Model.getInstance();
        endTime = new Date().getTime();
        System.out.println("Took : " + (endTime - startTime) + "ms to instanciate model");
    }

    @Test
    public void basicTest() {
        try {
            MetHash hash = MetamphetUtils.makeSHAHash("hashData1");
            DataFile data = model.getFactory().getDataFile(hash, new File("db/meta.kch"));
            Assert.assertTrue(model.set(data));
            Assert.assertNotNull(model.get(hash));
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testDataStringUpdate() {
        try {
            MetHash hash = MetamphetUtils.makeSHAHash("hashDataUpdate");
            DataString data = model.getFactory().getDataString(hash, "Data");
            Assert.assertTrue(model.set(data));
            data.setString("newData");
            Assert.assertTrue(model.set(data));
            DataString dataFromDb = model.getDataString(hash);
            Assert.assertNotNull(dataFromDb);
            Assert.assertEquals("newData", dataFromDb.getString());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testMetaDataUpdate() {
        MetHash hash = MetamphetUtils.makeSHAHash("hashMetaDataUpdate");
        DataString data = model.getFactory().getDataString(MetamphetUtils.makeSHAHash("hashLinkedData"), "data");
        MetaProperty prop = new MetaProperty("prop", "value");
        List<MetaProperty> props = Collections.singletonList(prop);
        MetaData metaData = model.getFactory().getMetaData(hash, Collections.singletonList((Data) data), props);
        Assert.assertTrue(model.set(metaData));

        props.get(0).setValue("newValue");
        metaData.setProperties(props);
        data = (DataString) metaData.getLinkedData().get(0);
        data.setString("newData");
        metaData.setLinkedData(Collections.singletonList((Data) data));
        Assert.assertTrue(model.set(metaData));

        MetaData fromDb = model.getMetaData(hash);
        Assert.assertNotNull(fromDb);
        Assert.assertEquals("newValue", fromDb.getProperties().get(0).getValue());
        Assert.assertEquals("newData", ((DataString) fromDb.getLinkedData().get(0)).getString());
    }

    @Test
    public void testSearchUpdate() {
        try {
            MetHash hash = MetamphetUtils.makeSHAHash("hashSearchUpdate");
            DataString source = model.getFactory().getDataString(MetamphetUtils.makeSHAHash("hashDataSource"), "data");
            DataString dataMetaData = model.getFactory().getDataString(MetamphetUtils.makeSHAHash("hashLinkedDataResults"), "dataTest");
            MetaProperty prop = new MetaProperty("prop", "value");
            List<MetaProperty> props = Collections.singletonList(prop);
            MetHash metadataHash = MetamphetUtils.makeSHAHash("metadataHash");
            MetaData metaData = model.getFactory().getMetaData(metadataHash, Collections.singletonList((Data) dataMetaData), props);
            Search search = model.getFactory().getSearch(hash, source, Collections.singletonList(metaData));
            Assert.assertTrue("1 model.set should be true!", model.set(search));

            ((DataString) search.getSource()).setString("newSourceData");
            Assert.assertTrue("2 model.set should be true!", model.set(search));
            Search fromDb = model.getSearch(hash);
            Assert.assertNotNull("object from db should be not null!", fromDb);
            Assert.assertEquals("Source data should be the same!!", "newSourceData", ((DataString) fromDb.getSource()).getString());
        } catch (Exception ex) {
            //System.err.println("ERROR IN testSearchUpdate");
            ex.printStackTrace();
        }

    }

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
            MetHash dataHash = MetamphetUtils.makeSHAHash("hashData1");
            DataFile data = model.getFactory().getDataFile(dataHash, new File("db/meta.kch"));
            List<Data> linkedData = new ArrayList<Data>();
            linkedData.add(data);

            // -- MetaProperty
            MetaProperty property = new MetaProperty("st", "fr");
            List<MetaProperty> properties = new ArrayList<MetaProperty>();
            properties.add(property);

            // -- MetaData answer
            MetHash metaDataHash = MetamphetUtils.makeSHAHash("hashMetaData");
            MetaData metaData = model.getFactory().getMetaData(
                    metaDataHash,
                    linkedData,
                    properties);
            List<MetaData> results = new ArrayList<MetaData>();
            results.add(metaData);

            // -- MetaData source
            MetHash data2Hash = MetamphetUtils.makeSHAHash("hashData2");
            DataFile data2 = model.getFactory().getDataFile(
                    data2Hash,
                    new File("db/meta.kch"));

            // -- Search
            MetHash searchHash = MetamphetUtils.makeSHAHash("hashSearch");
            Search search = model.getFactory().getSearch(searchHash, data2, results);

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
            Search readSearch = model.getSearch(searchHash);
            Assert.assertNotNull("readsearch", readSearch);

            Data readData = model.getDataFile(dataHash);
            Assert.assertNotNull("readData", readData);

            MetaData readMetaData = model.getMetaData(metaDataHash);
            Assert.assertNotNull("readMetaData", readMetaData);

            Data readData2 = model.getDataFile(data2Hash);
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
            Assert.assertTrue(model.remove(readData2));
            Assert.assertTrue(model.remove(readMetaData));
            Assert.assertTrue(model.remove(readData));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void perfTest() {
        MetHash hash;
        int NB_IT = 100000;
        startTime = new Date().getTime();
        File file = new File("db/meta.kch");
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            DataFile data = model.getFactory().getDataFile(hash, file);
            Assert.assertTrue("perf set" + hash, model.set(data));
        }
        endTime = new Date().getTime();
        System.out.println("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Serializations");
        startTime = new Date().getTime();
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            DataFile data = model.getFactory().getDataFile(hash, file);
            Assert.assertTrue("perf set" + hash, model.set(data));
        }
        endTime = new Date().getTime();
        System.out.println("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Serializations (updates)");
        startTime = new Date().getTime();
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            Assert.assertNotNull("perf get " + hash, model.getDataFile(hash));
        }
        endTime = new Date().getTime();
        System.out.println("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " Deserializations");
        startTime = new Date().getTime();
        for (int i = 0; i < NB_IT; i++) {
            hash = MetamphetUtils.makeSHAHash("hashData" + i);
            Assert.assertTrue("perf set" + hash, model.remove(hash));
        }
        endTime = new Date().getTime();
        System.out.println("Took : " + (endTime - startTime) + "ms to do " + NB_IT + " deletions");
    }

    @Test
    public void threadTest() throws InterruptedException {
        final int NB_THREADS = 100;
        final int NB_IT = 100;

        class testThread extends Thread {

            private MetHash hash;
            private DataString data;
            private Integer value;
            public boolean res;

            testThread(Integer id) {
                value = 0;
                hash = MetamphetUtils.makeSHAHash(id.toString());
                data = model.getFactory().getDataString(hash, value.toString());
                res = true;
            }

            public void run() {
                DataString tmp;

                for (int i = 0; i < NB_IT && res; ++i, ++value) {
                    data.setString(value.toString());
                    res = model.set(data);
                    tmp = model.getDataString(hash);
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
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
