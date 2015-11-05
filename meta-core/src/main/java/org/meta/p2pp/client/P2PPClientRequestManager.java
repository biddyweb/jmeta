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
import java.util.ArrayDeque;
import java.util.Queue;
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
     * Requests waiting to be sent to the server peer.
     */
    //private final Queue<P2PPRequest> waitQueue;
    /**
     * Request waiting for a response from the server peer.
     */
    private final Queue<P2PPRequest> readQueue;

    /**
     * Requests being sent to the server peer.
     */
    private final Queue<P2PPRequest> sendQueue;

    /**
     * Requests waiting to be handled.
     */
    private final Queue<P2PPRequest> waitQueue;

    /**
     * Buffers to use when there is no request.
     */
    private boolean headerBufferReading;

    /**
     * Buffers to use when there is no request.
     */
    private final ByteBuffer headerBuffer;

    /**
     * Unique token to give to request.
     */
    private short token = 0;

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
        this.sendQueue = new ArrayDeque<>(P2PPConstants.CONCURRENT_CLIENT_REQUESTS);
        this.readQueue = new ArrayDeque<>();
        this.waitQueue = new ArrayDeque<>();
        this.headerBuffer = BufferManager.aquireDirectBuffer(P2PPConstants.RESPONSE_HEADER_SIZE);
    }

    /**
     *
     * @return the token for the next request
     */
    private short nextToken() {
        return token++;
    }

    /**
     *
     * @return the manager socket
     */
    public final AsynchronousSocketChannel getSocket() {
        return this.socket;
    }

    /**
     *
     * @return true if the socket is connected, false otherwise.
     */
    public final boolean isConnected() {
        return this.connected;
    }

    /**
     *
     * @return true if currently trying to establish the connection
     */
    public final boolean isConnecting() {
        return this.connecting;
    }

    /**
     * Set the context's state to 'connected'.
     */
    public void connected() {
        this.connecting = false;
        this.connected = true;
    }

    /**
     * Set the context's state to 'connecting'.
     *
     * @param socketChannel the socket we are connecting to
     */
    public final void connecting(final AsynchronousSocketChannel socketChannel) {
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
        if (sendQueue.size() < P2PPConstants.CONCURRENT_CLIENT_REQUESTS) {
            if (!this.sendQueue.add(req)) {
                logger.error("FAILED TO ADD REQUEST TO SEND QUEUE");
            }
        } else {
            if (!this.waitQueue.add(req)) {
                logger.error("FAILED TO ADD REQUEST TO WAIT QUEUE");
            }
        }
        logger.info("Adding request. Cmd id = " + req.getCommandId());
    }

    /**
     * Called by the async write handler to notify us that request data has been sent to the server peer.
     */
    public synchronized void dataSent() {
        P2PPRequest req = this.sendQueue.peek(); //element();

        if (req != null && req.getStatus() == ClientRequestStatus.SEND_PENDING) {
            if (!req.getBuffer().hasRemaining()) {
                if (!req.hasResponse()) {
                    //req.setStatus(ClientRequestStatus.FINISHED);
                    req.finish();
                    this.sendQueue.poll();
                } else {
                    req.setStatus(ClientRequestStatus.SEND_COMPLETE);
                    if (!this.readQueue.add(this.sendQueue.poll())) {
                        logger.error("FAILED TO MOVE REQUEST FROM SEND QUEUE TO READ QUEUE!");
                    }
                }
            } else {
                logger.warn("DATA SENT BUT buffer not full");
            }
        } else {
            logger.error("Data sent (send queue empty or status != SEND_PENDING) !!");
        }
    }

    /**
     * Called by the async read handler to notify us that response data has been received.
     */
    public void dataReceived() {
        P2PPRequest req = this.readQueue.peek();

        if (req != null) {
            if (headerBufferReading) {
                if (req.getStatus() == ClientRequestStatus.SEND_COMPLETE
                        || req.getStatus() == ClientRequestStatus.RESPONSE_HEADER_PENDING) {
                    if (!headerBuffer.hasRemaining()) {
                        headerBufferReading = false;
                        if (req.getResponseHandler().parseHeader(headerBuffer)) {
                            req.setStatus(ClientRequestStatus.RESPONSE_HEADER_RECEIVED);
                        } else {
                            req.setFailed("Failed to parse response header.");
                            this.client.closeConnection(peer);
                        }
                    } else {
                        logger.warn("req.setStatus(ClientRequestStatus.RESPONSE_HEADER_PENDING); ??");
                    }
                }
            } else {
                if (req.getStatus() == ClientRequestStatus.RESPONSE_PENDING) {
                    if (!req.getResponseHandler().getPayloadBuffer().hasRemaining()) {
                        if (!req.getResponseHandler().parse()) {
                            req.setFailed("Failed to parse response.");
                            this.client.closeConnection(peer);
                        } else {
                            req.finish();
                            this.readQueue.poll();
                        }
                    } else {
                        logger.warn("DATA RECEIVED BUT PAYLOAD BUFFER HAS REMAINING");
                    }
                } else {
                    logger.warn("DATA PAYLOAD RECEIVED BUT reqStatus != " + ClientRequestStatus.RESPONSE_PENDING);
                }
            }
        } else {
            logger.error("DATA RECEIVED AND READ QUEUE EMPTY!!");
        }
    }

    /**
     * Schedule requests and find the next buffer to be read.
     *
     *
     * @return the next buffer that needs to be filled with read bytes
     */
    ByteBuffer getNextReadBuffer() {
        P2PPRequest req = this.readQueue.peek();

        if (req == null) {
            if (!headerBufferReading) {
                this.headerBuffer.rewind();
                headerBufferReading = true;
                return this.headerBuffer;
            } else {
                logger.debug("Header buffer already being filled...");
            }
        } else {
            if (headerBufferReading) {
                if (req.getStatus() == ClientRequestStatus.SEND_COMPLETE) {
//                    req.setStatus(ClientRequestStatus.RESPONSE_HEADER_PENDING);
                }
            } else {
                if (req.getStatus() == ClientRequestStatus.SEND_COMPLETE) {
                    req.setStatus(ClientRequestStatus.RESPONSE_HEADER_PENDING);
                    this.headerBuffer.rewind();
                    headerBufferReading = true;
                    return this.headerBuffer;
                } else if (req.getStatus() == ClientRequestStatus.RESPONSE_HEADER_RECEIVED) {
                    req.setStatus(ClientRequestStatus.RESPONSE_PENDING);
                    return req.getResponseHandler().getPayloadBuffer();
                } else {
                    logger.debug("Next read buffer: the request doesn't need reading... Status=" + req.getStatus());
                }
            }
        }
        return null;
    }

    /**
     *
     * Schedule requests and find the next buffer to be written.
     *
     * Null is returned if there is nothing to write or if we are already reading.
     *
     * Otherwise the next pending request i built, its status set to {@code SEND_PENDING} and its buffer is
     * returned.
     *
     * @return the next buffer that needs to be written to server peer or null
     */
    ByteBuffer getNextWriteBuffer() {
        P2PPRequest req = this.sendQueue.peek();

        if (req == null) {
            if (!waitQueue.isEmpty()) {
                if (!this.sendQueue.add(waitQueue.poll())) {
                    logger.error("FAILED TO PUT REQUEST FROM WAIT QUEUE TO SEND QUEUE!!");
                }
                return getNextWriteBuffer();
            }
        } else {
            logger.debug("getNextWriteBuffer: send queue not empty");
            if (req.getStatus() == ClientRequestStatus.CREATED) {
                logger.debug("getNextWriteBuffer: Building request");
                if (!req.build(this.nextToken())) {
                    //if the request failed to build then the user (or the dev...) made a mistake!
                    req.setFailed("Failed to build request.");
                    this.sendQueue.poll();
                    return this.getNextWriteBuffer();
                } else {
                    req.setStatus(P2PPConstants.ClientRequestStatus.BUILT);
                    logger.debug("Request successfully built.");
                }
            }
            if (req.getStatus() == ClientRequestStatus.BUILT) {
                logger.debug("getNextWriteBuffer: request built, send buffer");
                req.setStatus(ClientRequestStatus.SEND_PENDING);
                return req.getBuffer();
            }
        }
        //We don't need to send anything...
        return null;
    }

    /**
     *
     * @return the server peer of this context
     */
    public MetaPeer getServerPeer() {
        return this.peer;
    }

    /**
     * Reset internal state and put all pending requests to initial state, waiting for a new socket.
     *
     * Useful on server disconnection, when trying to re-send existing requests.
     */
//    public void reset() {
//    }
}
