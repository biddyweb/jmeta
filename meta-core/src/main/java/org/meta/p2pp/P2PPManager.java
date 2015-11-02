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
package org.meta.p2pp;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.Executors;
import org.meta.api.configuration.P2PPConfiguration;
import org.meta.api.model.ModelStorage;
import org.meta.p2pp.client.P2PPClient;
import org.meta.p2pp.exceptions.P2PPException;
import org.meta.p2pp.server.P2PPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The meta peer to peer protocol manager.
 *
 * It handles socket creation and client sockets pooling.
 *
 * @author dyslesiq
 */
public class P2PPManager {

    /**
     * The maximum number of threads the P2PP stack can use. This should be configurable in the conf class.
     */
    private static final int NB_THREADS = 1;

    private static final Logger logger = LoggerFactory.getLogger(P2PPManager.class);

    private final P2PPConfiguration configuration;

    private final ModelStorage modelStorage;

    private AsynchronousChannelGroup channelGroup;

    private AsynchronousChannelProvider channelProvider;

    private final P2PPServer server;

    private final P2PPClient client;

    /**
     * Creates the Peer-to-Peer protocol manager with given configuration.
     *
     * @param conf the configuration
     * @param storage the model storage
     * @throws IOException if failed to create provider or channel group
     */
    public P2PPManager(final P2PPConfiguration conf, final ModelStorage storage)
            throws IOException, P2PPException {
        this.configuration = conf;
        this.modelStorage = storage;
        this.channelGroup = AsynchronousChannelGroup.withFixedThreadPool(NB_THREADS,
                Executors.defaultThreadFactory());
        channelProvider = AsynchronousChannelProvider.provider();
        this.server = new P2PPServer(this, this.configuration);
        this.client = new P2PPClient(this, this.configuration);
    }

    /**
     *
     * @return the channel group for the p2pp stack
     */
    public AsynchronousChannelGroup getChannelGroup() {
        return channelGroup;
    }

    /**
     *
     * @return the channel provider for the p2pp stack
     */
    public AsynchronousChannelProvider getChannelProvider() {
        return channelProvider;
    }

    /**
     * Starts the server.
     *
     * @throws P2PPException if the server failed to start
     */
    public void startServer() throws P2PPException {
        try {
            this.server.run();
        } catch (IOException ex) {
            throw new P2PPException("Failed to start the server", ex);
        }
    }

    /**
     *
     * @return the p2pp client
     */
    public P2PPClient getClient() {
        return client;
    }

    /**
     *
     * @return the p2pp server
     */
    public P2PPServer getServer() {
        return this.server;
    }

    /**
     *
     * @return the model storage for use by the client and the server.
     */
    public final ModelStorage getModelStorage() {
        return this.modelStorage;
    }

}
