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
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.meta.api.common.MetaPeer;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ClientRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible of managing requests, connection and I/O operations of a socket to a server peer.
 *
 * Manages read/write and queuing for requests.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class P2PPClientRequestManager {

    private final Logger logger = LoggerFactory.getLogger(P2PPClientRequestManager.class);

    /**
     * The peer acting as the server we are connected to.
     */
    private final MetaPeer peer;

    /**
     * The socket to the server peer.
     */
    private AsynchronousSocketChannel socket;

    /**
     *
     */
    private final P2PPClient client;

    /**
     * Request waiting for a response from the server peer.
     */
    private final Queue<P2PPRequest> readQueue;

    /**
     * Requests being sent to the server peer.
     */
    private final Queue<P2PPRequest> sendQueue;

    private volatile P2PPRequest sendRequest;

    private volatile P2PPRequest recvRequest;

    /**
     * If the header buffer is in use or not.
     */
    private volatile boolean headerBufferReading;

    /**
     * Buffers used to read response headers.
     */
    private final ByteBuffer headerBuffer;

    /**
     * Unique token to give to request.
     */
    private char token = 0;

    /**
     * If connected or not to the server peer.
     */
    private boolean connected;

    /**
     * If currently trying to establish the connection.
     */
    private boolean connecting;

    /**
     * Creates a new context representing the client request sent to a server peer.
     *
     * @param serverPeer the server peer
     * @param p2ppClient the p2pp client manager
     */
    public P2PPClientRequestManager(final MetaPeer serverPeer, final P2PPClient p2ppClient) {
        this.peer = serverPeer;
        this.client = p2ppClient;
        this.sendQueue = new ConcurrentLinkedQueue<>();
        this.readQueue = new ConcurrentLinkedQueue<>();
        this.headerBuffer = BufferManager.aquireDirectBuffer(P2PPConstants.RESPONSE_HEADER_SIZE);
    }

    /**
     *
     * @return the token for the next request
     */
    private char nextToken() {
        return token++;
    }

    /**
     * <p>Getter for the field <code>socket</code>.</p>
     *
     * @return the manager socket
     */
    public final AsynchronousSocketChannel getSocket() {
        return this.socket;
    }

    /**
     * <p>isConnected</p>
     *
     * @return true if the socket is connected, false otherwise.
     */
    public synchronized boolean isConnected() {
        return this.connected;
    }

    /**
     * <p>isConnecting</p>
     *
     * @return true if currently trying to establish the connection
     */
    public synchronized boolean isConnecting() {
        return this.connecting;
    }

    /**
     * Set the context's state to 'connected'.
     */
    public synchronized void connected() {
        this.connecting = false;
        this.connected = true;
    }

    /**
     * Set the context's state to 'connecting'.
     *
     * @param socketChannel the socket we are connecting to
     */
    public synchronized void connecting(final AsynchronousSocketChannel socketChannel) {
        this.connected = false;
        this.connecting = true;
        this.socket = socketChannel;
    }

    /**
     * Closes the connection and dispose of any pending requests.
     */
    public void close() {
        try {
            this.connected = false;
            this.connecting = false;
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (final IOException ex) {
        }
        String failedReason = "Close requested";
        for (P2PPRequest req : this.readQueue) {
            req.setFailed(failedReason);
        }
        for (P2PPRequest req : this.sendQueue) {
            req.setFailed(failedReason);
        }
        BufferManager.release(headerBuffer);
    }

    /**
     * Add a new request to be sent to the server peer.
     *
     * Sends the request immediately if possible, otherwise it is put in the wait queue, waiting for pending
     * requests to finish.
     *
     * The outcome of the request is accessible using the request operation
     * {@code P2PPRequest.getOperation()}.
     *
     * @param req the request to send
     */
    public void addRequest(final P2PPRequest req) {
        if (!this.sendQueue.add(req)) {
            logger.error("FAILED TO ADD REQUEST TO SEND QUEUE");
            req.getOperation().setFailed("Failed to add request to send queue.");
        }
        logger.debug("Adding request. Cmd id = " + req.getCommandId());
    }

    /**
     * Called by the async write handler to notify us that request data has been sent to the server peer.
     */
    public void dataSent() {
        if (this.sendRequest == null) {
            logger.error("dataSent: SEND REQ == NULL!!");
            this.client.closeContext(peer);
            return;
        }
        if (this.sendRequest.getStatus() != ClientRequestStatus.SEND_PENDING) {
            logger.debug("dataSent SEND REQ STATUS != SEND_PENDING");
            this.client.closeContext(peer);
            return;
        }
        if (this.sendRequest.getBuffer().hasRemaining()) {
            logger.debug("dataSent SEND REQ BUFFER HAS REMAINING!");
            this.client.closeContext(peer);
            return;
        }
        if (this.sendRequest.hasResponse()) {
            this.sendRequest.setStatus(ClientRequestStatus.SEND_COMPLETE);
            if (!this.readQueue.add(this.sendRequest)) {
                logger.error("FAILED TO MOVE REQUEST FROM SEND QUEUE TO READ QUEUE!");
                this.client.closeContext(peer);
            }
            this.sendRequest = null;
        } else {
            this.sendRequest.finish();
            this.sendRequest = null;
        }
    }

    /**
     * Schedule requests and find the next buffer to be written.
     *
     * Null is returned if there is nothing to write or if we are already reading.
     *
     * Otherwise the next pending request is built, its status set to {@code SEND_PENDING} and its buffer is
     * returned.
     *
     * @return the next buffer that needs to be written to server peer or null
     */
    ByteBuffer getNextWriteBuffer() {
        if (this.sendRequest != null) {
            logger.debug("getNextWriteBuffer: sendReq != null");
            return null;
        }
        if (this.sendQueue.isEmpty()) {
            logger.debug("getNextWriteBuffer: sendQueue.isEmpty");
            return null;
        }
        this.sendRequest = this.sendQueue.poll();
        if (!this.sendRequest.build(this.nextToken())) {
            //if the request failed to build then the user (or the dev...) made a mistake!
            this.sendRequest.setFailed("Failed to build request.");
            this.sendRequest = null;
            return this.getNextWriteBuffer();
        }
        this.sendRequest.setStatus(ClientRequestStatus.SEND_PENDING);
        return this.sendRequest.getBuffer();
    }

    /**
     * Called by the async read handler to notify us that response data has been received.
     */
    public void dataReceived() {
        if (headerBufferReading) {
            headerBufferReading = false;
            if (this.recvRequest == null) {
                if (this.readQueue.isEmpty()) {
                    logger.error("dataReceived: readQueue.isEmpty() AND recvRequest == null!!!!!!!");
                    this.client.closeContext(peer);
                    return;
                }
                this.recvRequest = this.readQueue.poll();
            }
            if (!this.recvRequest.getResponseHandler().parseHeader(headerBuffer)) {
                this.recvRequest.setFailed("Failed to parse response header.");
                this.client.closeContext(peer);
                return;
            }
            this.recvRequest.setStatus(ClientRequestStatus.RESPONSE_HEADER_RECEIVED);
        } else {
            if (this.recvRequest == null) {
                logger.error("dataReceived: recvRequest == null!!!!!!!");
                this.client.closeContext(peer);
                return;
            }
//            if (!this.recvRequest.getResponseHandler().parse()) {
//                this.recvRequest.setFailed("Failed to parse response.");
//                this.client.closeContext(peer);
//                return;
//            }
            this.recvRequest.finish();
            this.recvRequest = null;
        }
    }

    /**
     * Schedule requests and find the next buffer to be read.
     *
     * @return the next buffer that needs to be filled with read bytes or null if not needed
     */
    ByteBuffer getNextReadBuffer() {
        if (headerBufferReading) {
            logger.debug("getNextReadBuffer: headerBufferReading");
            return null;
        }
        if (this.recvRequest != null) {
            if (this.recvRequest.getStatus() == ClientRequestStatus.RESPONSE_HEADER_RECEIVED) {
                this.recvRequest.setStatus(ClientRequestStatus.RESPONSE_PENDING);
                return this.recvRequest.getResponseHandler().getPayloadBuffer();
            }
        } else if (!this.readQueue.isEmpty()) {
            this.recvRequest = this.readQueue.poll();
            this.recvRequest.setStatus(ClientRequestStatus.RESPONSE_HEADER_PENDING);
            headerBufferReading = true;
            this.headerBuffer.rewind();
            return this.headerBuffer;
        } else {
            //No requests in read queue
            return null;
        }
        return null;
    }

    /**
     * <p>getServerPeer</p>
     *
     * @return the server peer of this context
     */
    public MetaPeer getServerPeer() {
        return this.peer;
    }
}
