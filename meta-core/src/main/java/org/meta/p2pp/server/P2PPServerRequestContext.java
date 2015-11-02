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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.P2PPCommand;
import org.meta.p2pp.P2PPConstants.ServerRequestStatus;

/**
 * Represents a request state in an existing client context.
 *
 * It is responsible of the buffers needed to read the request and write the response back to the client.
 *
 * Initial parsing of the request header is also done before dispatching to the actual handler based on the
 * request command identifier.
 */
public class P2PPServerRequestContext {

    private ServerRequestStatus status;

    /**
     * The unique request token, as defined in the protocol.
     */
    private short token;

    /**
     * The command identifier, as defined in the protocol.
     */
    private P2PPCommand commandId;

    /**
     * The size of the request content, as defined in the protocol.
     */
    private int requestDataSize;

    /**
     * The request header buffer (read).
     */
    private ByteBuffer headerBuffer;

    /**
     * The request data (read).
     */
    private ByteBuffer requestBuffer;

    /**
     * The response buffer.
     */
    private ByteBuffer responseBuffer;

    /**
     * The response data (write).
     */
    //private final Queue<ByteBuffer> responseBuffers;
    /**
     *
     */
    public P2PPServerRequestContext() {
        this.headerBuffer = BufferManager.createDirectBuffer(P2PPConstants.REQUEST_HEADER_SIZE);
        this.status = ServerRequestStatus.HEADER_PENDING;
    }

    /**
     * Stops this request for immediate shutdown.
     */
    public void close() {
    }

    /**
     * Reset all internal state to a clean one.
     *
     * After this call, this request can be safely used again.
     *
     * TODO create a pool of Request Contexts ?
     */
    public void reset() {
        this.status = ServerRequestStatus.HEADER_PENDING;
        this.headerBuffer.clear();
        this.token = 0;
        this.requestDataSize = 0;
    }

    /**
     * Called by async completion handlers to notify that internal data has changed.
     *
     * @return the current status of this request after update
     */
    public ServerRequestStatus bufferUpdated() {
        if (this.status == ServerRequestStatus.HEADER_PENDING) {
            if (!this.headerBuffer.hasRemaining()) {
                //We have fully received the request buffer.
                this.status = ServerRequestStatus.HEADER_RECEIVED;
            }
        } else if (this.status == ServerRequestStatus.HEADER_RECEIVED
                || this.status == ServerRequestStatus.DATA_PENDING) {
            if (!this.requestBuffer.hasRemaining()) {
                //We received the data
                this.status = ServerRequestStatus.DATA_RECEIVED;
            }
        }
        return this.status;
    }

    /**
     * Called by the command handler to notify that it finished.
     *
     * Only changes internal status.
     */
    public void handlerComplete() {
        if (this.hasResponseData()) {
            this.status = ServerRequestStatus.RESPONSE_READY;
        } else {
            this.status = ServerRequestStatus.FINISHED;
        }
    }

    /**
     * Called by the async completion handler to notify that this request is ready to be dispatched to
     * appropriate command handler.
     *
     * Only changes internal status.
     */
    public void dispatch() {
        this.status = ServerRequestStatus.DISPTACHED;
    }

    /**
     * Check that the given command identifier is valid.
     *
     * @param id the command identifier to check
     * @return true if valid, false otherwise
     */
    private boolean checkCommandId() {
        return this.commandId != null && this.commandId != P2PPCommand.INVALID;
    }

    /**
     * Check that the given request size is valid according to the protocol specs.
     *
     * @return true if the size is valid, false otherwise
     */
    private boolean checkRequestSize() {
        return this.requestDataSize >= 0 && this.requestDataSize <= P2PPConstants.MAX_REQUEST_DATA_SIZE;
    }

    /**
     * Check that the request header is valid.
     *
     * @param req the request
     * @return true if valid, false otherwise
     */
    private boolean checkHeader() {
        if (!this.checkCommandId()) {
            return false;
        }
        return this.checkRequestSize();
    }

    /**
     * Extracts data from the received request header.
     *
     * @return true if the parsed header is valid, false otherwise
     */
    public boolean parseRequestHeader() {
        this.headerBuffer.rewind(); //Set position to 0 for data access
        try {
            this.token = this.headerBuffer.getShort();
            byte id = this.headerBuffer.get();
            this.commandId = P2PPCommand.fromValue(id);
            this.requestDataSize = this.headerBuffer.getInt();
            return this.checkHeader();
        } catch (BufferUnderflowException e) {
            //What to do here ?? This should not happen...
            return false;
        }
    }

    /**
     * Header is valid, prepare buffer for content retrieval.
     */
    public void allocateDataBuffer() {
        if (requestDataSize > 0) {
            this.requestBuffer = BufferManager.createDirectBuffer(requestDataSize);
            this.status = ServerRequestStatus.DATA_PENDING;
        }
    }

    /**
     * Called by async completion handlers to notify that response data has been sent.
     *
     * @return the current status of this request after update
     */
    public ServerRequestStatus responseDataSent() {
        if (this.status == ServerRequestStatus.RESPONSE_PENDING) {
            if (!this.responseBuffer.hasRemaining()) {
                this.status = ServerRequestStatus.FINISHED;
            }
        }
        return this.status;
    }

    /**
     *
     * @return true if the response data is available and ready to be sent
     */
    public boolean hasResponseData() {
        return this.responseBuffer != null;
    }

    /**
     * @return a ByteBuffer large enough to contain the request header
     */
    public ByteBuffer getHeaderBuffer() {
        return this.headerBuffer;
    }

    /**
     * @return a ByteBuffer large enough to contain the request data
     */
    public ByteBuffer getDataBuffer() {
        return this.requestBuffer;
    }

    /**
     * @return the response buffer
     */
    public ByteBuffer getResponseBuffer() {
        return this.responseBuffer;
    }

    /**
     *
     * @param buf the response buffer
     */
    public void setResponseBuffer(final ByteBuffer buf) {
        this.responseBuffer = buf;
    }

    /**
     *
     * @return the token of this request
     */
    public short getToken() {
        return token;
    }

    /**
     *
     * @return the command identifier of this request
     */
    public P2PPCommand getId() {
        return commandId;
    }

    /**
     *
     * @return the size of this request content
     */
    public int getRequestDataSize() {
        return requestDataSize;
    }

    /**
     *
     * @return the current status of this request
     */
    public ServerRequestStatus getStatus() {
        return status;
    }

    /**
     *
     * @param reqStatus the new status of this request
     */
    public void setStatus(final ServerRequestStatus reqStatus) {
        this.status = reqStatus;
    }

}
