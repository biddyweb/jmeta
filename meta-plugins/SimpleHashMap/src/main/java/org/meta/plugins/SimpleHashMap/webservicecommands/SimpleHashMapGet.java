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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.api.model.Search;
import org.meta.api.model.SearchCriteria;
import org.meta.api.plugin.MetAPI;
import org.meta.api.plugin.SearchOperation;
import org.meta.api.ws.AbstractPluginWebServiceController;
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
public class SimpleHashMapGet extends AbstractWebService implements OperationListener<SearchOperation> {

    private final MetAPI api;

    InterfaceDescriptor initialDescriptor = null;
    TextOutput output = null;
    Iterator<Data> itDistantResults = null;
    Set<Data> localResults = null;

    String errorMesssage;

    private Logger logger = LoggerFactory.getLogger(SimpleHashMapGet.class);

    /**
     *
     * @param wsController
     */
    public SimpleHashMapGet(final AbstractPluginWebServiceController wsController) {
        super(wsController);
        TextInput path = new TextInput("id", "ID");
        rootColumn.addChild(path);
        output = new TextOutput("result", "Result");
        rootColumn.addChild(new SelfSubmitButton("submitToMe", "Search"));
        rootColumn.addChild(output);
        initialDescriptor = new InterfaceDescriptor(rootColumn);
        api = wsController.getAPI();
    }

    @Override
    public void executeCommand(final Map<String, String[]> map) {
        output.flush();
        String id = getParameter("id", map);

        if (id != "") {
            output.flush();
            TreeSet<MetaData> properties = new TreeSet<MetaData>();
            properties.add(new MetaData("hashmap", "value"));

            SearchCriteria metaData = api.getModel().getFactory().createCriteria(properties);
            Data source = api.getModel().getFactory().getData(id);
            Search contentSearch = api.getModel().getFactory().createSearch(source, metaData);
            logger.info("get hash : " + contentSearch.getHash().toString());

            SearchOperation operation = api.search(contentSearch.getHash(), false, true, null, null); //TODO
            operation.addListener(this);

            localResults = api.getLocalResults(contentSearch.getHash());
            redrawOutput();
        } else {
            output.flush();
            output.append("Please set a hash to lookup");
        }
    }

    @Override
    public void applySmallUpdate() {
        redrawOutput();
    }

    @Override
    public void failed(final SearchOperation operation) {
        logger.debug("Operation failed!" + operation.getFailureMessage());
        this.errorMesssage = operation.getFailureMessage();
        redrawOutput();
    }

    @Override
    public void complete(final SearchOperation operation) {
        logger.debug("Search Operation complete");
        itDistantResults = operation.iterator();
        redrawOutput();
    }

    private void redrawOutput() {
        output.flush();
        output.append("Error ?: " + errorMesssage == null ? " no errors!" : errorMesssage);

        if (!itDistantResults.hasNext()) {
            output.append("No distant results found...");
        } else {
            while (itDistantResults.hasNext()) {
                output.append("distant : " + itDistantResults.next().toString());
            }
        }
        if (localResults == null || localResults.isEmpty()) {
            output.append("No local results found...");
        } else {
            for (Data result : this.localResults) {
                output.append("local : " + result.toString());
            }
        }
        output.append("waiting for results...");
    }
}
