package org.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.Model;
import org.meta.model.Search;
import org.meta.model.exceptions.ModelException;

public class ModelTest {

    protected static Model model;
    private static final Logger LOGGER = Logger.getLogger(ModelTest.class.getName());

    @BeforeClass
    public static void setUp() {
        try {
            model = new Model();
        } catch (ModelException ex) {
            Logger.getLogger(ModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void basicTest() {
        try {
            DataFile data = new DataFile(
                    "hashData1",
                    new File("db/meta.kch"));
            Assert.assertTrue(model.set(data));
            Assert.assertNotNull(model.get("hashData1"));
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
            LOGGER.log(Level.SEVERE, null, ex);
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
            DataFile data = new DataFile(
                    "hashData1",
                    new File("db/meta.kch"));
            List<Data> linkedData = new ArrayList<Data>();
            linkedData.add(data);

            // -- MetaProperty
            MetaProperty property = new MetaProperty("st", "fr");
            List<MetaProperty> properties = new ArrayList<MetaProperty>();
            properties.add(property);

            // -- MetaData answer
            MetaData metaData = new MetaData(
                    "hashMetaData",
                    linkedData,
                    properties);
            List<MetaData> results = new ArrayList<MetaData>();
            results.add(metaData);

            // -- MetaData source
            DataFile data2 = new DataFile(
                    "hashData2",
                    new File("db/meta.kch"));

            // -- Search
            Search search = new Search("hashSearch", data2, results);

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
            Search readSearch = model.getSearch("hashSearch");
            Assert.assertNotNull("readsearch", readSearch);

            Data readData = model.getData("hashData1");
            Assert.assertNotNull("readData", readData);

            MetaData readMetaData = model.getMetaData("hashMetaData");
            Assert.assertNotNull("readMetaData", readMetaData);

            Data readData2 = model.getData("hashData2");
            Assert.assertNotNull("readData2", readData2);

            /**
             * *****************************************************************
             *
             *
             * Update in base
             *
             *
             *****************************************************************
             */
            // -- MetaData source
            DataFile data3 = new DataFile(
                    "hashData3",
                    new File("db/meta.kch"));
            readSearch.setSource(data3);
            Assert.assertTrue("Set readSearch to update", model.set(readSearch));
            Search readSearchAfterUpdate = model.getSearch("hashSearch");

            Assert.assertNotNull("readSearchAfterUpdate", readSearchAfterUpdate);

//            System.out.println(readSearchAfterUpdate.toJson());
            /**
             * *****************************************************************
             *
             *
             * Delete in base
             *
             *
             *****************************************************************
             */
            model.remove(readData2);
            model.remove(readMetaData);
            model.remove(readData);
            model.remove(readSearchAfterUpdate);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    //@Test
    public void perfTest() {
        int NB_IT = 100000;
        File file = new File("db/meta.kch");

        for (int i = 0; i < NB_IT; i++) {
            String hash = "hashData" + i;
            DataFile data = new DataFile(hash, file);
            Assert.assertTrue("perf set" + hash, model.set(data));
            Assert.assertNotNull("perf get " + hash, model.getData(hash));
        }

    }
}
