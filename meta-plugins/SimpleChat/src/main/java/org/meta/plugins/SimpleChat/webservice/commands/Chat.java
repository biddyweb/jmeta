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
package org.meta.plugins.SimpleChat.webservice.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.StoreOperation;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.api.model.ModelFactory;
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
public class Chat extends AbstractWebService implements OperationListener<SearchOperation> {

    private final Logger logger = LoggerFactory.getLogger(Chat.class);

    private InterfaceDescriptor initialDescriptor = null;
    private TextOutput output = null;
    private SearchCriteria chat = null;
    private Search channelSearch = null;
    private TextInput path;
    private TextInput nick;
    private Map<Long, String> results = null;
    private SimpleDateFormat sdf = null;

    private String errorMessage;

    private final ModelFactory modelFactory;

    private final MetAPI api;

    /**
     *
     * @param controller the ws controller
     */
    public Chat(final AbstractPluginWebServiceController controller) {
        super(controller);
        api = controller.getAPI();
        modelFactory = api.getModel().getFactory();

        results = new TreeMap<>();
        sdf = new SimpleDateFormat("dd /MM / yyyy HH : mm : ss");

        path = new TextInput("channel", "Channel room");
        rootColumn.addChild(path);

        nick = new TextInput("nickName", "Nick name");
        rootColumn.addChild(nick);

        output = new TextOutput("chat", "Chat");
        rootColumn.addChild(output);

        TextInput content = new TextInput("message", "Message");
        rootColumn.addChild(content);

        rootColumn.addChild(new SelfSubmitButton("send", "Send"));

        TreeSet<MetaData> props = new TreeSet<MetaData>();
        props.add(new MetaData("chat", "channel"));
        chat = modelFactory.createCriteria(props);
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        String channel = getParameter(path.getId(), map);
        String message = getParameter("message", map);
        String nickName = getParameter(nick.getId(), map);

        path.setValue(channel);
        nick.setValue(nickName);

        results.clear();

        if (channel != null && !channel.equals("")) {
            Data channelName = modelFactory.getData(channel);

            channelSearch = modelFactory.createSearch(channelName, chat);
            Search search = api.getModel().getSearch(channelSearch.getHash());
            if (search != null) {
                channelSearch = search;
            }

            if (message != null && !message.equals("")) {
                Date timeStamp = new Date();
                Data messageToSave = modelFactory.getData("" + nickName + ";"
                        + timeStamp.getTime() + ";" + message);
                //results.put(timeStamp.getTime(), nickName + " : " + message);
                channelSearch.addResult(messageToSave);
                //write into dataBase
                //and store it to the DHT
                if (!api.getModel().set(channelSearch)) {
                    logger.warn("Failed to set search into model");
                }
                api.getDHT().store(channelSearch.getHash()).addListener(
                        new OperationListener<StoreOperation>() {

                            @Override
                            public void failed(final StoreOperation operation) {
                                output.append("fail to push");
                            }

                            @Override
                            public void complete(final StoreOperation operation) {
                                output.append("succes to push");
                            }
                        });
            }
        }
        //ApplySmallUpdate does the search
        applySmallUpdate();
        redrawOutput();
    }

    @Override
    public void failed(final SearchOperation operation) {
        logger.warn("Search operation failed");
        this.errorMessage = operation.getFailureMessage();
        redrawOutput();
    }

    @Override
    public void complete(final SearchOperation operation) {
        Set<Data> searchResults = operation.getResults();

        for (Data result : searchResults) {
            String baseMessage = result.toString();
            String[] split = baseMessage.split(";");
            String pseudo = split[0];
            Date sDate = new Date(Long.parseLong(split[1]));
            String msg = split[2];
            this.results.put(sDate.getTime(), "<b>" + pseudo + "</b> : " + msg);
        }
        redrawOutput();
    }

    @Override
    public void applySmallUpdate() {
        if (channelSearch != null) {
            api.search(channelSearch.getHash(), true, true, null).addListener(this);
        }
    }

    private void redrawOutput() {
        output.flush();
        if (errorMessage != null) {
            output.append("Error in execution! : " + errorMessage);
            output.append("");
        }
        for (String msg : this.results.values()) {
            output.append(msg);
        }
    }
}
