package tests;

import java.io.File;
import java.util.ArrayList;

import com.meta.modele.Data;
import com.meta.modele.DataFile;
import com.meta.modele.MetaData;
import com.meta.modele.MetaProperty;
import com.meta.modele.Model;
import com.meta.modele.Search;

import djondb.LibraryException;

public class ModelTest {

	public static void main(String[] args) throws LibraryException {
		// -- instantiate the model
		Model model = new Model();
		/*******************************************************************
		 * 
		 * 						Create a new search
		 * 
		 ******************************************************************/
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
		
		/*******************************************************************
		 * 
		 * 
		 * 			Write in base
		 * 
		 * 
		 ******************************************************************/
		model.createInDataBase(search);
		model.createInDataBase(data2);
		model.createInDataBase(metaData);
		model.createInDataBase(data);
		/*******************************************************************
		 * 
		 * 
		 * 			read in base
		 * 
		 * 
		 ******************************************************************/
		Search 		readSearch 	= model.getSearch("hashSearch");
		Data		readData	= model.getData("hashData1");
		MetaData	readMetaData= model.getMetaData("hashMetaData");
		Data		readData2	= model.getData("hashData2");
		
		System.out.println(readSearch.toJson().toChar());
		System.out.println(readData.toJson().toChar());
		System.out.println(readMetaData.toJson().toChar());
		System.out.println(readData2.toJson().toChar());

		/*******************************************************************
		 * 
		 * 
		 * 			Update in base
		 * 
		 * 
		 ******************************************************************/
		// -- MetaData source
		DataFile data3 = new DataFile(
				"hashData3",
				new File("/home/ecodair/movie8.avi"));
		readSearch.setSource(data3);
		model.updateInDataBase(readSearch);
		Search 	readSearchAfterUpdate 	= model.getSearch("hashSearch");
		System.out.println(readSearchAfterUpdate.toJson().toChar());
		Data readData3 = model.getData("hashData3");
		System.out.println(readData3.toJson().toChar());
		/*******************************************************************
		 * 
		 * 
		 * 			Delete in base
		 * 
		 * 
		 ******************************************************************/
		model.deleteInDataBase(readData2);
		model.deleteInDataBase(readMetaData);
		model.deleteInDataBase(readData);
		model.deleteInDataBase(readData3);
		model.deleteInDataBase(readSearchAfterUpdate);
	}
}
