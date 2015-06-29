package org.meta.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.Model;
import org.meta.model.ModelFactory;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;
import org.meta.plugin.tcp.amp.AMPAnswerParser;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;

public class AMPAnswerParserTest {

    private Search search;
    private DataString data2;
    private MetaData metaData;
    private MetaProperty property;
    private Data data;
    private ArrayList<Searchable> datas;
	private DataFile dataFile;
    private MetaProperty titre;

    public AMPAnswerParserTest(){
        try {
            Model.getInstance();

            ModelFactory factory = Model.getInstance().getFactory();
            
            titre = new MetaProperty("titre", "toto");
            ArrayList<MetaProperty> description = new ArrayList<MetaProperty>();
            description.add(titre);
            
            
            //Data File
            dataFile = factory.createDataFile(new File("LICENSE"));
            dataFile.setDescription(description);

            // -- Data String
            data = factory.createDataString("Toto va à la plage");
            ArrayList<Data> linkedData = new ArrayList<Data>();
            linkedData.add(data);
            data.setDescription(description);

            // -- MetaProperty
            property = new MetaProperty("st", "fr");
            TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
            properties.add(property);

            // -- MetaData answer
            metaData = factory.createMetaData(
                        properties);

            // -- MetaData source
            data2 = factory.createDataString("Ma super chaine");
            data2.setDescription(description);

            // -- Search
            search = factory.createSearch(data2, metaData, linkedData);

            datas = new ArrayList<Searchable>();
            datas.add(data);
            datas.add(dataFile);
            datas.add(search);
            datas.add(metaData);
        
        } catch (ModelException e1) {
            e1.printStackTrace();
        }
    }
    
    @Test
    public void test() {
        datas = new ArrayList<Searchable>();
        datas.add(data);
    	testData(datas);
    	
    	datas = new ArrayList<Searchable>();
        datas.add(dataFile);
        testData(datas);
    	
        datas = new ArrayList<Searchable>();
        datas.add(search);
        testData(datas);
        
        datas = new ArrayList<Searchable>();
        datas.add(metaData);
        testData(datas);
        
        datas = new ArrayList<Searchable>();
        datas.add(data);
        datas.add(dataFile);
        datas.add(search);
        datas.add(metaData);
        testData(datas);
    }

	private void testData(ArrayList<Searchable> datas) {
        AMPAnswerFactory factory = new AMPAnswerFactory("12", datas);
        AMPAnswerParser parser;
        try {
            parser = new AMPAnswerParser(factory.getMessage());
            ArrayList<Searchable> dataReceived = parser.getDatas();
            for (int i=0; i<dataReceived.size(); i++) {
                Searchable searchable = dataReceived.get(i);
                if(searchable instanceof Search){
                    Search search = (Search) searchable;
                    System.out.println(search.getTmpResultsHashes());
                    System.out.println(search.getTmpLinkedData());
                }else if(searchable instanceof MetaData){
                    MetaData metaData = (MetaData) searchable;
                }else if(searchable instanceof DataFile){
                    DataFile dataFile = (DataFile) searchable;
                    Assert.assertEquals(1, dataFile.getDescription().size());
                    for(MetaProperty desc : dataFile.getDescription()){
                        Assert.assertEquals(titre.getName(), desc.getName());
                        Assert.assertEquals(titre.getValue(), desc.getValue());
                    }
                    System.out.println(dataFile.getFile().getName());
                }else if(searchable instanceof DataString){
                    DataString dataString = (DataString) searchable;
                    System.out.println(dataString.getString());
                    Assert.assertEquals(1, dataFile.getDescription().size());
                    for(MetaProperty desc : dataFile.getDescription()){
                        Assert.assertEquals(titre.getName(), desc.getName());
                        Assert.assertEquals(titre.getValue(), desc.getValue());
                    }
                }
            }
        } catch (NotAValidAMPCommand e) {
            e.printStackTrace();
        }
	}
}
