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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.meta.api.common.MetaPeer;
import org.meta.api.configuration.P2PPConfiguration;
import org.meta.api.model.ModelFactory;
import org.meta.p2pp.P2PPConstants;
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

    private final P2PPClientConnectHandler connectHandler;

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
        this.config = conf;
        try {
            logger.debug("Starting client channelGroup with: " + this.config.getClientThreads() + " Threads");
            this.channelGroup = AsynchronousChannelGroup.withFixedThreadPool(conf.getClientThreads(),
                    Executors.defaultThreadFactory());
        } catch (IOException ex) {
            throw new P2PPException("Failed to create P2PP client channel group", ex);
        }
        this.readHandler = new P2PPClientReadHandler();
        this.writeHandler = new P2PPClientWriteHandler();
        this.connectHandler = new P2PPClientConnectHandler();
        this.connections = new HashMap<>();
    }

    /**
     * Closes the P2PP client.
     *
     * All connections to server peers are closed and pending I/O operations are aborted.
     *
     * All pending requests are set to failed state.
     */
    public void close() {
        this.channelGroup.shutdown();
        for (P2PPClientRequestManager c : this.connections.values()) {
            c.close();
        }
    }

    /**
     *
     * @param context the context to connecting
     * @throws java.io.IOException if failed to create the client socket
     */
    public void connect(final P2PPClientRequestManager context) throws IOException {
        logger.debug("Socket needs to connect!");
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(
                this.channelGroup);
        context.connecting(socketChannel);
        socketChannel.connect(context.getServerPeer().getSocketAddr(), context, this.connectHandler);
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
        P2PPClientRequestManager c;

        synchronized (this.connections) {
            c = this.connections.get(peer);

            if (c == null) {
                logger.info("New client request manager for server peer: " + peer);
                c = new P2PPClientRequestManager(peer, this);
                this.connections.put(peer, c);
            }
        }
        try {
            synchronized (c) {
                c.addRequest(req);
                if (!c.isConnected() && !c.isConnecting()) {
                    this.connect(c);
                } else if (c.isConnected() && !c.isConnecting()) {
                    write(c);
                }
            }
        } catch (IOException ex) {
            logger.warn("Failed to get socket channel from group.");
            req.setFailed(ex);
        }
    }

    /**
     *
     * @param context the context that needs to read from server peer
     */
    public void read(final P2PPClientRequestManager context) {
        ByteBuffer buf = context.getNextReadBuffer();

        if (buf != null) {
            logger.debug("CLIENT READING");
            context.getSocket().read(buf, P2PPConstants.READ_TIMEOUT, TimeUnit.SECONDS,
                    context, readHandler);
        }
    }

    /**
     *
     * @param context the context that needs to write to server peer
     */
    public void write(final P2PPClientRequestManager context) {
        ByteBuffer buf = context.getNextWriteBuffer();

        if (buf != null) {
            logger.debug("CLIENT WRITING");
            context.getSocket().write(buf, P2PPConstants.WRITE_TIMEOUT, TimeUnit.SECONDS,
                    context, writeHandler);
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
        P2PPClientRequestManager c;

        synchronized (this.connections) {
            logger.info("Closing client manager for peer : " + peer);
            c = this.connections.remove(peer);
        }
        if (c != null) {
            synchronized (c) {
                c.close();
            }
        }
    }

    /**
     * On read or write failure, dispose of the connection context.
     */
    private void handleSocketError(final Throwable t, final P2PPClientRequestManager clientManager) {
        //Try to reconnect on server disconnection or not ?
        //Needs discussion.
        //Maybe a dead peer is a dead peer and should not be contacted after a certain timeout (30minutes?)
        logger.warn("Socket error for server peer: " + clientManager.getServerPeer());
        this.closeConnection(clientManager.getServerPeer());
    }

    /**
     *
     * @return the model factory for use by the client response handlers
     */
    public final ModelFactory getModelFactory() {
        return this.manager.getModelStorage().getFactory();
    }

    /**
     * Handler for client connections.
     */
    private class P2PPClientConnectHandler implements CompletionHandler<Void, P2PPClientRequestManager> {

        @Override
        public void completed(final Void v, final P2PPClientRequestManager c) {
            logger.info("Connected to :" + c.getServerPeer());
            synchronized (c) {
                c.connected();
                //Launch read/write once the socket is connected
                write(c);
                //read(c);
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final P2PPClientRequestManager c) {
            P2PPClient.this.handleSocketError(thrwbl, c);
        }
    }

    /**
     * Handler for client requests read operations.
     */
    private class P2PPClientReadHandler implements CompletionHandler<Integer, P2PPClientRequestManager> {

        @Override
        public void completed(final Integer bytes, final P2PPClientRequestManager context) {
            logger.info("Read bytes:" + bytes);
            if (bytes == 0) {
                logger.error("ZERO BYTES READ!!!!!!!!");
                return;
            }
            if (bytes == -1) {
                P2PPClient.this.handleSocketError(null, context);
            } else if (bytes >= 0) {
                synchronized (context) {
                    context.dataReceived();
                    read(context);
                }
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final P2PPClientRequestManager context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Read interrupted by timeout.");
            } else {
                logger.warn("Exception catched by read completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPClient.this.handleSocketError(thrwbl, context);
        }
    }

    /**
     * Handler for client requests write operations.
     */
    private class P2PPClientWriteHandler implements CompletionHandler<Integer, P2PPClientRequestManager> {

        @Override
        public void completed(final Integer bytes, final P2PPClientRequestManager context) {
            logger.debug("Wrote bytes:" + bytes);
            if (bytes == -1) {
                P2PPClient.this.handleSocketError(null, context);
            } else if (bytes >= 0) {
                synchronized (context) {
                    context.dataSent();
                    write(context);
                    read(context);
                }
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final P2PPClientRequestManager context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Write interrupted by timeout.");
            } else {
                logger.warn("Exception catched by write completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPClient.this.handleSocketError(thrwbl, context);
        }
    }
}
