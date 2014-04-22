package org.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.Model;
import org.meta.model.Search;
import org.meta.model.exceptions.ModelException;

public class ModelTest {

    protected Model model;

    @Before
    public void setUp() {
//		model = new Model();
    }

    @Test
    public void testCreate() throws Exception {
        assertEquals(2, 2);
    }

    @Test
    public void testCreate2() throws Exception {
        assertEquals(2, 2);
    }

    public static void main(String[] args) {
        try {
            // -- instantiate the model
            Model model = new Model();
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
                    new File("/home/ecodair/subtitle.txt"));
            ArrayList<Data> linkedData = new ArrayList<Data>();
            linkedData.add(data);
            
            // -- MetaProperty
            MetaProperty property = new MetaProperty("st", "fr");
            ArrayList<MetaProperty> properties = new ArrayList<MetaProperty>();
            properties.add(property);
            
            // -- MetaData answer
            MetaData metaData = new MetaData(
                    "hashMetaData",
                    linkedData,
                    properties);
            ArrayList<MetaData> results = new ArrayList<MetaData>();
            results.add(metaData);
            
            // -- MetaData source
            DataFile data2 = new DataFile(
                    "hashData2",
                    new File("/home/ecodair/movie.avi"));
            
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
            model.set(search);
            model.set(data2);
            model.set(metaData);
            model.set(data);
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
            Data readData = model.getData("hashData1");
            MetaData readMetaData = model.getMetaData("hashMetaData");
            Data readData2 = model.getData("hashData2");
            
            System.out.println(readSearch.toJson());
            System.out.println(readData.toJson());
            System.out.println(readMetaData.toJson());
            System.out.println(readData2.toJson());
            
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
                    new File("/home/ecodair/movie8.avi"));
            readSearch.setSource(data3);
            model.updateInDataBase(readSearch);
            Search readSearchAfterUpdate = model.getSearch("hashSearch");
            System.out.println(readSearchAfterUpdate.toJson());
            Data readData3 = model.getData("hashData3");
            System.out.println(readData3.toJson());
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
            model.remove(readData3);
            model.remove(readSearchAfterUpdate);
        } catch (ModelException ex) {
            Logger.getLogger(ModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
