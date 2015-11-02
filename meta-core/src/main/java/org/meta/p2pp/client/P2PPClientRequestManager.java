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
import java.nio.channels.CompletionHandler;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
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
public class P2PPClientRequestManager implements CompletionHandler<Void, Void> {

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
     * Buffers to use when there is no request.
     */
    private ByteBuffer headerBuffer;

    /**
     * If the header buffer is currently in use by read().
     */
    private boolean headerReading;

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
        this.readQueue = new ArrayDeque<>();
        this.sendQueue = new ArrayDeque<>();
        //this.waitQueue = new ArrayDeque<>();
        this.headerBuffer = BufferManager.createDirectBuffer(P2PPConstants.RESPONSE_HEADER_SIZE);
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
        return this.socket != null && this.socket.isOpen() && connected;
    }

    /**
     *
     * @return true if currently trying to establish the connection
     */
    public final boolean isConnecting() {
        return this.connecting;
    }

    /**
     * Try to connect to the server peer.
     *
     * @param socketChannel the socket to connect to
     */
    public final void connect(final AsynchronousSocketChannel socketChannel) {
        this.connected = false;
        this.connecting = true;
        this.socket = socketChannel;
        this.reset();
        this.socket.connect(peer.getSocketAddr(), null, this);
    }

    /**
     * Closes the connection and dispose of any pending requests.
     */
    public void close() {
        try {
            this.connected = false;
            this.connected = false;
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException ex) {
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
     * The socket has successfully connected to the server peer.
     *
     * @param v useless
     * @param a should == this, but we are our own completion handler
     */
    @Override
    public synchronized void completed(final Void v, final Void a) {
        connected = true;
        connecting = false;
        logger.debug("Connected to " + this.peer);
        scheduleRequests();
    }

    /**
     * Called when the socket failed to connect.
     *
     * @param thrwbl the exception that caused the connection failure.
     * @param a nothing
     */
    @Override
    public void failed(final Throwable thrwbl, final Void a) {
        logger.warn("Failed to connect to " + this.peer);
        connecting = false;
        if (!this.sendQueue.isEmpty()) {
            for (P2PPRequest req : this.sendQueue) {
                req.setFailed("Failed to connect.");
            }
        }
        this.client.closeConnection(this.peer);
    }

    /**
     * Reset internal state and put all pending requests to initial state, waiting for a new socket.
     *
     * Useful on server disconnection, when trying to re-send existing requests.
     */
    public void reset() {
        //TODO
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
        //Build the request buffer
        if (!req.build(this.nextToken())) {
            //if the request failed to build then the user (or the dev...) made a mistake!
            req.setFailed("Failed to build request.");
            return;
        } else {
            req.setStatus(ClientRequestStatus.BUILT);
            logger.debug("Request successfully built.");
        }
        logger.info("Adding request. Cmd id = " + req.getCommandId());
        if (!this.sendQueue.add(req)) {
            logger.info("FAILEDD TO ADD REQUESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
        }
        this.scheduleRequests();
    }

    /**
     * Called by the async write handler to notify us that request data has been sent to the server peer.
     */
    public synchronized void dataSent() {
        logger.debug("Data sent!");
        P2PPRequest req = this.sendQueue.element();

        if (req.getStatus() == ClientRequestStatus.SEND_PENDING) {
            if (!req.getBuffer().hasRemaining()) {
                if (req.hasResponse()) {
                    req.setStatus(ClientRequestStatus.SEND_COMPLETE);
                } else {
                    req.setStatus(ClientRequestStatus.FINISHED);
                }
            }
        }
        this.scheduleRequests();
    }

    /**
     * Called by the async read handler to notify us that response data has been received.
     */
    public void dataReceived() {
        logger.debug("Data received!");
        if (this.readQueue.isEmpty()) {
            logger.info("BIG PROBLEM!!! data received and read queue empty!!!!!!!!");
            logger.info("!! Header reading ? " + headerReading + " buffer full ? " + headerBuffer.hasRemaining());
            return;
        }
        P2PPRequest req = this.readQueue.element();
        ClientRequestStatus status = req.getStatus();

        if (status == ClientRequestStatus.RESPONSE_HEADER_PENDING) {
            if (!this.headerBuffer.hasRemaining()) {
                //Header has been fully received
                if (!req.getResponseHandler().parseHeader(headerBuffer)) {
                    logger.warn("Failed to parse response header!");
                    this.client.closeConnection(this.peer);
                    return;
                }
                headerReading = false;
                req.setStatus(ClientRequestStatus.RESPONSE_HEADER_RECEIVED);
            }
        } else if (status == ClientRequestStatus.RESPONSE_PENDING) {
            if (!req.getResponseHandler().getPayloadBuffer().hasRemaining()) {
                //Response has been fully received.
                req.setStatus(ClientRequestStatus.FINISHED);
            }
        } else {
            logger.info("Data received and no ******* matching status..........");
        }
        scheduleRequests();
    }

    /**
     * Schedule I/O operations on queues of requests.
     */
    private synchronized void scheduleRequests() {
        if (!isConnected()) {
            logger.info("Not connected yet, skipping request scheduling.");
            //Nothing to do if we are not connected yet...
            return;
        }
        if (!this.sendQueue.isEmpty()) {
            logger.debug("Send queue not empty.");
            ClientRequestStatus status = this.sendQueue.element().getStatus();

            if (status == ClientRequestStatus.SEND_COMPLETE) {
                if (!this.readQueue.add(this.sendQueue.poll())) {
                    logger.info("FAILED TO PASS REQUEST FROM SEND TO READ QUEUE!!!!");
                }
            } else if (status == ClientRequestStatus.FINISHED) {
                logger.debug("Send complete, finished.");
                this.sendQueue.poll().finish();
            }
        }
        if (!this.readQueue.isEmpty()) {
            logger.debug("Read queue not empty.");

            if (this.readQueue.element().getStatus() == ClientRequestStatus.FINISHED) {
                //logger.debug("Response received: finished.");
                this.readQueue.poll().finish();
            }
        }
        this.scheduleSend();
        this.scheduleReceive();
    }

    /**
     * Sends the current request to the server peer.
     */
    private void scheduleSend() {
        if (!this.sendQueue.isEmpty()) {
            P2PPRequest req = this.sendQueue.element();

            if (req.getStatus() == ClientRequestStatus.BUILT) {
                req.setStatus(ClientRequestStatus.SEND_PENDING);
                this.socket.write(req.getBuffer(), P2PPConstants.WRITE_TIMEOUT, TimeUnit.SECONDS,
                        this, this.client.getWriteHandler());
            }
        }
    }

    /**
     * Read from the server peer, if possible.
     */
    private void scheduleReceive() {
        boolean prepareHeaderBuffer = false;
        ByteBuffer buf = null;

        if (this.readQueue.isEmpty()) {
            if (!headerReading) {
                prepareHeaderBuffer = true;
            }
        } else {
            P2PPRequest req = this.readQueue.element();
            ClientRequestStatus status = req.getStatus();

            if (status == ClientRequestStatus.SEND_COMPLETE) {
                req.setStatus(ClientRequestStatus.RESPONSE_HEADER_PENDING);
                if (!headerReading) {
                    prepareHeaderBuffer = true;
                }
            } else if (status == ClientRequestStatus.RESPONSE_HEADER_RECEIVED) {
                buf = req.getResponseHandler().getPayloadBuffer();
                req.setStatus(ClientRequestStatus.RESPONSE_PENDING);
            }
        }
        if (prepareHeaderBuffer) {
            this.headerBuffer.rewind();
            buf = this.headerBuffer;
            headerReading = true;
        }
        if (buf != null) {
            this.socket.read(buf, P2PPConstants.READ_TIMEOUT, TimeUnit.SECONDS,
                    this, this.client.getReadHandler());
        }
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
     * @return the server peer of this context
     */
    public MetaPeer getServerPeer() {
        return this.peer;
    }

}
