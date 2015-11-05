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
 * Doesn't do much for now but could be used for common part between server and client. Could be used to
 * manage the maximum connections/up and down bandwith, etc...
 *
 * @author dyslesiq
 */
public class P2PPManager {

    private static final Logger logger = LoggerFactory.getLogger(P2PPManager.class);

    private final P2PPConfiguration configuration;

    private final ModelStorage modelStorage;

    private final P2PPServer server;

    private final P2PPClient client;

    /**
     * Creates the Peer-to-Peer protocol manager with given configuration.
     *
     * @param conf the configuration
     * @param storage the model storage
     * @throws P2PPException if the client creation failed
     */
    public P2PPManager(final P2PPConfiguration conf, final ModelStorage storage) throws P2PPException {
        this.configuration = conf;
        this.modelStorage = storage;
        this.server = new P2PPServer(this, this.configuration);
        this.client = new P2PPClient(this, this.configuration);
    }

    /**
     * Starts the server.
     *
     * @throws P2PPException if the server failed to start
     */
    public void startServer() throws P2PPException {
        this.server.run();
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
