package org.meta.tests;

import org.meta.api.common.MetHash;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.amp.AMPAnswerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommanndTest extends PluginAMPCommand {
    private Logger logger = LoggerFactory.getLogger(CommanndTest.class);

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
//    public AMPAnswerFactory execute(String answer, String hash) {
//        logger.info("toto");
//
//        //Data File
//        DataFile dataFile = new DataFile("toto va au cinéma", new File("/home/faquin/done.txt"));
//
//
//        // -- Data String
//        DataString data = new DataString(
//                "hashData1",
//                "Toto va à la plage");
//        ArrayList<Data> linkedData = new ArrayList<Data>();
//        linkedData.add(data);
//
//        // -- MetaProperty
//        MetaProperty property = new MetaProperty("st", "fr");
//        ArrayList<MetaProperty> properties = new ArrayList<MetaProperty>();
//        properties.add(property);
//
//        // -- MetaData answer
//        MetaData metaData = new MetaData(
//                    "hashMetaData",
//                    linkedData,
//                    properties);
//        ArrayList<MetaData> results = new ArrayList<MetaData>();
//        results.add(metaData);
//
//        // -- MetaData source
//        DataString data2 = new DataString(
//                "hashData2",
//                "Ma super chaine");
//
//        // -- Search
//        Search search = new Search("hashSearch", data2, results);
//
//        ArrayList<Searchable> datas = new ArrayList<Searchable>();
//        datas.add(data);
//        datas.add(dataFile);
//        datas.add(search);
//        datas.add(metaData);
//
//        AMPAnswerFactory factory = new AMPAnswerFactory("12", datas);
//        return factory;
//    }
}
