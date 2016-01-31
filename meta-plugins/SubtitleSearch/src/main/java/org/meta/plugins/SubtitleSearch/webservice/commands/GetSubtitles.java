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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.plugin.DownloadOperation;
import org.meta.api.plugin.MetAPI;
import org.meta.api.ws.AbstractPluginWebServiceController;
import org.meta.api.ws.AbstractWebService;
import org.meta.api.ws.forms.fields.TextInput;
import org.meta.api.ws.forms.fields.TextOutput;
import org.meta.api.ws.forms.submit.SelfSubmitButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>GetSubtitles class.</p>
 *
 * @author nico
 * @version $Id: $Id
 */
public class GetSubtitles extends AbstractWebService implements OperationListener<DownloadOperation> {

    private final Logger logger = LoggerFactory.getLogger(GetSubtitles.class);

    private TextInput subtitleHash = null;
    private SelfSubmitButton submitToMe = null;
    private TextOutput errorTextOutput = null;
    private TextOutput successTextOutput = null;
    private final MetAPI api;
    private ModelFactory factory = null;
    private Search subtitleSearch = null;
    private String failure;

    /**
     * <p>Constructor for GetSubtitles.</p>
     *
     * @param controller parent controller
     */
    public GetSubtitles(final AbstractPluginWebServiceController controller) {
        super(controller);
        api = controller.getAPI();
        factory = api.getModel().getFactory();

        //Path to the movie
        subtitleHash = new TextInput("subtitleHash", "Hash of the subtitle");
        subtitleHash.setDescription("If you don't know what to do here, "
                + "make a full search from searchSubtitle");
        rootColumn.addChild(subtitleHash);

        submitToMe = new SelfSubmitButton("submitToMe", "Download");
        //had a linked button on himself
        rootColumn.addChild(submitToMe);

        //tex output
        errorTextOutput = new TextOutput("errorOutput", "Errors");
        rootColumn.addChild(errorTextOutput);

        //tex output
        successTextOutput = new TextOutput("successOutput", "Success");
        rootColumn.addChild(successTextOutput);
    }

    /** {@inheritDoc} */
    @Override
    protected void executeCommand(final Map<String, String[]> map) {
        errorTextOutput.flush();
        successTextOutput.flush();
        //Get path to the movie
        String moviePath = getParameter("path", map);
        String paramHash = getParameter(this.subtitleHash.getId(), map);
        this.subtitleHash.setValue(paramHash);

        /*
         * if we are in the user workflow and we have a path to the movie, so
         * we can rebuild the search to store the future result in a clean way
         */
//        if (moviePath != null) {
//            logger.info("moviePath = " + moviePath);
//            SearchCriteria criteria = factory.createCriteria(new MetaData("st", "fr"));
//
//            //instanciate a new DataFile Object
//            DataFile movie = factory.getDataFile(new File(moviePath));
//
//            //create a new search with the movie as source
//            subtitleSearch = factory.createSearch(movie, criteria);
//            //if a DB element exist, prefer it
//            Search dbSearch = api.getModel().getSearch(subtitleSearch.getHash());
//            if (dbSearch != null) {
//                subtitleSearch = dbSearch;
//            }
//        }
        if (paramHash != null && !paramHash.isEmpty()) {
            logger.info("hash = " + paramHash);
            MetHash hash = new MetHash(paramHash);

            //TESTING Context-passing between plugin commands
            Map<String, Object> context = this.controller.getContext();
            Map<MetHash, Data> results = (Map<MetHash, Data>) context.get("results");
            Collection<MetaPeer> peers = (Collection<MetaPeer>) context.get("peers");
            successTextOutput.append("Hash: " + hash);
            Data data = results.get(hash);
            if (data != null) {
                Path downloadPath = Paths.get("", getFileName(data));
                DataFile destinationFile = factory.getDataFile(hash, downloadPath.toUri(), data.getSize());
                successTextOutput.append("Downloading to: " + downloadPath);
                api.download(destinationFile, peers).addListener(this);
            } else {
                successTextOutput.append("Unknown data! ");
            }
        } else {
            errorTextOutput.append("Please set a valide path name");
        }
    }

    private String getFileName(final Data data) {
        String name = "meta_subtitle_download_";

        MetaData mdName = data.getMetaData("name");
        if (mdName != null) {
            name += mdName.getValue();
        }
        name += "." + System.currentTimeMillis();
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void applySmallUpdate() {
    }

//    public void callbackSuccess(ArrayList<Searchable> results) {
//        /*
//         * On success, if there is results,
//         */
//        if (results.size() > 0) {
//            if (results.get(0) instanceof DataFile) {
//                //Get the first as subtitle
//                DataFile subtitle = (DataFile) results.get(0);
//                successTextOutput.append("New subtitle "
//                        + subtitle.getFile().getName()
//                        + " download to "
//                        + subtitle.getFile().getPath());
//                /*
//                 * If subtitle search !=null, it mean that we where able to
//                 * rebuild it, and we are in the search subtitle workflow.
//                 * We need to change the search value in DB
//                 * In this case, it means adding a new subtitle in the dataBase
//                 */
////                subtitle = (DataFile) super.consolidateData(subtitle);
////                if (subtitleSearch != null) {
////                    subtitleSearch = super.updateSearch(subtitleSearch, subtitle);
////                    super.storePush(subtitleSearch);
////                } else {
////                    super.onlySave(subtitle);
////                }
////                super.onlyPush(subtitle);
//            }
//        }
//    }
    /** {@inheritDoc} */
    @Override
    public void failed(final DownloadOperation operation) {
        errorTextOutput.flush();
        errorTextOutput.append("Download failed! Error: " + operation.getFailureMessage());
    }

    /** {@inheritDoc} */
    @Override
    public void complete(final DownloadOperation operation) {
        successTextOutput.append("Download complete! See file: " + operation.getFile().getURI());
    }
}
