/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import org.meta.api.amp.AMPAnswerBuilder;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.DataString;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaProperty;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.configuration.MetaConfiguration;
import org.meta.model.KyotoCabinetModel;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.tcp.amp.AMPAnswerParser;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class AMPAnswerParserTest extends MetaBaseTests {

    private Search search;
    private DataString data2;
    private MetaData metaData;
    private MetaProperty property;
    private DataString dataString;
    private ArrayList<Searchable> datas;
	private DataFile dataFile;
    private MetaProperty titre;
    private ArrayList<Data> linkedData;
    private Logger logger = LoggerFactory.getLogger(AMPAnswerParserTest.class);
    private KyotoCabinetModel  model  = null;
    private ModelFactory factory;

    /**
     *
     */
    public AMPAnswerParserTest(){
        try {
            model = new KyotoCabinetModel(MetaConfiguration.getModelConfiguration());

            factory = model.getFactory();
            
            titre = new MetaProperty("titre", "toto");
            ArrayList<MetaProperty> description = new ArrayList<MetaProperty>();
            description.add(titre);
            
            
            //Data File
            dataFile = factory.createDataFile(new File("/etc/hosts"));
            dataFile.setDescription(description);

            // -- Data String
            dataString = factory.createDataString("Toto va a la plage");
            linkedData = new ArrayList<Data>();
            linkedData.add(dataString);
            dataString.setDescription(description);

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
            datas.add(dataString);
            datas.add(dataFile);
            datas.add(search);
            datas.add(metaData);
        
        } catch (ModelException e1) {
            logger.error(e1.getMessage(), e1);
        }
    }
    
    /**
     *
     */
    @Test
    public void test() {
        datas = new ArrayList<Searchable>();
        datas.add(dataString);
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
        datas.add(dataString);
        datas.add(dataFile);
        datas.add(search);
        datas.add(metaData);
        testData(datas);
    }

	private void testData(ArrayList<Searchable> datas) {
        AMPAnswerBuilder factory = new AMPAnswerBuilder("12", datas);
        AMPAnswerParser parser;
        try {
            parser = new AMPAnswerParser(factory.getMessage(), model.getFactory());
            ArrayList<Searchable> dataReceived = parser.getDatas();
            for (int i=0; i<dataReceived.size(); i++) {
                Searchable searchable = dataReceived.get(i);
                if(searchable instanceof Search){
                    Search search = (Search) searchable;
                    Assert.assertEquals(this.search.getHash().toString(), search.getHash().toString());
                    Assert.assertEquals(metaData.getHash().toString(), search.getTmpmetaDataHash());
                    int count=0;
                    for(String linked : search.getTmpLinkedData()){
                        Assert.assertEquals(linkedData.get(count).getHash().toString(), linked);
                        count++;
                    }
                }else if(searchable instanceof MetaData){
                    MetaData metaData = (MetaData) searchable;
                    Assert.assertEquals(this.metaData.getHash(), metaData.getHash());
                }else if(searchable instanceof DataFile){
                    DataFile dataFile = (DataFile) searchable;
                    Assert.assertEquals(this.dataFile.getHash(), dataFile.getHash());
                    Assert.assertEquals(1, dataFile.getDescription().size());
                    for(MetaProperty desc : dataFile.getDescription()){
                        Assert.assertEquals(titre.getName(), desc.getName());
                        Assert.assertEquals(titre.getValue(), desc.getValue());
                    }
                    
                    try {
                        byte[] b1 = Files.readAllBytes(Paths.get(dataFile.getFile().getPath()));
                        byte[] b2 = Files.readAllBytes(Paths.get(this.dataFile.getFile().getPath()));
                        Assert.assertArrayEquals(b2, b1);
                    } catch (IOException e) {
                        Assert.fail(e.getMessage());
                    }
                }else if(searchable instanceof DataString){
                    DataString dataString = (DataString) searchable;
                    Assert.assertEquals(this.dataString.getHash(), dataString.getHash());
                    Assert.assertEquals(this.dataString.getString(), dataString.getString());
                    
                    Assert.assertEquals(1, dataString.getDescription().size());
                    for(MetaProperty desc : dataString.getDescription()){
                        Assert.assertEquals(titre.getName(), desc.getName());
                        Assert.assertEquals(titre.getValue(), desc.getValue());
                    }
                }
            }
        } catch (InvalidAMPCommand e) {
            logger.error(e.getMessage(), e);
        }
	}
}
