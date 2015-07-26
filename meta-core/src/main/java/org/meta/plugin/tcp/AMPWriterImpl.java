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

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.meta.api.amp.AMPResponseCallback;
import org.meta.api.amp.AMPWriter;
import org.meta.api.common.MetHash;
import org.meta.api.configuration.AMPConfiguration;
import org.meta.api.model.ModelFactory;
import org.meta.plugin.tcp.amp.AMPAskBuilder;

/**
 * Used to contact a peer using AMP protocol.
 *
 * @author faquin
 *
 */
public class AMPWriterImpl implements AMPWriter {

    private int lastAsk = 0;
    private ExecutorService executor = null;
    private ModelFactory factory = null;

    /**
     * The global AMP configuration.
     */
    private final AMPConfiguration configuration;

    /**
     *
     * @param config
     * @param factory
     */
    public AMPWriterImpl(AMPConfiguration config, ModelFactory factory) {
        this.configuration = config;
        this.factory = factory;
        this.executor = Executors.newFixedThreadPool(
                this.configuration.getSenderThPoolSize());
    }

    /**
     *
     * @param factory
     */
    public void setFactory(ModelFactory factory) {
        this.factory = factory;
    }

    /**
     * Ask a question to the given address, with the given parameters
     *
     * @param adress who do we call ?
     * @param port on wich port do we call ?
     * @param plugin which plugin is concerned ?
     * @param command what command to we ask him to execute ?
     * @param hash on wich hash ?
     * @param listenner after getting result, who is getting the callback ?
     * @return the sender thread for join purposes
     */
    @Override
    public Future<?> askTo(InetAddress adress,
            String plugin,
            String command,
            MetHash hash,
            AMPResponseCallback listenner,
            int port) {
        //each command is "unique" at least where its send
        //TODO use unique ID here
        lastAsk++;
        //Forge a new AMP ask
        AMPAskBuilder ask = new AMPAskBuilder(lastAsk + "", plugin, command, hash);
        //Let the sender thread do the rest
        AnswerSenderThread sender = new AnswerSenderThread(ask,
                adress,
                port,
                listenner,
                factory);
        return executor.submit(sender);
    }
}
