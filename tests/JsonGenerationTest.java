package tests;

import java.io.File;
import java.util.ArrayList;

import com.meta.modele.Data;
import com.meta.modele.MetaData;
import com.meta.modele.MetaProperty;
import com.meta.modele.Search;

import djondb.LibraryException;
import djondb.Loader;

public class JsonGenerationTest {

	public static void main(String[] args) {
		try {
			Loader.initialize();
		} catch (LibraryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/***********************************************************************
		 * 
		 * 						Create a new search
		 * 
		 **********************************************************************/
		// -- Data
		Data data = new Data(
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
		Data data2 = new Data(
				"hashData2",
				new File("/home/ecodair/movie.avi"));
		
		// -- Search
		Search search = new Search("hashSearch", data2, results);
		
		/***********************************************************************
		 * 
		 * 						print json 
		 * 
		 **********************************************************************/
		System.out.println(search.toJson());
		System.out.println();
		System.out.println(data2.toJson());
		System.out.println();
		System.out.println(metaData.toJson());
		System.out.println();
		System.out.println(data.toJson());
		
		/***********************************************************************
		 * 
		 * Expecting result : 
		 * 
		 * { hash  	: 'hashSearch',  source 	: 'hashData2',   results 	: ['hashMetaData' ]}
		 * { hash : 'hashData2',  file : '/home/ecodair/movie.avi' }
		 * { hash 		: 'hashMetaData',  linkedData : ['hashData1'], properties : [{name : 'st', value : 'fr' }]}
		 * { hash : 'hashData1',  file : '/home/ecodair/subtitle.txt' }
		 * 
		 **********************************************************************/
	}
}
