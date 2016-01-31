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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.meta.api.common.MetaPeer;
import org.meta.api.configuration.P2PPConfiguration;
import org.meta.api.model.ModelFactory;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPManager;
import org.meta.p2pp.client.P2PPClientEventHandler.ClientActionContext;
import org.meta.p2pp.exceptions.P2PPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client for the peer-to-peer protocol.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class P2PPClient {

    private final Logger logger = LoggerFactory.getLogger(P2PPClient.class);

    private static final int MAX_REQUEST_HANDLERS_THREADS = 10;

    private final P2PPManager manager;

    private final P2PPConfiguration config;

    private final P2PPClientReadHandler readHandler;

    private final P2PPClientWriteHandler writeHandler;

    private final P2PPClientConnectHandler connectHandler;

    private final AsynchronousChannelGroup channelGroup;

    private final RequestCompletionExecutor operationExecutor;

    /**
     * Opened connections to server peers.
     */
    private final Map<MetaPeer, ClientSocketContext> connections;

    /**
     * Creates the P2PP client with the given manager and configuration.
     *
     * @param p2ppManager the manager
     * @param conf the configuration
     * @throws org.meta.p2pp.exceptions.P2PPException if the channel group creation failed
     */
    public P2PPClient(final P2PPManager p2ppManager, final P2PPConfiguration conf) throws P2PPException {
        this.manager = p2ppManager;
        this.config = conf;
        try {
            //Unbounded cached pool. This will use more and more threads if needed, depending on the load
            //Created threads will be shut down after 60s.
            ExecutorService ioExecutor = new ThreadPoolExecutor(1, this.config.getClientThreads(), 120L,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            logger.debug("Starting P2PP client with: " + this.config.getClientThreads()
                    + " Threads for I/O events");
            this.channelGroup = AsynchronousChannelGroup.withThreadPool(ioExecutor);
        } catch (IOException ex) {
            throw new P2PPException("Failed to create P2PP client channel group", ex);
        }
        //Executor service handling the execution of the requests's completion handlers
        this.operationExecutor = new RequestCompletionExecutor();
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
        for (ClientSocketContext c : this.connections.values()) {
            c.close();
        }
        if (!this.channelGroup.isShutdown()) {
            this.channelGroup.shutdown();
        }
        if (!this.channelGroup.isTerminated()) {
            try {
                this.channelGroup.shutdownNow();
            } catch (final IOException ex) {
                logger.error("Exception while shutting down P2PP Client channel group.", ex);
            }
        }
        this.operationExecutor.close();
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
     */
    public void submitRequest(final MetaPeer peer, final P2PPRequest req) {
        ClientSocketContext c;

        synchronized (this.connections) {
            c = this.connections.get(peer);

            if (c == null) {
                logger.info("New client request manager for server peer: " + peer);
                c = new ClientSocketContext(peer);
                this.connections.put(peer, c);
            }
        }
        dispatchActions(c.addRequest(req));
    }

    /**
     * Executes the actions of the given ClientActionContext.
     *
     * @param actionContext
     */
    private void dispatchActions(ClientActionContext actionContext) {
        try {
            ClientActionContext tmp;
            while (actionContext != null) {
                logger.info("dispatchIoActions: " + actionContext.getAction());
                tmp = actionContext.next();
                //Cut the next action if any
                actionContext.next(null);
                switch (actionContext.getAction()) {
                    case CONNECT:
                        this.connect(actionContext);
                        break;
                    case READ:
                        this.read(actionContext);
                        break;
                    case WRITE:
                        this.write(actionContext);
                        break;
                    case ERROR:
                        this.handleError(null, actionContext);
                        break;
                    case NONE:
                        logger.info("dispatchActions: nothing to do");
                        break;
                    case COMPLETE_REQUEST:
                        this.operationExecutor.submitRequest(actionContext.getAttachment());
                        break;
                    default:
                        throw new AssertionError(actionContext.getAction().name());
                }
                actionContext = tmp;
            }
        } catch (IOException | IllegalStateException ex) {
            this.handleError(ex, actionContext);
        }
    }

    /**
     *
     * @param ioContext
     * @throws java.io.IOException if failed to create the client socket
     */
    private void connect(final ClientActionContext ioContext) throws IOException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(
                this.channelGroup);
        ioContext.getEventHandler().connecting(socketChannel);
        socketChannel.connect(ioContext.getEventHandler().getServerPeer().getSocketAddr(), ioContext,
                this.connectHandler);
    }

    /**
     *
     * @param context the context that needs to read from server peer
     */
    private void read(final ClientActionContext context) {
        logger.debug("CLIENT READING");
        context.getEventHandler().getSocket().read(context.getAttachment(), P2PPConstants.READ_TIMEOUT,
                TimeUnit.SECONDS, context, readHandler);
    }

    /**
     *
     * @param context the context that needs to write to server peer
     */
    private void write(final ClientActionContext context) {
        logger.debug("CLIENT WRITING");
        context.getEventHandler().getSocket().write(context.getAttachment(), P2PPConstants.WRITE_TIMEOUT,
                TimeUnit.SECONDS, context, writeHandler);
    }

    /**
     * Removes the connection context to the given peer from the connection pool.
     *
     * All pending requests are set to failed state.
     *
     * @param peer the server peer
     */
    public void closeContext(final MetaPeer peer) {
        ClientSocketContext c;

        synchronized (this.connections) {
            logger.info("Closing client manager for peer : " + peer);
            c = this.connections.remove(peer);
        }
        if (c != null) {
            c.close();
        }
    }

    /**
     * On read or write failure, dispose of the connection context.
     */
    private void handleError(final Throwable t, final ClientActionContext ioContext) {
        //Try to reconnect on server disconnection or not ?
        //Needs discussion.
        //Maybe a dead peer is a dead peer and should not be contacted after a certain timeout (30minutes?)
        logger.warn("Socket error for server peer: " + ioContext.getEventHandler().getServerPeer());
        if (t != null) {
            logger.warn("Error in P2PP Client:", t);
        }
        this.closeContext(ioContext.getEventHandler().getServerPeer());
    }

    /**
     * <p>getModelFactory</p>
     *
     * @return the model factory for use by the client response handlers
     */
    public final ModelFactory getModelFactory() {
        return this.manager.getModelStorage().getFactory();

    }

    /**
     * Handler for client connections.
     */
    private class P2PPClientConnectHandler implements CompletionHandler<Void, ClientActionContext> {

        @Override
        public void completed(final Void v, final ClientActionContext ioContext) {
            dispatchActions(ioContext.getEventHandler().connected(ioContext));
        }

        @Override
        public void failed(final Throwable thrwbl, final ClientActionContext context) {
            P2PPClient.this.handleError(thrwbl, context);
        }
    }

    /**
     * Handler for client requests read operations.
     */
    private class P2PPClientReadHandler implements CompletionHandler<Integer, ClientActionContext> {

        @Override
        public void completed(final Integer bytes, final ClientActionContext ioContext) {
            logger.debug("Read bytes:" + bytes);
            if (bytes <= 0) {
                P2PPClient.this.handleError(null, ioContext);
            } else if (bytes >= 0) {
                dispatchActions(ioContext.getEventHandler().dataReceived(ioContext));
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final ClientActionContext ioContext) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Read interrupted by timeout.");
            } else {
                logger.warn("Exception catched by read completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPClient.this.handleError(thrwbl, ioContext);
        }
    }

    /**
     * Handler for client requests write operations.
     */
    private class P2PPClientWriteHandler implements CompletionHandler<Integer, ClientActionContext> {

        @Override
        public void completed(final Integer bytes, final ClientActionContext ioContext) {
            logger.debug("Wrote bytes:" + bytes);
            if (bytes <= 0) {
                P2PPClient.this.handleError(null, ioContext);
            } else if (bytes >= 0) {
                dispatchActions(ioContext.getEventHandler().dataSent(ioContext));
            }
        }

        @Override
        public void failed(final Throwable thrwbl, final ClientActionContext ioContext) {
            if (thrwbl instanceof InterruptedByTimeoutException) {
                logger.warn("Write interrupted by timeout.");
            } else {
                logger.warn("Exception catched by write completion handler: {}", thrwbl.getMessage(), thrwbl);
            }
            P2PPClient.this.handleError(thrwbl, ioContext);
        }
    }

    /**
     * Simple wrapper around a P2PPRequest making Runnable the finish() call.
     */
    private class RunnableRequest implements Runnable {

        private final P2PPRequest request;

        RunnableRequest(final P2PPRequest req) {
            this.request = req;
        }

        @Override
        public void run() {
            this.request.finish();
        }
    }

    /**
     * Simple wrapper around an executor service to allows AsyncOperation listeners to be notified outside the
     * I/O executors threads.
     */
    class RequestCompletionExecutor {

        private final ExecutorService executor;

        RequestCompletionExecutor() {
            executor = new ThreadPoolExecutor(2, MAX_REQUEST_HANDLERS_THREADS, 120L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());
        }

        /**
         *
         * @param req
         */
        void submitRequest(final P2PPRequest req) {
            this.executor.submit(new RunnableRequest(req));
        }

        void close() {
            if (!this.executor.isShutdown()) {
                this.executor.shutdown();
            }
            if (!this.executor.isTerminated()) {
                this.executor.shutdownNow();
            }
        }

    }

}
