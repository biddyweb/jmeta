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
package org.meta.p2pp.client;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import org.meta.api.common.MetaPeer;
import org.meta.api.configuration.P2PPConfiguration;
import org.meta.api.model.ModelFactory;
import org.meta.p2pp.P2PPManager;
import org.meta.p2pp.exceptions.P2PPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client for the peer-to-peer protocol.
 *
 * @author dyslesiq
 */
public class P2PPClient {

    private static final Logger logger = LoggerFactory.getLogger(P2PPClient.class);

    private final P2PPManager manager;

    private final P2PPConfiguration config;

    private final P2PPClientReadHandler readHandler;

    private final P2PPClientWriteHandler writeHandler;

    private final AsynchronousChannelGroup channelGroup;

    /**
     * Opened connections to server peers.
     */
    private final Map<MetaPeer, P2PPClientRequestManager> connections;

    /**
     * Creates the P2PP client with the given manager and configuration.
     *
     * @param p2ppManager the manager
     * @param conf the configuration
     * @throws P2PPException if the channel group creation failed
     */
    public P2PPClient(final P2PPManager p2ppManager, final P2PPConfiguration conf) throws P2PPException {
        this.manager = p2ppManager;
        //Unused for now, but could be used for max connections/s, max global connections, etc...
        this.config = conf;
        this.readHandler = new P2PPClientReadHandler();
        this.writeHandler = new P2PPClientWriteHandler();
        this.connections = new ConcurrentHashMap<>();
        try {
            logger.info("Starting client with: " + this.config.getClientThreads() + " Threads");
            this.channelGroup = AsynchronousChannelGroup.withFixedThreadPool(this.config.getClientThreads(),
                    Executors.defaultThreadFactory());
        } catch (IOException ex) {
            throw new P2PPException("Failed to create P2PP client channel group", ex);
        }
    }

    /**
     * Submit a request to be sent to the given server peer.
     *
     * If there is an existing connection to the peer, it is used and the request is either send directly or
     * put in the wait queue.
     *
     * Otherwise the connection is attempted before sending the request.
     *
     * The outcome of the operation is accessible through the request async operation
     * {@code P2PPRequest.getOperation()}.
     *
     * @param peer the peer to send to request to
     * @param req the request to send to the server peer
     *
     */
    public void submitRequest(final MetaPeer peer, final P2PPRequest req) {
        try {
            P2PPClientRequestManager c;

            synchronized (this.connections) {
                c = this.connections.get(peer);

                if (c == null) {
                    logger.info("New client request manager for server peer: " + peer);
                    c = new P2PPClientRequestManager(peer, this);
                    this.connections.put(peer, c);
                }
            }
            synchronized (c) {
                if (!c.isConnected() && !c.isConnecting()) {
                    logger.debug("Socket needs to connect!");
                    AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(
                            this.channelGroup);
                    c.connect(socketChannel);
                }
                c.addRequest(req);
            }
        } catch (IOException ex) {
            logger.warn("Failed to get socket channel from group.");
            req.setFailed(ex);
        }
    }

    /**
     * Removes the connection to the given peer from the connection pool.
     *
     * All pending requests are set to failed state.
     *
     * @param peer the server peer
     */
    public void closeConnection(final MetaPeer peer) {
        logger.info("Closing client manager for peer : " + peer);
        P2PPClientRequestManager c = this.connections.remove(peer);

        if (c != null) {
            synchronized (c) {
                c.close();
            }
        }
    }

    /**
     * On read or write failure, dispose of the connection context.
     */
    private void handleSocketError(final P2PPClientRequestManager clientManager) {
        //Try to reconnect on server disconnection or not ?
        //Needs discussion.
        //Maybe a dead peer is a dead peer and should not be contacted after a certain timeout (30minutes?)
        logger.warn("Socket error: " + clientManager.getServerPeer());
        this.closeConnection(clientManager.getServerPeer());
    }

    /**
     * @return the completion handler for read operations
     */
    P2PPClientReadHandler getReadHandler() {
        return this.readHandler;
    }

    /**
     *
     * @return the completion handler for write operations
     */
    P2PPClientWriteHandler getWriteHandler() {
        return this.writeHandler;
    }

    /**
     *
     * @return the model factory for use by the client response handlers
     */
    public final ModelFactory getModelFactory() {
        return this.manager.getModelStorage().getFactory();
    }

    /**
     * Handler for client requests read operations.
     */
    class P2PPClientReadHandler implements CompletionHandler<Integer, P2PPClientRequestManager> {

        @Override
        public void completed(final Integer bytes, final P2PPClientRequestManager context) {
            if (bytes == -1) {
                P2PPClient.this.handleSocketError(context);
            } else if (bytes > 0) {
                synchronized (context) {
                    context.dataReceived();
                }
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final P2PPClientRequestManager context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Read completion handler: interrupted by timeout.");
            } else {
                logger.warn("Exception catched by read completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPClient.this.handleSocketError(context);
        }
    }

    /**
     * Handler for client requests write operations.
     */
    class P2PPClientWriteHandler implements CompletionHandler<Integer, P2PPClientRequestManager> {

        @Override
        public void completed(final Integer bytes, final P2PPClientRequestManager context) {
            if (bytes == -1) {
                P2PPClient.this.handleSocketError(context);
            } else if (bytes > 0) {
                synchronized (context) {
                    context.dataSent();
                }
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final P2PPClientRequestManager context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Write completion handler: interrupted by timeout.");
            } else {
                logger.warn("Exception catched by write completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPClient.this.handleSocketError(context);
        }
    }
}
