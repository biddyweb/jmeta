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
package org.meta.plugins.SubtitleSearch.webservice.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.model.SearchCriteria;
import org.meta.api.plugin.MetAPI;
import org.meta.api.plugin.SearchOperation;
import org.meta.api.ws.AbstractPluginWebServiceController;
import org.meta.api.ws.AbstractWebService;
import org.meta.api.ws.forms.fields.TextInput;
import org.meta.api.ws.forms.fields.TextOutput;
import org.meta.api.ws.forms.fields.radio.RadioButton;
import org.meta.api.ws.forms.fields.radio.RadioList;
import org.meta.api.ws.forms.submit.SelfSubmitButton;
import org.meta.api.ws.forms.submit.SubmitToButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class SearchSubtitles extends AbstractWebService implements OperationListener<SearchOperation> {

    private final Logger logger = LoggerFactory.getLogger(SearchSubtitles.class);

    private TextOutput initialTextOutput = null;
    private ModelFactory factory = null;
    private MetAPI api;
    private TextInput path = null;
    private Map<MetHash, Data> results = null;
    private SelfSubmitButton submitToMe = null;
    private SubmitToButton getSubtitleButton = null;
    private RadioList resultsOutput = null;
    private String failure = null;

    private final Set<String> subtitleMetaKeys;

    /**
     *
     * @param controller the parent web service controller
     */
    public SearchSubtitles(final AbstractPluginWebServiceController controller) {
        super(controller);
        this.api = controller.getAPI();
        factory = this.api.getModel().getFactory();

        //Path to the movie
        path = new TextInput("path", "Path to the movie");
        path.setDescription("Point to the file on your hardrive");
        rootColumn.addChild(path);

        submitToMe = new SelfSubmitButton("submitToMe", "Search");
        //had a linked button on himself
        rootColumn.addChild(submitToMe);

        //tex output
        initialTextOutput = new TextOutput("initialStateOutput", "callback :");
        rootColumn.addChild(initialTextOutput);

        results = new HashMap<>();

        subtitleMetaKeys = new HashSet<>(2);
        subtitleMetaKeys.add("description");
        subtitleMetaKeys.add("name");
    }

    @Override
    public void executeCommand(final Map<String, String[]> map) {
        //initiate state
        initialTextOutput.flush();
        rootColumn.removeChild(resultsOutput);
        resultsOutput = null;
        rootColumn.removeChild(getSubtitleButton);
        getSubtitleButton = null;
        //Get file path
        String paramPath = getParameter(this.path.getId(), map);

        //set retrieving path as new defaults value of path field
        this.path.setValue(paramPath);

        //if path is not empty, try to search othrerwise just return the main
        //interface
        if (paramPath != null && !paramPath.isEmpty()) {
            //Only go further if the file exist
            File file = new File(paramPath);
            if (file.exists()) {

                //instanciate a new SearchCriteria st:<choosen language>
                SearchCriteria metaData = factory.createCriteria(new MetaData("st", "fr"));

                //instanciate a new DataFile Object
                DataFile movieFile = factory.getDataFile(file);

                //create a new search with in input the DataFile and in output
                //the metaData
                Search subtitleSearch = factory.createSearch(movieFile, metaData);

                //lookup on the network to find the subtitles
                this.api.search(subtitleSearch.getHash(), false, false, subtitleMetaKeys, null).addListener(this);//TODO
            } else {
                initialTextOutput.append("The file does not exist");
            }
        } else {
            initialTextOutput.append("Please set a valide path name");
        }
        //Change the label of the selfSubmit to me to "make a new search"
        submitToMe.setLabel("Make a new search");
    }

    @Override
    public void applySmallUpdate() {
    }

    @Override
    public void failed(final SearchOperation operation) {
        logger.info("Search operation failed!");
        failure = operation.getFailureMessage();
    }

    @Override
    public void complete(final SearchOperation operation) {
        logger.info("Search complete! Results:  ");

        this.results.clear();
        for (Data data : operation.getResults()) {
            this.results.put(data.getHash(), data);
        }
        //FOr test.
        this.controller.getContext().put("results", this.results);
        this.controller.getContext().put("peers", operation.getPeers());
        redrawOutPut();
    }

    private void redrawOutPut() {
        /*
         * At first successful callback, initialize the resultOutput radioList
         * and add the button to submit to the getCommand
         */
        if (this.failure != null && !this.failure.isEmpty()) {
            initialTextOutput.flush();
            initialTextOutput.append("Error: " + failure);
        }
        if (resultsOutput == null) {
            resultsOutput = new RadioList("subtitleHash", "Pick a subtitle");
            rootColumn.addChild(resultsOutput);
        }
        if (getSubtitleButton == null) {
            getSubtitleButton = new SubmitToButton(
                    "send",
                    "Download selected subtitle",
                    "getSubtitles");
            rootColumn.addChild(getSubtitleButton);
        }
        if (results.isEmpty()) {
            initialTextOutput.append("No subtitles found!");
        }
        //Feed the output with the results
        ArrayList<RadioButton> buttons = new ArrayList<>();
        for (Data data : results.values()) {
            String description = extractDescription(data);
            buttons.add(new RadioButton(data.getHash().toString(), description));
        }
        resultsOutput.setButtons(buttons);
    }

    /**
     *
     * @param properties
     * @return the printable description for the data
     */
    private String extractDescription(final Data data) {
        String description = "Subtitle: ";

        MetaData md = data.getMetaData("description");
        if (md != null) {
            description += " description(" + md.getValue() + ") ";
        }
        md = data.getMetaData("name");
        if (md != null) {
            description += " name(" + md.getValue() + ") ";
        }
        description += " size(" + data.getSize() + " bytes)";
        return description;
    }

}
