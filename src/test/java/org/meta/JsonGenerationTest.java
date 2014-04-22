package org.meta;

import java.io.File;
import java.util.ArrayList;

import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.Search;

public class JsonGenerationTest {

    public static void main(String[] args) {
        /**
         * *********************************************************************
         *
         * Create a new search
         *
         *********************************************************************
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
         * *********************************************************************
         *
         * print json
         *
         *********************************************************************
         */
        System.out.println(search.toJson());
        System.out.println();
        System.out.println(data2.toJson());
        System.out.println();
        System.out.println(metaData.toJson());
        System.out.println();
        System.out.println(data.toJson());

        /**
         * *********************************************************************
         *
         * Expecting result :
         *
         * { hash : 'hashSearch', source : 'hashData2', results :
         * ['hashMetaData' ]} { hash : 'hashData2', file :
         * '/home/ecodair/movie.avi' } { hash : 'hashMetaData', linkedData :
         * ['hashData1'], properties : [{name : 'st', value : 'fr' }]} { hash :
         * 'hashData1', file : '/home/ecodair/subtitle.txt' }
         *
         *********************************************************************
         */
    }
}
