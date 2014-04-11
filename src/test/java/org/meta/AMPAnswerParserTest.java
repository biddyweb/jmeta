package org.meta;

import java.io.File;
import java.util.ArrayList;

import org.meta.modele.Data;
import org.meta.modele.DataFile;
import org.meta.modele.DataString;
import org.meta.modele.MetaData;
import org.meta.modele.MetaProperty;
import org.meta.modele.Search;
import org.meta.modele.Searchable;
import org.meta.plugin.TCP.AMP.exception.NotAValidAMPCommand;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;
import org.meta.plugin.tcp.amp.AMPAnswerParser;

public class AMPAnswerParserTest {
	
	public static void main(String[] args) {
		//Data File
		DataFile dataFile = new DataFile("toto va au cinéma", new File("/home/faquin/done.txt"));
		
		
		// -- Data String
		DataString data = new DataString(
				"hashData1", 
				"Toto va à la plage");
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
		
		ArrayList<Searchable> datas = new ArrayList<Searchable>();
		datas.add(data);
		datas.add(dataFile);
		datas.add(search);
		datas.add(metaData);
		
		AMPAnswerFactory factory = new AMPAnswerFactory("12", datas);
		AMPAnswerParser parser;
		try {
			parser = new AMPAnswerParser(factory.getMessage());
			
			ArrayList<Searchable> dataReceived = parser.getDatas();
			
			for (int i=0; i<dataReceived.size(); i++) {
				Searchable searchable = dataReceived.get(i);
				
				if(searchable instanceof Search){
					Search search3 = (Search) searchable;
					System.out.println(search3.getTmpResultsHashes());
				}else if(searchable instanceof MetaData){
					MetaData metaData3 = (MetaData) searchable;
					System.out.println(metaData3.getTmpLinkedData());
				}else if(searchable instanceof DataFile){
					DataFile dataFile3 = (DataFile) searchable;
					System.out.println(dataFile3.getFile().getName());
				}else if(searchable instanceof DataString){
					DataString dataString = (DataString) searchable;
					System.out.println(dataString.getString());
				}
			}
		} catch (NotAValidAMPCommand e) {
			e.printStackTrace();
		}
	}
}
