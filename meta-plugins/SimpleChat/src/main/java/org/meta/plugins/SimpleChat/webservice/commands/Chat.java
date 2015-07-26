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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.StoreOperation;
import org.meta.api.model.Data;
import org.meta.api.model.DataString;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaProperty;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
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
public class Chat extends AbstractWebService {

    InterfaceDescriptor initialDescriptor = null;
    TextOutput output = null;
    ModelFactory factory = null;
    MetaData chat = null;
    private Search retrieveMessage = null;
    private TextInput path;
    private TextInput nick;
    private TreeMap<Date, String> results = null;
    private SimpleDateFormat sdf = null;
    private Logger logger = LoggerFactory.getLogger(Chat.class);

    /**
     *
     * @param controler
     */
    public Chat(AbstractPluginWebServiceControler controler) {
        super(controler);
        results = new TreeMap<Date, String>();
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

        factory = super.controller.getModel().getFactory();

        TreeSet<MetaProperty> props = new TreeSet<MetaProperty>();
        props.add(new MetaProperty("chat", "channel"));
        chat = factory.createMetaData(props);
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        String channel = getParameter(path.getId(), map);
        String message = getParameter("message", map);
        String nickName = getParameter(nick.getId(), map);

        path.setValue(channel);
        nick.setValue(nickName);

        if (channel != null && !channel.equals("")) {
            DataString channelName = factory.createDataString(channel);
            retrieveMessage = factory.createSearch(channelName, chat, null);
            if (message != null && !message.equals("")) {
                Date timeStamp = new Date();
                Data messageToSave = factory.createDataString("" + nickName + ";" + timeStamp.getTime() + ";" + message);
                results.put(timeStamp, nickName + " : " + message);
                Search searchToSave = factory.createSearch(channelName, chat, Collections.singletonList(messageToSave));
                //write into dataBase
                //and store it to the DHT
                super.controller.getModel().set(searchToSave);
                super.controller.getDht().store(searchToSave.getHash()).addListener(
                        new OperationListener<StoreOperation>() {

                            @Override
                            public void failed(StoreOperation operation) {
                                output.append("fail to push");
                            }

                            @Override
                            public void complete(StoreOperation operation) {
                                output.append("succes to push");
                            }

                        });
            }
            super.controller.search(retrieveMessage.getHash(),
                    "SimpleChat",
                    "getLastMessages",
                    this);

        }
        redrawOutput();
    }

    @Override
    public void applySmallUpdate() {
        super.controller.search(retrieveMessage.getHash(),
                "SimpleChat",
                "getLastMessages",
                this);
    }

    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        //Those results are incomplete ==> why ?
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();

            if (searchable instanceof Search) {
                Search search = (Search) searchable;
                Collection<Data> linkDatas = search.getLinkedData();

                for (Iterator<Data> k = linkDatas.iterator(); k.hasNext();) {
                    Data data = (Data) k.next();
                    if (data instanceof DataString) {
                        DataString distantMessage = (DataString) data;
                        String baseMessage = distantMessage.getString();
                        String[] split = baseMessage.split(";");
                        String pseudo = split[0];
                        Date sDate = new Date(Long.parseLong(split[1]));
                        String msg = split[2];
                        this.results.put(sDate, pseudo + " : " + msg);
                    }

                }
            }
        }
        redrawOutput();
    }

    @Override
    public void callbackFailure(String failureMessage) {
        // TODO Auto-generated method stub

    }

    private void redrawOutput() {
        output.flush();
        for (Entry<Date, String> entry : this.results.entrySet()) {
            output.append(sdf.format(entry.getKey()) + " " + entry.getValue());
        }
    }
}
