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
import java.util.LinkedList;
import java.util.Queue;
import org.meta.api.common.MetaPeer;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ClientRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ClientSocketContext class.</p>
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class ClientSocketContext implements P2PPClientEventHandler {

    private final Logger logger = LoggerFactory.getLogger(ClientSocketContext.class);

    private final MetaPeer serverPeer;

    /**
     * The socket to the server peer.
     */
    private AsynchronousSocketChannel socket;

    private final SocketIOState ioState;

    private final Queue<P2PPRequest> readQueue;

    private final Queue<P2PPRequest> sendQueue;

    private final ByteBuffer headerBuffer;

    /**
     * Unique token to give to request.
     */
    private char token = 0;

    ClientSocketContext(final MetaPeer peer) {
        this.serverPeer = peer;
        this.ioState = new SocketIOState();
        this.readQueue = new LinkedList<>();
        this.sendQueue = new LinkedList<>();
        headerBuffer = BufferManager.aquireBuffer(P2PPConstants.RESPONSE_HEADER_SIZE);
    }

    /**
     *
     * @return the token for the next request
     */
    private char nextToken() {
        return token++;
    }

    /**
     * <p>
     * addRequest</p>
     *
     * @param req the request to add
     * @return the resulting I/O action
     */
    public synchronized ClientActionContext addRequest(final P2PPRequest req) {
        ClientActionContext ioContext = new ClientActionContext(this);
        if (!this.sendQueue.add(req)) {
            return ioContext.setAction(ClientAction.ERROR);
        }
        if (!ioState.isConnected()) {
            if (ioState.isConnecting()) {
                return ioContext.setAction(ClientAction.NONE);
            }
            return ioContext.setAction(ClientAction.CONNECT);
        }
        if (!ioState.isWriting()) {
            getSendableBuffer(ioContext);
            //No other check needed, we know we can write because a request has just been added.
            if (ioContext.getAction() == ClientAction.WRITE) {
                ioState.writing();
            }
            return ioContext;
        }
        return ioContext.setAction(ClientAction.NONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void connecting(final AsynchronousSocketChannel s) {
        this.socket = s;
        ioState.connecting();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ClientActionContext connected(final ClientActionContext ioContext) {
        logger.info("CONNECTED");
        ioState.connecting(false);
        ioState.connected();
        //We just connected to the server peer, there is no need to check current I/O read/write states
        //as we known we can read and write.
        getWritableBuffer(ioContext);
        if (ioContext.getAction() == ClientAction.READ) {
            ioState.reading();
            ClientActionContext sendContext = new ClientActionContext(this);
            ioContext.next(sendContext);
            getSendableBuffer(sendContext);
            if (sendContext.getAction() == ClientAction.WRITE) {
                ioState.writing();
            }
            logger.info("connected: getSendableBuffer io action = " + sendContext.getAction());
        }
        logger.info("connected: getWritableBuffer io action = " + ioContext.getAction());
        return ioContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ClientActionContext dataReceived(final ClientActionContext ioContext) {
        ByteBuffer buf = ioContext.getAttachment();
        if (buf.hasRemaining()) {
            logger.info("dataReceived: buffer has remaining");
            //We received data but not all of it, READ again
            return ioContext;
        }
        if (this.readQueue.isEmpty()) {
            logger.info("==================== Avoiding sync problem by re-reading for nothing....");
            return ioContext;
        }
        ioState.reading(false);
        P2PPRequest req = this.readQueue.peek();
        if (req == null) {
            //If we received data and there is no associated request, there is a big sync problem
            logger.error("dataReceived: received data but no associated request, BIG ISSUE");
            logger.error("THIS FUCKING SHOULD NEVER FUCKING HAPPEN, FUKING EVER EVER EVER");
            return ioContext.setAction(ClientAction.ERROR);
        }
        if (req.getStatus() == ClientRequestStatus.RESPONSE_HEADER_PENDING) {
            if (!req.getResponseHandler().parseHeader(ioContext.getAttachment())) {
                logger.error("Failed to parse response header!!!!!!");
                return ioContext.setAction(ClientAction.ERROR);
            }
            req.setStatus(ClientRequestStatus.RESPONSE_HEADER_RECEIVED);
        } else if (req.getStatus() == ClientRequestStatus.RESPONSE_PENDING) {
            if (!req.getResponseHandler().parse(ioContext.getAttachment())) {
                logger.error("Failed to parse response Body!!!!!!");
                return ioContext.setAction(ClientAction.ERROR);
            }
            req.setStatus(ClientRequestStatus.RESPONSE_RECEIVED);
            this.readQueue.poll();
            ClientActionContext reqContext = new ClientActionContext(this);
            reqContext.setAction(ClientAction.COMPLETE_REQUEST).setAttachment(req);
            ioContext.next(reqContext);
        } else {
            return ioContext.setAction(ClientAction.ERROR);
        }
        //We always need to read to be aware of the socket state
        ioState.reading();
        return getWritableBuffer(ioContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ClientActionContext dataSent(final ClientActionContext ioContext) {
        ByteBuffer buf = ioContext.getAttachment();
        if (buf.hasRemaining()) {
            //We sen data but not all of it, WRITE again
            return ioContext;
        }
        ioState.writing(false);
        P2PPRequest req = this.sendQueue.peek();
        if (req == null) {
            //If we sent someting but there is no associated request, there is a big problem
            return ioContext.setAction(ClientAction.ERROR);
        }
        if (req.getStatus() != ClientRequestStatus.SEND_PENDING) {
            return ioContext.setAction(ClientAction.ERROR);
        }
        //Once the buffer has been sent, we can safely release it.
        BufferManager.release(req.getBuffer());
        if (req.hasResponse()) {
            req.setStatus(ClientRequestStatus.RESPONSE_HEADER_PENDING);
            if (!this.readQueue.add(this.sendQueue.poll())) {
                return ioContext.setAction(ClientAction.ERROR);
            }
        } else {
            //This request doesn't have a response, just remove it
            this.sendQueue.poll();
            ClientActionContext reqContext = new ClientActionContext(this);
            reqContext.setAction(ClientAction.COMPLETE_REQUEST).setAttachment(req);
            ioContext.next(reqContext);
        }
        getSendableBuffer(ioContext);
        if (ioContext.getAction() == ClientAction.WRITE) {
            ioState.writing();
        }
        return ioContext;
    }

    /**
     * Called by IO event handler to get the next sendable buffer.
     *
     * Updates the given context action and buffer according to the state.
     *
     * If there is no valid sendable buffer, context results in an error
     *
     * @param context
     * @return the updated I/O action context
     */
    private ClientActionContext getSendableBuffer(final ClientActionContext context) {
        P2PPRequest req = this.sendQueue.peek();
        if (req == null) {
            return context.setAction(ClientAction.NONE);
        }
        if (req.getStatus() != ClientRequestStatus.CREATED) {
            //We can only get sendable buffer from a not-yet-built request
            return context.setAction(ClientAction.ERROR);
        }
        if (!req.build(nextToken())) {
            logger.error("getSendableBuffer failed to build request.");
            return context.setAction(ClientAction.ERROR);
        }
        req.setStatus(ClientRequestStatus.SEND_PENDING);
        return context.setAttachment(req.getBuffer()).setAction(ClientAction.WRITE);
    }

    /**
     * Get the next ByteBuffer that will be used to read the server response.
     *
     * @param context
     * @return the updated I/O action context
     */
    private ClientActionContext getWritableBuffer(final ClientActionContext context) {
        P2PPRequest req = this.readQueue.peek();
        if (req == null || req.getStatus() == ClientRequestStatus.RESPONSE_HEADER_PENDING) {
            //We read the header buffer even if there is no request just to keep watching the socket state
            headerBuffer.rewind();
            return context.setAction(ClientAction.READ).setAttachment(headerBuffer);
        }
        if (req.getStatus() == ClientRequestStatus.RESPONSE_HEADER_RECEIVED) {
            req.setStatus(ClientRequestStatus.RESPONSE_PENDING);
            return context.setAction(ClientAction.READ).
                    setAttachment(req.getResponseHandler().getPayloadBuffer());
        }
        return context.setAction(ClientAction.ERROR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ClientActionContext error() {
        //We might want (one day?) try to reconnect on error, or handle errors differently
        return new ClientActionContext(this).setAction(ClientAction.ERROR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() {
        try {
            socket.close();
        } catch (final IOException ex) {
            logger.warn("IOException while closing socket.", ex);
        }
        for (P2PPRequest req : this.sendQueue) {
            req.setFailed("Closing P2PP Client");
        }
        for (P2PPRequest req : this.readQueue) {
            req.setFailed("Closing P2PP Client");
        }
        BufferManager.release(headerBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaPeer getServerPeer() {
        return this.serverPeer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AsynchronousSocketChannel getSocket() {
        return this.socket;
    }

}
