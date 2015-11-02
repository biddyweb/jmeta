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
import java.util.Map;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.model.SearchCriteria;
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
public class AddSubtitle extends AbstractWebService {

    private InterfaceDescriptor initialDescriptor = null;
    private TextOutput output = null;
    private ModelFactory factory = null;
    private MetAPI api;
    private ArrayList<Data> results = null;
    private TextInput source = null;
    private TextInput result = null;
    private TextInput description = null;

    private Logger logger = LoggerFactory.getLogger(AddSubtitle.class);

    /**
     *
     * @param controller the web service controller
     */
    public AddSubtitle(final AbstractPluginWebServiceControler controller) {
        super(controller);
        this.api = controller.getAPI();
        factory = this.api.getModel().getFactory();

        results = new ArrayList<>();
        source = new TextInput("path", "Path to the movie");
        rootColumn.addChild(source);

        result = new TextInput("pathS", "Path to the subtitle");
        rootColumn.addChild(result);

        description = new TextInput("description", "Description of the subtitle");
        rootColumn.addChild(description);

        output = new TextOutput("result", "Result");
        rootColumn.addChild(new SelfSubmitButton("submitToMe", "add a subtitle"));
        rootColumn.addChild(output);

        initialDescriptor = new InterfaceDescriptor(rootColumn);
    }

    @Override
    public void executeCommand(final Map<String, String[]> map) {
        String srcString = getParameter(this.source.getId(), map);
        String resultString = getParameter(this.result.getId(), map);
        String desc = getParameter(this.description.getId(), map);

        if (srcString != null && resultString != null) {
            File fSource = new File(srcString);
            File fResult = new File(resultString);

            if (fSource.exists() && fResult.exists()) {
                processCreation(fResult, fSource, desc);
            } else {
                output.flush();
                output.append("Please set valid paths");
            }
        } else {
            output.flush();
            output.append("Please set both pathes");
        }
    }

    /**
     * Process DataFile creation for source and result. Process MetaSearch creation.
     *
     *
     * process a save action to the search process a DHT push on the search process a push action on the
     * content
     *
     * @param fResult the subtitle file
     * @param fSource the movie file
     * @param desc the description of the subtitle
     */
    private void processCreation(final File fResult, final File fSource, final String desc) {
        //Create the empty search
        SearchCriteria metaData = factory.createCriteria(new MetaData("st", "fr"));

        DataFile src = factory.getDataFile(fSource);

        DataFile newResult = factory.getDataFile(fResult);
        newResult.addMetaData(new MetaData("description", desc));
        newResult.addMetaData(new MetaData("name", fResult.getName()));

        newResult = (DataFile) api.consolidateData(newResult);

        Search newSearch = factory.createSearch(src, metaData, newResult);
        newSearch = api.consolidateSearch(newSearch);

        if (!api.storePush(newSearch)) {
            logger.error("Failed to store the seach into db...");
        } else {
            output.flush();
            output.append("Subtitle registered successfully.");
        }
    }

    @Override
    public void applySmallUpdate() {
    }

}
