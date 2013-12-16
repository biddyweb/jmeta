package tests;

import java.io.File;
import java.util.ArrayList;

import com.meta.modele.Data;
import com.meta.modele.MetaData;
import com.meta.modele.MetaProperty;
import com.meta.modele.Search;

import djondb.BSONArrayObj;
import djondb.BSONObj;
import djondb.DjondbConnection;
import djondb.DjondbConnectionManager;
import djondb.LibraryException;
import djondb.Loader;

public class DataBaseTest {

	public static void main(String[] args) {
		try {
			// -- init database connection
			Loader.initialize();
			DjondbConnection  connection = 
					DjondbConnectionManager.getConnection("localhost");
			connection.open();
			System.out.println(connection);
			// -- 
			/*******************************************************************
			 * 
			 * 						Create a new search
			 * 
			 ******************************************************************/
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
			
			/*******************************************************************
			 * 
			 * 
			 * 			Write in base
			 * 
			 * 
			 ******************************************************************/
 			connection.insert("JMeta", "JMeta", search.toJson());
			connection.insert("JMeta", "JMeta", data2.toJson());
			connection.insert("JMeta", "JMeta", metaData.toJson());
			connection.insert("JMeta", "JMeta", data.toJson());
			/*******************************************************************
			 * 
			 * 
			 * 			read in base
			 * 
			 * 
			 ******************************************************************/
			BSONArrayObj result	 = connection.find("JMeta", "JMeta", "");

			for(int i=0; i<result.length(); i++){
				BSONObj bsonSearch = result.get(i);
				System.out.println(bsonSearch.toChar());
			}
			
			/*******************************************************************
			 * 
			 * 
			 * 			Delete in base
			 * 
			 * 
			 ******************************************************************/
			for(int i=0; i<result.length(); i++){
				BSONObj bsonSearch = result.get(i);
				connection.remove(	
						"JMeta", 
						"JMeta",  
						bsonSearch.getString("_id"), 
						bsonSearch.getString("_revision"));
			}
			// -- Release connection
			DjondbConnectionManager.releaseConnection(connection);
		} catch (LibraryException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
