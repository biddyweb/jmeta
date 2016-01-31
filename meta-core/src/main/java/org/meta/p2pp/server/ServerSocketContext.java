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
import java.util.LinkedList;
import java.util.Queue;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ServerRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ServerSocketContext class.</p>
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class ServerSocketContext implements P2PPServerEventHandler {

    private final Logger logger = LoggerFactory.getLogger(ServerSocketContext.class);

    /**
     * The opened socket.
     */
    private AsynchronousSocketChannel socket;

    private P2PPServerRequestContext readRequest;

    private final Queue<P2PPServerRequestContext> sendQueue;

    private final ByteBuffer headerBuffer;

    /**
     * <p>Constructor for ServerSocketContext.</p>
     */
    public ServerSocketContext() {
        sendQueue = new LinkedList<>();
        this.headerBuffer = BufferManager.aquireDirectBuffer(P2PPConstants.REQUEST_HEADER_SIZE);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized ServerActionContext accepted(final AsynchronousSocketChannel sock) {
        this.socket = sock;
        return getWritableBuffer(new ServerActionContext(this));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized ServerActionContext dataReceived(final ServerActionContext context) {
        ByteBuffer buf = context.getAttachment();
        if (buf.hasRemaining()) {
            //We received data but not all of it, READ again.
            return context;
        }
        if (this.readRequest == null) {
            //Data received but no associated request context, big problem
            return context.setAction(ServerEventAction.ERROR);
        }
        if (this.readRequest.getStatus() == P2PPConstants.ServerRequestStatus.HEADER_PENDING) {
            if (!this.readRequest.parseRequestHeader(buf)) {
                return context.setAction(ServerEventAction.ERROR);
            }
            if (this.readRequest.hasPayload()) {
                this.readRequest.setStatus(P2PPConstants.ServerRequestStatus.HEADER_RECEIVED);
            } else {
                //If the request contains only the header, dispatch it now
                this.readRequest.setStatus(P2PPConstants.ServerRequestStatus.DISPTACHED);
                ServerActionContext dispatchAction = new ServerActionContext(this);
                dispatchAction.setAction(ServerEventAction.DISPATCH).setAttachment(this.readRequest);
                context.next(dispatchAction);
                this.readRequest = null;
            }
        } else if (this.readRequest.getStatus() == P2PPConstants.ServerRequestStatus.DATA_PENDING) {
            this.readRequest.setStatus(P2PPConstants.ServerRequestStatus.DISPTACHED);
            ServerActionContext dispatchAction = new ServerActionContext(this);
            dispatchAction.setAction(ServerEventAction.DISPATCH).setAttachment(this.readRequest);
            context.next(dispatchAction);
            this.sendQueue.add(readRequest);
            this.readRequest = null;
        }
        return getWritableBuffer(context);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized ServerActionContext dataSent(final ServerActionContext context) {
        logger.info("Data sent");
        ByteBuffer buf = context.getAttachment();
        if (buf.hasRemaining()) {
            //We have received data but not all of it, WRITE again
            return context;
        }
        P2PPServerRequestContext req = this.sendQueue.peek();
        if (req == null) {
            return context.setAction(ServerEventAction.ERROR);
        }
        if (req.getStatus() == ServerRequestStatus.RESPONSE_PENDING) {
            //Response has been sent, finished.
            this.sendQueue.poll();
        } else {
            return context.setAction(ServerEventAction.ERROR);
        }
        return getSendableBuffer(context);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized ServerActionContext handlerComplete(final ServerActionContext context) {
        if (context.getAction() == ServerEventAction.ERROR) {
            return context;
        }
        P2PPServerRequestContext req = context.getAttachment();
        if (req.getStatus() == ServerRequestStatus.DISCARDED) {
            return context.setAction(ServerEventAction.ERROR);
        }
        if (req.hasResponse()) {
            req.setStatus(ServerRequestStatus.RESPONSE_READY);
            //this.sendQueue.add(req);
        } else {
            req.setStatus(ServerRequestStatus.FINISHED);
            //The request has been handled but do not need to send back a response, nothing to do
        }
        return getSendableBuffer(context);
    }

    /**
     * Updates the given action for a READ action.
     *
     * @param context the context to update
     */
    private ServerActionContext getWritableBuffer(final ServerActionContext context) {
        if (this.readRequest == null) {
            this.readRequest = new P2PPServerRequestContext();
            this.readRequest.setStatus(ServerRequestStatus.HEADER_PENDING);
            this.headerBuffer.rewind();
            context.setAction(ServerEventAction.READ);
            context.setAttachment(this.headerBuffer);
        } else if (this.readRequest.getStatus() == ServerRequestStatus.HEADER_RECEIVED) {
            this.readRequest.setStatus(ServerRequestStatus.DATA_PENDING);
            context.setAction(ServerEventAction.READ).setAttachment(this.readRequest.getDataBuffer());
        } else {
            logger.warn("getWritableBuffer: read request status != HEADER_RECEIVED");
            context.setAction(ServerEventAction.ERROR);
        }
        return context;
    }

    /**
     * Updates the given action for a WRITE action.
     *
     * @param context the action context to update
     */
    private ServerActionContext getSendableBuffer(final ServerActionContext context) {
        if (this.sendQueue.isEmpty()) {
            return context.setAction(ServerEventAction.NONE);
        }
        P2PPServerRequestContext req = this.sendQueue.peek();
        if (req.getStatus() == ServerRequestStatus.FINISHED) {
            //Requests without responses are in the send queue but do not need to send anything
            this.sendQueue.poll();
            return getSendableBuffer(context);
        } else if (req.getStatus() == ServerRequestStatus.DISPTACHED
                || req.getStatus() == ServerRequestStatus.RESPONSE_PENDING) {
            return context.setAction(ServerEventAction.NONE);
        }
        if (req.getStatus() == ServerRequestStatus.RESPONSE_READY) {
            req.setStatus(ServerRequestStatus.RESPONSE_PENDING);
            return context.setAction(ServerEventAction.WRITE).setAttachment(req.getResponseBuffer());
        }
        return context.setAction(ServerEventAction.ERROR).setAttachment(null);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized ServerActionContext error() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void close() {
        try {
            this.socket.close();
        } catch (final IOException ex) {
            logger.error("Error while closing client socket.", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public AsynchronousSocketChannel getSocket() {
        return this.socket;
    }

}
