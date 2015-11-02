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
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.util.ArrayQueue;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ServerRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing the state of an open connection made to our server.
 *
 * It manages incoming requests and outgoing responses as queues.
 *
 * Stacking a request is possible only once the previous one has been fully received.
 *
 * @author dyslesiq
 */
public class P2PPServerClientContext {

    private final Logger logger = LoggerFactory.getLogger(P2PPServerClientContext.class);

    /**
     * The maximum ongoing requests a client can make to a server.
     */
    private final P2PPServer server;

    /**
     * The opened socket.
     */
    private final AsynchronousSocketChannel clientSocket;

    /**
     * Request waiting for data retrieval.
     */
    private P2PPServerRequestContext recvRequest;

    /**
     * Requests waiting for handler completion.
     */
    private final Queue<P2PPServerRequestContext> disptachQueue;

    /**
     * Treated request, waiting to be sent to client.
     */
    private P2PPServerRequestContext sendRequest;

    private final CompletionHandler<Integer, P2PPServerClientContext> readHandler;

    private final CompletionHandler<Integer, P2PPServerClientContext> writeHandler;

    /**
     * New client context for the given socket.
     *
     * @param p2ppServer the Peer-to-Peer protocol server instance
     * @param clientChannel the socket
     * @param rHandler the completion handler for read operations
     * @param wHandler the completion handler for write operations
     */
    public P2PPServerClientContext(final P2PPServer p2ppServer, final AsynchronousSocketChannel clientChannel,
            final CompletionHandler<Integer, P2PPServerClientContext> rHandler,
            final CompletionHandler<Integer, P2PPServerClientContext> wHandler) {
        this.server = p2ppServer;
        this.clientSocket = clientChannel;
        this.readHandler = rHandler;
        this.writeHandler = wHandler;
        this.disptachQueue = new ArrayQueue<>(P2PPConstants.CONCURRENT_CLIENT_REQUESTS);
    }

    /**
     * Stops the client socket and all running requests for immediate shutdown.
     *
     * @throws IOException if an I/O error occurred while closing
     */
    public void close() throws IOException {
        if (this.recvRequest != null) {
            this.recvRequest.close();
        }
        if (this.sendRequest != null) {
            this.sendRequest.close();
        }
        this.disptachQueue.clear();
        this.clientSocket.close();
    }

    /**
     * @return the socket channel of this context
     */
    public AsynchronousSocketChannel getClientSocket() {
        return clientSocket;
    }

    /**
     * Check that the given token is valid.
     *
     * @param token the token to verify
     * @return true if valid, false otherwise
     */
    public boolean checkToken(final short token) {
        //TODO : tokens should increment for each subsequent requests.
        //Such as token(req t - 1) < token(req t) < token(req t + 1)
        return true;
    }

    /**
     *
     * @return true if a new request can be handled, false otherwise
     */
    public boolean canStackRequest() {
        if (this.recvRequest != null) {
            return false;
        }
        return this.disptachQueue.size() < P2PPConstants.CONCURRENT_CLIENT_REQUESTS;
    }

    /**
     * Prepares a new request to be received.
     */
    public void newRequest() {
        logger.debug("New request");
        this.recvRequest = new P2PPServerRequestContext();
        this.receive();
    }

    /**
     *
     * Called by the async read handler when data as been received.
     */
    public void requestDataReceived() {
        ServerRequestStatus status = this.recvRequest.bufferUpdated();

        if (status == ServerRequestStatus.HEADER_PENDING || status == ServerRequestStatus.DATA_PENDING) {
            this.receive();
        } else if (status == ServerRequestStatus.HEADER_RECEIVED) {
            logger.debug("Request header received!");
            if (!this.recvRequest.parseRequestHeader() || !this.checkToken(this.recvRequest.getToken())) {
                logger.debug("Invalid request header received!");
                this.handleInvalidRequest(this.recvRequest);
            } else {
                logger.debug("Valid request header received!");
                //If the header is valid, we can read the remaining content of the request
                //But first check if the request has a payload
                if (this.recvRequest.getRequestDataSize() > 0) {
                    this.recvRequest.allocateDataBuffer();
                    this.receive();
                } else {
                    this.dispatchRequest();
                }
            }
        } else if (status == ServerRequestStatus.DATA_RECEIVED) {
            logger.debug("Request data received!");
            this.dispatchRequest();
        }
    }

    /**
     * Called by the command handler to notify that the request has been fully handled.
     *
     * If there is response, it is now ready to be sent.
     *
     * @param request the request that has been handled
     */
    public synchronized void handlerComplete(final P2PPServerRequestContext request) {
        ServerRequestStatus status = request.getStatus();

        if (status == ServerRequestStatus.DISCARDED) {
            this.handleInvalidRequest(request);
        } else if (status == ServerRequestStatus.RESPONSE_READY && request.hasResponseData()) {
            this.trySend();
        } else if (status == ServerRequestStatus.FINISHED) {
            this.trySend();
            if (this.canStackRequest()) {
                this.newRequest();
            }
        }
    }

    /**
     * Called by the async write handler when data has been sent to the client.
     */
    public void responseDataSent() {
        ServerRequestStatus status = this.sendRequest.responseDataSent();

        if (status == ServerRequestStatus.FINISHED) {
            logger.debug("Response data sent, FINISHED");
            this.sendRequest = null;
            this.trySend();
            if (this.canStackRequest()) {
                this.newRequest();
            }
        } else if (status == ServerRequestStatus.RESPONSE_PENDING) {
            logger.debug("Response data sent, PENDING");
            this.send();
        } else {
            logger.debug("Response data sent, WUT ?");
        }
    }

    /**
     * Read client request on the socket.
     */
    public void receive() {
        if (this.recvRequest.getStatus() == ServerRequestStatus.HEADER_PENDING) {
            logger.debug("Receiving request header");
            //read header
            this.clientSocket.read(this.recvRequest.getHeaderBuffer(),
                    P2PPConstants.READ_TIMEOUT, TimeUnit.SECONDS, this, readHandler);
        } else if (this.recvRequest.getStatus() == ServerRequestStatus.DATA_PENDING) {
            //Read payload
            logger.debug("Receiving request payload");
            this.clientSocket.read(this.recvRequest.getDataBuffer(),
                    P2PPConstants.READ_TIMEOUT, TimeUnit.SECONDS, this, readHandler);
        }
    }

    /**
     *
     * Sends the response data on the socket.
     */
    public void send() {
        this.clientSocket.write(this.sendRequest.getResponseBuffer(), P2PPConstants.WRITE_TIMEOUT,
                TimeUnit.SECONDS, this, writeHandler);
    }

    /**
     * Sends the next response, if available.
     */
    private void trySend() {
        if (this.sendRequest != null) {
            logger.debug("TRY SEND, not null, aborting");
            return;
        }
        if (!this.disptachQueue.isEmpty()) {
            ServerRequestStatus status = this.disptachQueue.element().getStatus();

            if (status == ServerRequestStatus.RESPONSE_READY) {
                this.sendRequest = this.disptachQueue.poll();
                if (this.sendRequest.hasResponseData()) {
                    this.sendRequest.setStatus(ServerRequestStatus.RESPONSE_PENDING);
                    this.send();
                }
            } else if (status == ServerRequestStatus.FINISHED) {
                this.disptachQueue.remove();
                this.trySend();
            }
        } else {
            logger.debug("TRY SEND, dispatch queue empty, aborting");
        }
    }

    /**
     *
     * Moves the current request to the dispatch queue and executes the associated command handler.
     */
    private void dispatchRequest() {
        P2PPCommandHandler handler = server.getCommandHandler(this.recvRequest.getId());

        if (handler != null) {
            P2PPServerRequestContext req = this.recvRequest;
            this.recvRequest = null;
            req.dispatch();
            this.disptachQueue.add(req);
            handler.handle(this, req);
            if (canStackRequest()) {
                this.newRequest();
            }
        } else {
            this.handleInvalidRequest(this.recvRequest);
        }
    }

    /**
     * Discards the given request and closes connection if something went wrong (I/O, protocol, ...).
     *
     * @param req the faulty request
     */
    private void handleInvalidRequest(final P2PPServerRequestContext req) {
        req.setStatus(ServerRequestStatus.DISCARDED);
        this.server.closeClient(this);
    }
}
