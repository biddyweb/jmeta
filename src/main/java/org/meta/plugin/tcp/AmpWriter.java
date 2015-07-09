/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Thomas LAVOCAT/Nicolas Michon/Pablo Joubert
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.plugin.tcp;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.meta.common.MetHash;
import org.meta.configuration.AMPConfiguration;
import org.meta.configuration.MetaConfiguration;
import org.meta.model.ModelFactory;
import org.meta.plugin.tcp.amp.AMPAskBuilder;

/**
 *  used to contact peers
 *
 * @author faquin
 *
 */
public class AmpWriter {

    private int lastAsk = 0;
    private ExecutorService executor = null;
    private ModelFactory factory = null;

    /**
     * Unused for now.
     */
    private final AMPConfiguration configuration;

    public AmpWriter(AMPConfiguration config, ModelFactory factory) {
        this.configuration = config;
        this.factory = factory;
        this.executor = Executors.newFixedThreadPool(
                MetaConfiguration.getAmpConfiguration().getSenderThPoolSize());
    }

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
    public Future<?> askTo(InetAddress adress,
            String plugin,
            String command,
            MetHash hash,
            TCPResponseCallbackInteface listenner,
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
