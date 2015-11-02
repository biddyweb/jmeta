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
package org.meta.plugin.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import org.meta.api.amp.AMPResponseCallback;
import org.meta.api.model.Data;
import org.meta.api.model.Searchable;
import org.meta.model.MetaObjectModelFactory;
import org.meta.model.MetaSearch;
import org.meta.plugin.tcp.amp.AMPAnswerParser;
import org.meta.plugin.tcp.amp.AMPAskBuilder;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class AnswerSenderThread implements Runnable {

    private InetAddress address = null;
    private AMPAskBuilder ask = null;
    private ArrayList<Searchable> results = null;
    private int port = 0;
    private MetaObjectModelFactory factory = null;
    private Logger logger = LoggerFactory.getLogger(AnswerSenderThread.class);
    private AMPResponseCallback listener = null;

    /**
     *
     * @param askBuilder Question
     * @param addr address to contact
     * @param p contact port
     * @param callback who to call back
     * @param modelFactory
     *
     * TODO replace InetAdress/port couple by MetaPeer
     */
    public AnswerSenderThread(final AMPAskBuilder askBuilder,
            final InetAddress addr,
            final int p,
            final AMPResponseCallback callback,
            final MetaObjectModelFactory modelFactory) {
        this.ask = askBuilder;
        this.address = addr;
        this.port = p;
        this.listener = callback;
        this.results = new ArrayList<>();
        this.factory = modelFactory;
    }

    /**
     *
     */
    public void run() {
        try {
            // Open a connection- to the pair
            Socket client = new Socket(address, port);
            // write the message
            OutputStream os = client.getOutputStream();
            os.write(ask.getMessage());
            client.shutdownOutput();

            // wait for an answer
            InputStream is = client.getInputStream();
            //Open the input stream
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int count = 0;
            while ((count = is.read()) != -1) {
                buffer.write(count);
            }
            if (buffer.size() > 0) {
                //parse it into an answer
                AMPAnswerParser parser = new AMPAnswerParser(buffer.toByteArray(), factory);
                this.results = parser.getDatas();
                /*
                 * When results are retrieved.
                 * MetaSearch objects are incompletes,
                 * Iterate, find search, and look if an Element inside the rest
                 * of retrieved datas looks like the search's child data
                 */
                for (Searchable searchable : this.results) {
                    if (searchable instanceof MetaSearch) {
                        MetaSearch search = (MetaSearch) searchable;
//                        Searchable source = searchElement(this.results, search.getTmpSourceHash());
//                        SearchCriteria metaData = (SearchCriteria) searchElement(this.results,
//                                search.getTmpmetaDataHash());
                        ArrayList<Data> linked = new ArrayList<>();
//                        for (String link : search.getTmpLinkedData()) {
//                            Searchable s = searchElement(this.results, link);
//                            if (s != null) {
//                                linked.add((Data) s);
//                            }
//                        }
                        //factory.updateFromNewtork(search, source, metaData, linked);
                    }
                }
            }
            //close everything that use memory
            buffer.flush();
            buffer.close();
            client.close();
            is.close();
        } catch (IOException | InvalidAMPCommand e) {
            logger.warn(e.getMessage());
        }

        listener.callbackSuccess(results);
    }

    /**
     * Look if an element pointed by his hash is present in a searchable list.
     *
     * @param res searchable list
     * @param hash hash we're looking for
     * @return the element if found, null otherwise
     */
    private Searchable searchElement(final ArrayList<Searchable> res, final String hash) {
        Searchable element = null;
        for (int i = 0; i < res.size() && element == null; i++) {
            Searchable e = res.get(i);
            if (e.getHash().toString().equals(hash)) {
                element = e;
                break;
            }
        }
        return element;
    }
}
