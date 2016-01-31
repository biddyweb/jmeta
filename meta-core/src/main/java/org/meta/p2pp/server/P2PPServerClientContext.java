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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ServerRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing the state of an open connection made to our server.
 *
 * It manages incoming requests and outgoing responses as queues.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class P2PPServerClientContext {

    private final Logger logger = LoggerFactory.getLogger(P2PPServerClientContext.class);

    private final P2PPServer server;

    /**
     * The opened socket.
     */
    private AsynchronousSocketChannel clientSocket;

    /**
     * Requests waiting for handler completion.
     */
    private volatile P2PPServerRequestContext recvRequest;

    /**
     * Request being treated by the command handler.
     */
    //private volatile P2PPServerRequestContext dispatchRequest;
    /**
     * Requests waiting for handler completion.
     */
    private final Deque<P2PPServerRequestContext> sendQueue;

    /**
     * The request being sent to the client.
     */
    private volatile P2PPServerRequestContext sendRequest;

    private final ByteBuffer headerBuffer;

    /**
     * New client context for the given socket.
     *
     * @param p2ppServer the Peer-to-Peer protocol server instance
     * @param clientChannel the socket
     */
    public P2PPServerClientContext(final P2PPServer p2ppServer,
            final AsynchronousSocketChannel clientChannel) {
        this.server = p2ppServer;
        this.clientSocket = clientChannel;
        this.sendQueue = new ConcurrentLinkedDeque<>();
        this.headerBuffer = BufferManager.aquireDirectBuffer(P2PPConstants.REQUEST_HEADER_SIZE);
    }

    /**
     * Stops the client socket and all running requests for immediate shutdown.
     */
    public synchronized void close() {
        if (this.clientSocket == null) {
            return;
        }
        try {
            this.clientSocket.close();
        } catch (IOException ex) {
            logger.warn("IOException while closing client socket: ", ex);
        }
        if (this.recvRequest != null) {
            this.recvRequest.close();
        }
        if (this.sendRequest != null) {
            this.sendRequest.close();
        }
        for (P2PPServerRequestContext req : this.sendQueue) {
            req.close();
        }
        this.sendQueue.clear();
        this.recvRequest = null;
        this.sendRequest = null;
        BufferManager.release(headerBuffer);
        this.clientSocket = null;
    }

    /**
     * <p>Getter for the field <code>clientSocket</code>.</p>
     *
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
    public boolean checkToken(final char token) {
        //TODO : tokens should increment for each subsequent requests.
        //Such as token(req t - 1) < token(req t) < token(req t + 1)
        return true;
    }

    /**
     *
     * Called by the async read handler when data as been received.
     */
    public void requestDataReceived() {
        if (this.recvRequest.getStatus() == ServerRequestStatus.HEADER_PENDING) {
            if (!this.headerBuffer.hasRemaining()) {
                if (!this.recvRequest.parseRequestHeader(headerBuffer)) {
                    handleInvalidRequest(this.recvRequest);
                    return;
                }
                if (this.recvRequest.hasPayload()) {
                    this.recvRequest.allocateDataBuffer();
                    this.recvRequest.setStatus(ServerRequestStatus.HEADER_RECEIVED);
                } else {
                    //If the request doesn't have a payload, no need to read further
                    this.recvRequest.setStatus(ServerRequestStatus.DATA_RECEIVED);
                    if (!this.sendQueue.add(recvRequest)) {
                        logger.error("Failed to add read request into dispatch queue!");
                    }
                    this.recvRequest = null;
                }
            } else {
                logger.error("Data received, HEADER_PENDING But headerBuffer hasRemaining!!");
                handleInvalidRequest(this.recvRequest);
            }
        } else if (this.recvRequest.getStatus() == ServerRequestStatus.DATA_PENDING) {
            if (!this.recvRequest.getDataBuffer().hasRemaining()) {
                this.recvRequest.setStatus(ServerRequestStatus.DATA_RECEIVED);
                if (!this.sendQueue.add(recvRequest)) {
                    logger.error("Failed to add read request into dispatch queue!");
                }
                this.recvRequest = null;
            } else {
                logger.error("Data received, DATA_PENDING But Buffer hasRemaining!!");
                handleInvalidRequest(this.recvRequest);
            }
        } else {
            logger.error("requestDataReceived: unhandled status: " + this.recvRequest.getStatus());
        }
    }

    /**
     * <p>getNextReadBuffer</p>
     *
     * @return the buffer to read
     */
    public ByteBuffer getNextReadBuffer() {
        if (this.recvRequest == null) {
            this.recvRequest = new P2PPServerRequestContext();
            this.recvRequest.setStatus(ServerRequestStatus.HEADER_PENDING);
            headerBuffer.rewind();
            return headerBuffer;
        } else if (this.recvRequest.getStatus() == ServerRequestStatus.HEADER_RECEIVED) {
            this.recvRequest.setStatus(ServerRequestStatus.DATA_PENDING);
            return this.recvRequest.getDataBuffer();
        } else {
            logger.error("UNHANDLED STATUS FOR GET NEXT READ BUFFER: " + this.recvRequest.getStatus());
        }
        return null;
    }

    /**
     * Called by the async write handler when data has been sent to the client.
     */
    public void responseDataSent() {
        logger.debug("responseDataSent");
        if (this.sendRequest == null) {
            logger.error("OMG!!! responseDataSent and sendRequest NULL!!!!");
            return;
        }
        if (this.sendRequest.getStatus() == ServerRequestStatus.RESPONSE_PENDING) {
            if (!this.sendRequest.getResponseBuffer().hasRemaining()) {
                //this.sendRequest.setStatus(ServerRequestStatus.FINISHED);
                this.sendRequest.close();
                this.sendRequest = null;
            } else {
                logger.error("responseDataSent RESPONSE BUFFER has REMAINING: "
                        + this.sendRequest.getResponseBuffer().remaining() + "/" + this.sendRequest.getResponseBuffer().limit());
                this.handleInvalidRequest(sendRequest);
            }
        } else {
            logger.error("responseDataSent req status != RESPONSE_PENDING = " + this.sendRequest.getStatus());
        }
    }

    /**
     * <p>getNextWriteBuffer</p>
     *
     * @return the next buffer to write or null if none
     */
    public ByteBuffer getNextWriteBuffer() {
        if (this.sendRequest != null) {
            logger.debug("getNextWriteBuffer : sendRequest != null");
            return null;
        }
        if (this.sendQueue.isEmpty()) {
            logger.debug("getNextWriteBuffer : disptachQueue.isEmpty()");
            return null;
        }
        this.sendRequest = this.sendQueue.poll();

        if (this.sendRequest.getStatus() == ServerRequestStatus.DATA_RECEIVED) {

        } else {
            logger.debug("getNextWriteBuffer: sendReq status != DATA_RECEIVED");
        }
        if (this.sendRequest.getStatus() == ServerRequestStatus.FINISHED) {
            this.sendRequest.close();
            this.sendRequest = null;
            return getNextWriteBuffer();
        } else if (this.sendRequest.getStatus() == ServerRequestStatus.RESPONSE_READY) {
            this.sendRequest.setStatus(ServerRequestStatus.RESPONSE_PENDING);
            return this.sendRequest.getResponseBuffer();
        } else {
            logger.debug("getNextWriteBuffer: sendReq status != RESPONSE_READY");
            //this.handleInvalidRequest(this.sendRequest);
        }
        return null;
    }

    /**
     * Called by the command handler to notify that the request has been fully handled.
     *
     * @param req the request that has been handled
     */
    public void handlerComplete(final P2PPServerRequestContext req) {
        logger.debug("handlerComplete");
        if (req.getStatus() == ServerRequestStatus.DISCARDED) {
            this.handleInvalidRequest(req);
            return;
        }
        if (req.hasResponse()) {
            logger.debug("handlerComplete REQ HAS RESP: RESPONSE_READY");
            req.setStatus(ServerRequestStatus.RESPONSE_READY);
        } else {
            logger.debug("handlerComplete REQ HAS NO RESP: FINISHED");
            req.setStatus(ServerRequestStatus.FINISHED);
            req.close();
        }
    }

    /**
     * <p>getNextDispatchRequest</p>
     *
     * @return the next request to dispatch, or null if none
     */
    public P2PPServerRequestContext getNextDispatchRequest() {
//        if (this.dispatchRequest != null
//                && this.dispatchRequest.getStatus() == ServerRequestStatus.DATA_RECEIVED) {
//            P2PPServerRequestContext req = this.dispatchRequest;
//            req.setStatus(ServerRequestStatus.DISPTACHED);
//            return req;
//        } else {
//            logger.debug("getNextDispatchRequest: unhandled status: " + this.recvRequest.getStatus());
//        }
        //logger.debug("DISPATCH NEXT REQUEST DISPACH QUEUE SIZE = " + this.sendQueue.size());
//        if (!this.sendQueue.isEmpty()) {
//            P2PPServerRequestContext req = this.sendQueue.peekLast(); //Note: last instead of first
//
//            if (req.getStatus() == ServerRequestStatus.DATA_RECEIVED) {
//                req.setStatus(ServerRequestStatus.DISPTACHED);
//                return req;
//            } else if (req.getStatus() == ServerRequestStatus.FINISHED) {
//                this.sendQueue.pollLast().close();
//                return getNextDispatchRequest();
//            } else {
//                logger.debug("getNextDispatchRequest: unhandled status: " + req.getStatus());
//            }
//        } else {
//            logger.debug("No request to dispatch");
//        }
        return null;
    }

    /**
     * Discards the given request and closes connection if something went wrong (I/O, protocol, ...).
     *
     * @param req the faulty request
     */
    private synchronized void handleInvalidRequest(final P2PPServerRequestContext req) {
        req.setStatus(ServerRequestStatus.DISCARDED);
        //this.server.closeClient(this);
    }

}
