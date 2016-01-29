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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a request state in an existing client context.
 *
 * It is responsible of the buffers needed to read the request and write the response back to the client.
 *
 * Initial parsing of the request header is also done before dispatching to the actual handler based on the
 * request command identifier.
 */
public class P2PPServerRequestContext {

    private Logger logger = LoggerFactory.getLogger(P2PPServerRequestContext.class);

    private volatile ServerRequestStatus status;

    /**
     * The unique request token, as defined in the protocol.
     */
    private char token;

    /**
     * The command identifier, as defined in the protocol.
     */
    private P2PPCommand commandId;

    /**
     * The size of the request content, as defined in the protocol.
     */
    private int requestDataSize;

    /**
     * The request data (read).
     */
    private ByteBuffer requestBuffer;

    /**
     * The response buffer.
     */
    private ByteBuffer responseBuffer;

    /**
     *
     */
    public P2PPServerRequestContext() {
        this.status = ServerRequestStatus.CREATED;
    }

    /**
     *
     */
    public void close() {
        if (this.requestBuffer != null) {
            BufferManager.release(requestBuffer);
        }
        if (this.responseBuffer != null) {
            BufferManager.release(responseBuffer);
        }
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
     * @param headerBuffer the header buffer
     * @return true if the parsed header is valid, false otherwise
     */
    public boolean parseRequestHeader(final ByteBuffer headerBuffer) {
        try {
            headerBuffer.rewind();
            this.token = (char) headerBuffer.getShort();
            byte id = headerBuffer.get();
            this.commandId = P2PPCommand.fromValue(id);
            this.requestDataSize = headerBuffer.getInt();
            if (!this.checkHeader()) {
                return false;
            }
            this.allocateDataBuffer();
            return true;
        } catch (final BufferUnderflowException e) {
            logger.error("BufferUnderflowException!!", e);
            return false;
        }
    }

    /**
     * Header is valid, prepare buffer for request content retrieval.
     */
    public void allocateDataBuffer() {
        if (requestDataSize > 0) {
            this.requestBuffer = BufferManager.aquireDirectBuffer(requestDataSize);
        }
    }

    /**
     *
     * @return true if the request has a data payload (request content), false otherwise
     */
    public boolean hasPayload() {
        return this.requestDataSize > 0;
    }

    /**
     * @return true if the request has an associated response ready to be sent, false otherwise
     */
    public boolean hasResponse() {
        return this.responseBuffer != null;
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
    public char getToken() {
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
