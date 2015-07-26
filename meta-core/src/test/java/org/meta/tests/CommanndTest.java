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

import org.meta.api.amp.AMPAnswerBuilder;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.common.MetHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
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
