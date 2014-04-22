package org.meta;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.Model;
import org.meta.model.Search;
import org.meta.model.exceptions.ModelException;

public class DataBaseTest {

    public static void main(String[] args) {

        try {
            Model model = new Model();
            
            // --
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
            DataString data2 = new DataString(
                    "hashData2",
                    "Ma super chaine");
            
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
            
//                        connection.insert("JMeta", "JMeta", search.toJson());
//			connection.insert("JMeta", "JMeta", data2.toJson());
//			connection.insert("JMeta", "JMeta", metaData.toJson());
//			connection.insert("JMeta", "JMeta", data.toJson());
            /**
             * *****************************************************************
             *
             *
             * read in base
             *
             *
             *****************************************************************
             */
//			BSONArrayObj result	 = connection.find("JMeta", "JMeta", "");
//
//			for(int i=0; i<result.length(); i++){
//				BSONObj bsonSearch = result.get(i);
//				System.out.println(bsonSearch.toChar());
//			}
            /**
             * *****************************************************************
             *
             *
             * Delete in base
             *
             *
             *****************************************************************
             */
//			for(int i=0; i<result.length(); i++){
//				BSONObj bsonSearch = result.get(i);
//				connection.remove(
//						"JMeta",
//						"JMeta",
//						bsonSearch.getString("_id"), 
//						bsonSearch.getString("_revision"));
//			}
        } catch (ModelException ex) {
            Logger.getLogger(DataBaseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
