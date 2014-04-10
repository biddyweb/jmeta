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
import org.meta.plugin.TCP.AMPCommand;
import org.meta.plugin.TCP.AMP.AMPAnswerFactory;
import org.meta.plugin.TCP.AMP.AMPAnswerParser;

public class CommanndTest extends AMPCommand {

	@Override
	public byte[] execute() {
		System.out.println("toto");
		
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
		return factory.getMessage();
	}

	@Override
	public void setParameters(String hash) {
		// TODO Auto-generated method stub

	}

}
