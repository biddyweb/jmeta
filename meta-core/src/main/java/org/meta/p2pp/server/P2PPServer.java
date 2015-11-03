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
package org.meta.p2pp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Collection;
import java.util.EnumMap;
import java.util.concurrent.Executors;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.configuration.P2PPConfiguration;
import org.meta.p2pp.P2PPConstants.P2PPCommand;
import org.meta.p2pp.P2PPManager;
import org.meta.p2pp.exceptions.P2PPException;
import org.meta.p2pp.server.handlers.P2PPGetHandler;
import org.meta.p2pp.server.handlers.P2PPKeepAliveHandler;
import org.meta.p2pp.server.handlers.P2PPSearchGetHandler;
import org.meta.p2pp.server.handlers.P2PPSearchHandler;
import org.meta.p2pp.server.handlers.P2PPSearchMetaHandler;
import org.meta.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meta peer-to-peer protocol server.
 *
 * @author dyslesiq
 */
public class P2PPServer implements CompletionHandler<AsynchronousSocketChannel, P2PPServer> {

    private static final Logger logger = LoggerFactory.getLogger(P2PPServer.class);

    private final P2PPManager manager;

    private final P2PPConfiguration config;

    private AsynchronousChannelGroup channelGroup;

    private AsynchronousServerSocketChannel server;

    private final P2PPServerReadHandler readHandler;

    private final P2PPServerWriteHandler writeHandler;

    private final EnumMap<P2PPCommand, CommandHandlerAccessor<?>> commandHandlers;

    /**
     * Creates the server with the given manager.
     *
     * @param p2ppManager the manager
     * @param conf the configuration
     */
    public P2PPServer(final P2PPManager p2ppManager, final P2PPConfiguration conf) {
        this.manager = p2ppManager;
        this.config = conf;
        this.readHandler = new P2PPServerReadHandler();
        this.writeHandler = new P2PPServerWriteHandler();
        this.commandHandlers = new EnumMap<>(P2PPCommand.class);
        this.initHandlers();
    }

    private void initHandlers() {
        this.commandHandlers.put(P2PPCommand.KEEP_ALIVE, new CommandHandlerAccessor<P2PPKeepAliveHandler>() {
            @Override
            public P2PPKeepAliveHandler getHandler() {
                return new P2PPKeepAliveHandler(P2PPServer.this.manager.getModelStorage());
            }
        });
        this.commandHandlers.put(P2PPCommand.SEARCH, new CommandHandlerAccessor<P2PPSearchHandler>() {
            @Override
            public P2PPSearchHandler getHandler() {
                return new P2PPSearchHandler(P2PPServer.this.manager.getModelStorage());
            }
        });
        this.commandHandlers.put(P2PPCommand.SEARCH_META,
                new CommandHandlerAccessor<P2PPSearchMetaHandler>() {
                    @Override
                    public P2PPSearchMetaHandler getHandler() {
                        return new P2PPSearchMetaHandler(P2PPServer.this.manager.getModelStorage());
                    }
                });
        this.commandHandlers.put(P2PPCommand.SEARCH_GET, new CommandHandlerAccessor<P2PPSearchGetHandler>() {
            @Override
            public P2PPSearchGetHandler getHandler() {
                return new P2PPSearchGetHandler(P2PPServer.this.manager.getModelStorage());
            }
        });
        this.commandHandlers.put(P2PPCommand.GET, new CommandHandlerAccessor<P2PPGetHandler>() {
            @Override
            public P2PPGetHandler getHandler() {
                return new P2PPGetHandler(P2PPServer.this.manager.getModelStorage());
            }
        });
    }

    private void bindServerSocket() throws P2PPException {
        NetworkConfiguration nwConfig = this.config.getNetworkConfig();
        Collection<InetAddress> configAddresses = NetworkUtils.getConfigAddresses(nwConfig);
        //For now only one address is supported! TODO supper multi-binding of the Server...

        InetAddress addr = configAddresses.iterator().next();
        logger.info("P2PP Server listening on port " + nwConfig.getPort());
        InetSocketAddress inetAddr = new InetSocketAddress(addr, nwConfig.getPort());
        try {
            server.bind(inetAddr);
        } catch (IOException ex) {
            throw new P2PPException("Failed to bind server socket on address: " + inetAddr, ex);
        }
    }

    /**
     * Starts the server.
     *
     * @throws P2PPException if the server failed to start
     */
    public void run() throws P2PPException {
        try {
            logger.info("Starting server with: " + this.config.getServerThreads() + " Threads");
            this.channelGroup = AsynchronousChannelGroup.withFixedThreadPool(this.config.getServerThreads(),
                    Executors.defaultThreadFactory());
            server = AsynchronousChannelProvider.provider().
                    openAsynchronousServerSocketChannel(this.channelGroup);
            bindServerSocket();
            server.accept(this, this);
        } catch (IOException ex) {
            throw new P2PPException("Failed to created channel group", ex);
        }
    }

    /**
     * @param socket the newly accepted socket channel
     * @param a should be == to 'this' here but we are our own completion handler
     */
    @Override
    public void completed(final AsynchronousSocketChannel socket, final P2PPServer a) {
        logger.debug("ACCEPT Completed, socket = " + socket);
        P2PPServerClientContext clientContext = new P2PPServerClientContext(this, socket,
                this.readHandler, this.writeHandler);
        clientContext.newRequest();
        server.accept(this, this);
    }

    @Override
    public void failed(final Throwable thrwbl, final P2PPServer a) {
        logger.error("An error occured while accepting client connections.", thrwbl);
    }

    /**
     * If a socket error occurred on the given context.
     *
     * @param context the client context
     */
    private void handleSocketError(final P2PPServerClientContext context) {
        logger.debug("Socket error: " + context.getClientSocket());
        this.closeClient(context);
    }

    /**
     *
     * @param commandId the command id to check
     * @return true if this command exists, false otherwise
     */
    public boolean hasCommand(final P2PPCommand commandId) {
        return this.commandHandlers.containsKey(commandId);
    }

    /**
     * Get the request handler for the given command.
     *
     * @param commandId the command identifier
     * @return the handler, or null if invalid command
     */
    public P2PPCommandHandler getCommandHandler(final P2PPCommand commandId) {
        return this.commandHandlers.get(commandId).getHandler();
    }

    /**
     * Close the given client socket context.
     *
     * @param clientContext the client context
     */
    public void closeClient(final P2PPServerClientContext clientContext) {
        try {
            logger.debug("Closing client context.");
            clientContext.close();
        } catch (IOException ex) {
            logger.warn("Exception while closing client context.", ex);
        }
    }

    /**
     * Close the server.
     */
    public void close() {
        try {
            //Try graceful shutdown here ?
            //i.e wait for pending request to finish ?
            this.server.close();
        } catch (IOException ex) {
            logger.warn("Got exception while closing server socket.", ex);
        }
    }

    /**
     * Handler for client requests read operations.
     */
    class P2PPServerReadHandler implements CompletionHandler<Integer, P2PPServerClientContext> {

        @Override
        public void completed(final Integer bytes, final P2PPServerClientContext context) {
            logger.debug("Received bytes: " + bytes);
            if (bytes == -1) {
                P2PPServer.this.handleSocketError(context);
            } else if (bytes >= 0) {
                synchronized (context) {
                    context.requestDataReceived();
                }
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final P2PPServerClientContext context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Read completion handler: interrupted by timeout.");
            } else {
                logger.warn("Exception catched by read completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPServer.this.handleSocketError(context);
        }
    }

    /**
     * Handler for client requests responses write operations.
     */
    class P2PPServerWriteHandler implements CompletionHandler<Integer, P2PPServerClientContext> {

        @Override
        public void completed(final Integer bytes, final P2PPServerClientContext context) {
            logger.debug("Wrote bytes: " + bytes);
            if (bytes == -1) {
                P2PPServer.this.handleSocketError(context);
            } else if (bytes >= 0) {
                synchronized (context) {
                    context.responseDataSent();
                }
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final P2PPServerClientContext context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Write completion handler: interrupted by timeout.");
            } else {
                logger.warn("Exception catched by write completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPServer.this.handleSocketError(context);
        }
    }

    /**
     *
     * @param <T> the type of the command handler
     */
    private abstract class CommandHandlerAccessor<T extends P2PPCommandHandler> {

        public abstract T getHandler();

    }

}
