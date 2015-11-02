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
package org.meta.plugins.SimpleHashMap.webservicecommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.StoreOperation;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.api.model.Search;
import org.meta.api.model.SearchCriteria;
import org.meta.api.model.Searchable;
import org.meta.api.plugin.MetAPI;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.api.ws.AbstractWebService;
import org.meta.api.ws.forms.InterfaceDescriptor;
import org.meta.api.ws.forms.fields.TextInput;
import org.meta.api.ws.forms.fields.TextOutput;
import org.meta.api.ws.forms.submit.SelfSubmitButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class SimpleHashMapPut extends AbstractWebService {

    private final MetAPI api;

    InterfaceDescriptor initialDescriptor = null;
    TextOutput output = null;
    ArrayList<Data> results = null;
    private Logger logger = LoggerFactory.getLogger(SimpleHashMapPut.class);

    /**
     *
     * @param wsController
     */
    public SimpleHashMapPut(AbstractPluginWebServiceControler wsController) {
        super(wsController);
        results = new ArrayList<Data>();
        TextInput path = new TextInput("id", "ID");
        rootColumn.addChild(path);
        TextInput content = new TextInput("content", "Content");
        rootColumn.addChild(content);
        output = new TextOutput("result", "Result");
        rootColumn.addChild(new SelfSubmitButton("submitToMe", "put"));
        rootColumn.addChild(output);
        initialDescriptor = new InterfaceDescriptor(rootColumn);
        api = wsController.getAPI();
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        String id = getParameter("id", map);
        String content = getParameter("content", map);

        if (content != null && id != null) {
            output.flush();
            Data res = api.getModel().getFactory().getData(content);
            List<Data> lst = new ArrayList<Data>();
            lst.add(res);
            TreeSet<MetaData> properties = new TreeSet<MetaData>();
            properties.add(new MetaData("hashmap", "value"));
            SearchCriteria metaData = api.getModel().getFactory().createCriteria(properties);
            Data source = api.getModel().getFactory().getData(id);
            Search hashM = api.getModel().getFactory().createSearch(source, metaData, lst);
            logger.info("Put search hash : " + hashM.getHash().toString());
            logger.info("Put result hash : " + res.getHash().toString());

            //write into dataBase
            //and store it to the DHT
            if (!api.getModel().set(hashM)) {
                logger.debug("Failed to store search into model...");
                output.append("Failed to store search into model...");
            }
            Search test = api.getModel().getSearch(hashM.getHash());
            if (test != null) {
                if (test.getResults() != null || !test.getResults().isEmpty()) {
                    for (Data data : test.getResults()) {
                        logger.debug("JUST PUT SEARCH RESULT :" + data.getHash() + " content = " + data.toString());
                    }
                } else {
                    logger.debug("WTTTTFFFF ???? results null");
                }
            } else {
                logger.debug("WTTTTFFFF ????");
            }
            api.getDHT().store(hashM.getHash()).addListener(
                    new OperationListener<StoreOperation>() {

                        @Override
                        public void failed(StoreOperation operation) {
                            output.append("fail to push");
                        }

                        @Override
                        public void complete(StoreOperation operation) {
                            output.append("success to push");
                        }

                    });
        } else {
            output.flush();
            output.append("Please set an id");
        }
    }

    @Override
    public void applySmallUpdate() {
    }

    //@Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        output.flush();
        //Those results are incomplete
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();
            if (searchable instanceof Search) {
                Search search = (Search) searchable;
                Collection<Data> linkDatas = search.getResults();
                for (Iterator<Data> k = linkDatas.iterator(); k.hasNext();) {
                    Data data = (Data) k.next();
                    if (data instanceof Data) {
                        this.results.add((Data) data);
                    }
                }
            }
        }
        for (Data result : this.results) {
            output.append(result.toString());
        }
        output.append("waiting for results");
    }

//    @Override
//    public void callbackFailure(String failureMessage) {
//        // TODO Auto-generated method stub
//
//    }
}
