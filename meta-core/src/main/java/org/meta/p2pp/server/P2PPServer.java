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
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Collection;
import java.util.EnumMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.configuration.P2PPConfiguration;
import org.meta.api.model.ModelStorage;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.P2PPCommand;
import org.meta.p2pp.P2PPManager;
import org.meta.p2pp.exceptions.P2PPException;
import org.meta.p2pp.server.P2PPServerEventHandler.ServerActionContext;
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
 * @version $Id: $
 */
public class P2PPServer implements CompletionHandler<AsynchronousSocketChannel, P2PPServer> {

    private static final Logger logger = LoggerFactory.getLogger(P2PPServer.class);

    private final P2PPManager manager;

    private final P2PPConfiguration config;

    private AsynchronousChannelGroup channelGroup;

    private AsynchronousServerSocketChannel serverSocket;

    private final P2PPServerReadHandler readHandler;

    private final P2PPServerWriteHandler writeHandler;

    private final EnumMap<P2PPCommand, CommandHandlerAccessor<?>> commandHandlers;

    /**
     * Creates the server with the given manager and configuration.
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
                return new P2PPKeepAliveHandler(P2PPServer.this);
            }
        });
        this.commandHandlers.put(P2PPCommand.SEARCH, new CommandHandlerAccessor<P2PPSearchHandler>() {
            @Override
            public P2PPSearchHandler getHandler() {
                return new P2PPSearchHandler(P2PPServer.this);
            }
        });
        this.commandHandlers.put(P2PPCommand.SEARCH_META,
                new CommandHandlerAccessor<P2PPSearchMetaHandler>() {
            @Override
            public P2PPSearchMetaHandler getHandler() {
                return new P2PPSearchMetaHandler(P2PPServer.this);
            }
        });
        this.commandHandlers.put(P2PPCommand.SEARCH_GET, new CommandHandlerAccessor<P2PPSearchGetHandler>() {
            @Override
            public P2PPSearchGetHandler getHandler() {
                return new P2PPSearchGetHandler(P2PPServer.this);
            }
        });
        this.commandHandlers.put(P2PPCommand.GET, new CommandHandlerAccessor<P2PPGetHandler>() {
            @Override
            public P2PPGetHandler getHandler() {
                return new P2PPGetHandler(P2PPServer.this);
            }
        });
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
     * Bind the server socket(s) to configured local interfaces/addresses.
     *
     * @throws P2PPException
     */
    private void bindServerSocket() throws P2PPException, UnknownHostException {
        NetworkConfiguration nwConfig = this.config.getNetworkConfig();
        Collection<InetAddress> configAddresses = NetworkUtils.getConfigAddresses(nwConfig);
        //For now only one address is supported! TODO support multi-binding of the Server...
        try {
            InetAddress addr;
            if (configAddresses.iterator().hasNext()) {
                addr = configAddresses.iterator().next();
            } else {
                //TODO proper binding
                //Ugly fix when there is no addresses configured. Bind to everything
                addr = InetAddress.getByName("0.0.0.0");
            }
            InetSocketAddress inetAddr = new InetSocketAddress(addr, nwConfig.getPort());
            serverSocket.bind(inetAddr);
            logger.info("P2PP Server started on port " + nwConfig.getPort());
        } catch (IOException ex) {
            throw new P2PPException("Failed to bind server socket.", ex);
        }
    }

    /**
     * Starts the server.
     *
     * @throws org.meta.p2pp.exceptions.P2PPException if the server failed to start
     */
    public void run() throws P2PPException {
        try {
            logger.info("Starting server with: " + this.config.getServerThreads() + " Threads");
            this.channelGroup = AsynchronousChannelGroup.withFixedThreadPool(this.config.getServerThreads(),
                    Executors.defaultThreadFactory());
            serverSocket = AsynchronousChannelProvider.provider().
                    openAsynchronousServerSocketChannel(this.channelGroup);
            bindServerSocket();
            serverSocket.accept(null, this);
        } catch (IOException ex) {
            throw new P2PPException("Failed to created channel group", ex);
        }
    }

    /**
     * Close the server.
     */
    public void close() {
        try {
            //TODO keep a list of client contexts and close them properly here...
            //Try graceful shutdown here ?
            //i.e wait for pending request to finish ?
            this.serverSocket.close();
            this.channelGroup.shutdownNow();
        } catch (final IOException ex) {
            //Nothing to do here, we may abort ongoing work thus catch an exception
            logger.warn("Got exception while closing server socket.");
        }
    }

    /**
     * Executes actions.
     *
     * @param actionContext
     */
    private void dispatchActions(ServerActionContext actionContext) {
        ServerActionContext tmp;
        while (actionContext != null) {
            tmp = actionContext.next();
            actionContext.next(null);
            switch (actionContext.getAction()) {
                case READ:
                    this.read(actionContext);
                    break;
                case WRITE:
                    this.write(actionContext);
                    break;
                case DISPATCH:
                    this.dispatch(actionContext);
                    break;
                case ERROR:
                    this.handleSocketError(actionContext);
                    break;
                case NONE:
                    logger.info("dispatchActions: nothing to do");
                    break;
                default:
                    throw new AssertionError(actionContext.getAction().name());
            }
            actionContext = tmp;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Success completion handler for accept().
     */
    @Override
    public void completed(final AsynchronousSocketChannel socket, final P2PPServer a) {
        logger.debug("ACCEPT Complete, socket = {}", socket);
        serverSocket.accept(null, this);
        ServerSocketContext socketContext = new ServerSocketContext();
        this.dispatchActions(socketContext.accepted(socket));
    }

    /**
     * {@inheritDoc}
     *
     * Failure completion handler for accept().
     */
    @Override
    public void failed(final Throwable thrwbl, final P2PPServer a) {
        if (thrwbl instanceof AsynchronousCloseException) {
            //The server is shutting down, it's alright
            logger.warn("Closed server channel group resulted in failed accept.");
        } else {
            logger.error("An error occured while accepting client connections.", thrwbl);
        }
    }

    /**
     * If a socket error occurred on the given context.
     *
     * @param context the client context
     */
    private void handleSocketError(final ServerActionContext context) {
        logger.error("Socket error: " + context.getHandler().getSocket());
        context.getHandler().close();
    }

    /**
     * Starts an asynchronous read operation with the given action context content.
     *
     * @param context the action context
     */
    private void read(final ServerActionContext context) {
        context.getHandler().getSocket().read(context.getAttachment(), P2PPConstants.READ_TIMEOUT,
                TimeUnit.SECONDS, context, readHandler);
    }

    /**
     * Starts an asynchronous write operation with the given action context content.
     *
     * @param context the context
     */
    private void write(final ServerActionContext context) {
        context.getHandler().getSocket().write(context.getAttachment(), P2PPConstants.WRITE_TIMEOUT,
                TimeUnit.SECONDS, context, writeHandler);
    }

    /**
     * Actions in this method are synchronous.
     *
     * dispatchActions() will be called directly after the request has been handled.
     *
     * @param context
     */
    private void dispatch(final ServerActionContext context) {
        P2PPServerRequestContext req = context.getAttachment();

        P2PPCommandHandler handler = this.getCommandHandler(req.getId());
        if (handler == null) {
            this.handleSocketError(context);
            return;
        }
        handler.handle(req);
        this.dispatchActions(context.getHandler().handlerComplete(context));
    }

    /**
     * Handler for client requests read operations.
     */
    class P2PPServerReadHandler implements CompletionHandler<Integer, ServerActionContext> {

        @Override
        public void completed(final Integer bytes, final ServerActionContext context) {
            if (bytes == -1) {
                P2PPServer.this.handleSocketError(context);
            } else {
                P2PPServer.this.dispatchActions(context.getHandler().dataReceived(context));
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final ServerActionContext context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Read completion handler: interrupted by timeout.");
            } else if (thrwbl instanceof AsynchronousCloseException) {
                //Nothing to do, we are shuting down
                logger.warn("Read interrupted: socket close requested.");
            } else {
                //TODO handle exceptions based on their types.
                logger.warn("Exception catched by read completion handler.", thrwbl);
            }
            P2PPServer.this.handleSocketError(context);
        }
    }

    /**
     * Handler for client requests responses write operations.
     */
    class P2PPServerWriteHandler implements CompletionHandler<Integer, ServerActionContext> {

        @Override
        public void completed(final Integer bytes, final ServerActionContext context) {
            if (bytes == -1) {
                P2PPServer.this.handleSocketError(context);
            } else {
                logger.info("P2PPServerWriteHandler: wrote " + bytes + " bytes");
                P2PPServer.this.dispatchActions(context.getHandler().dataSent(context));
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final ServerActionContext context) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Write completion handler: interrupted by timeout.");
            } else if (thrwbl instanceof AsynchronousCloseException) {
                //Nothing to do, we are shuting down
                logger.warn("Write interrupted: socket close requested.");
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

    /**
     * <p>
     * getStorage</p>
     *
     * @return the model object storage for use by handlers
     */
    public ModelStorage getStorage() {
        return this.manager.getModelStorage();
    }

}
