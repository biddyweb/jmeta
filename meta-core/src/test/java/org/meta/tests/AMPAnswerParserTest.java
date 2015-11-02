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

/**
 *
 * @author nico
 */
public class AMPAnswerParserTest extends MetaBaseTests {

//    private MetaSearch search;
//    private Data data2;
//    private SearchCriteria metaData;
//    private MetaData property;
//    private Data dataString;
//    private ArrayList<Searchable> datas;
//    private DataFile dataFile;
//    private MetaData titre;
//    private ArrayList<Data> linkedData;
//    private Logger logger = LoggerFactory.getLogger(AMPAnswerParserTest.class);
//    private MetaModelStorage model = null;
//    private MetaObjectModelFactory factory;
//
//    /**
//     *
//     */
//    public AMPAnswerParserTest() {
//        try {
//            MetaStorage storage = new KyotoCabinetStorage(MetaConfiguration.getModelConfiguration());
//            model = new MetaModelStorage(storage);
//
//            factory = model.getFactory();
//
//            titre = new MetaData("titre", "toto");
//            ArrayList<MetaData> description = new ArrayList<MetaData>();
//            description.add(titre);
//
//            //Data File
//            dataFile = factory.createDataFile(new File("/home/nico/Documents/projects/jmeta/meta-core/db/meta.kch"));
//            dataFile.setMetaData(description);
//
//            // -- GenericData String
//            dataString = factory.getData("Toto va a la plage");
//            linkedData = new ArrayList<Data>();
//            linkedData.add(dataString);
//            dataString.setMetaData(description);
//
//            // -- MetaData
//            property = new MetaData("st", "fr");
//            TreeSet<MetaData> properties = new TreeSet<MetaData>();
//            properties.add(property);
//
//            // -- SearchCriteria answer
//            metaData = factory.createCriteria(
//                    properties);
//
//            // -- SearchCriteria source
//            data2 = factory.getData("Ma super chaine");
//            data2.setMetaData(description);
//
//            // -- MetaSearch
//            search = factory.createSearch(data2, metaData, linkedData);
//
//            datas = new ArrayList<Searchable>();
//            datas.add(dataString);
//            datas.add(dataFile);
//            datas.add(search);
//            datas.add(metaData);
//
//        } catch (StorageException e1) {
//            logger.error(e1.getMessage(), e1);
//        }
//    }
//
//    /**
//     *
//     */
//    //@Test
//    public void test() {
//        datas = new ArrayList<Searchable>();
//        datas.add(dataString);
//        testData(datas);
//
//        datas = new ArrayList<Searchable>();
//        datas.add(dataFile);
//        testData(datas);
//
//        datas = new ArrayList<Searchable>();
//        datas.add(search);
//        testData(datas);
//
//        datas = new ArrayList<Searchable>();
//        datas.add(metaData);
//        testData(datas);
//
//        datas = new ArrayList<Searchable>();
//        datas.add(dataString);
//        datas.add(dataFile);
//        datas.add(search);
//        datas.add(metaData);
//        testData(datas);
//    }
//
//    private void testData(ArrayList<Searchable> datas) {
//        AMPAnswerBuilder factory = new AMPAnswerBuilder("12", datas);
//        AMPAnswerParser parser;
//        try {
//            parser = new AMPAnswerParser(factory.getMessage(), model.getFactory());
//            ArrayList<Searchable> dataReceived = parser.getDatas();
//            for (int i = 0; i < dataReceived.size(); i++) {
//                Searchable searchable = dataReceived.get(i);
//                if (searchable instanceof MetaSearch) {
//                    MetaSearch search = (MetaSearch) searchable;
//                    Assert.assertEquals(this.search.getHash().toString(), search.getHash().toString());
//                    Assert.assertEquals(metaData.getHash().toString(), search.getTmpmetaDataHash());
//                    int count = 0;
//                    for (String linked : search.getTmpLinkedData()) {
//                        Assert.assertEquals(linkedData.get(count).getHash().toString(), linked);
//                        count++;
//                    }
//                } else if (searchable instanceof SearchCriteria) {
//                    SearchCriteria metaData = (SearchCriteria) searchable;
//                    Assert.assertEquals(this.metaData.getHash(), metaData.getHash());
//                } else if (searchable instanceof DataFile) {
//                    DataFile dataFile = (DataFile) searchable;
//                    Assert.assertEquals(this.dataFile.getHash(), dataFile.getHash());
//                    Assert.assertEquals(1, dataFile.getMetaData().size());
//                    for (MetaData desc : dataFile.getMetaData()) {
//                        Assert.assertEquals(titre.getKey(), desc.getKey());
//                        Assert.assertEquals(titre.getValue(), desc.getValue());
//                    }
//
//                    try {
//                        byte[] b1 = Files.readAllBytes(Paths.get(dataFile.getFile().getPath()));
//                        byte[] b2 = Files.readAllBytes(Paths.get(this.dataFile.getFile().getPath()));
//                        Assert.assertArrayEquals(b2, b1);
//                    } catch (IOException e) {
//                        Assert.fail(e.getMessage());
//                    }
//                } else if (searchable instanceof Data) {
//                    Data dataString = (Data) searchable;
//                    Assert.assertEquals(this.dataString.getHash(), dataString.getHash());
//                    Assert.assertEquals(this.dataString.getString(), dataString.getString());
//
//                    Assert.assertEquals(1, dataString.getMetaData().size());
//                    for (MetaData desc : dataString.getMetaData()) {
//                        Assert.assertEquals(titre.getKey(), desc.getKey());
//                        Assert.assertEquals(titre.getValue(), desc.getValue());
//                    }
//                }
//            }
//        } catch (InvalidAMPCommand e) {
//            logger.error(e.getMessage(), e);
//        }
//    }
}
