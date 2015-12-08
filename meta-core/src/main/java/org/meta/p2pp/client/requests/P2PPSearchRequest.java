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
package org.meta.p2pp.client.requests;

import java.nio.BufferOverflowException;
import org.meta.api.common.MetHash;
import org.meta.api.plugin.SearchOperation;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.P2PPCommand;
import org.meta.p2pp.client.P2PPClient;
import org.meta.p2pp.client.P2PPRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class P2PPSearchRequest extends P2PPRequest {

    private final Logger logger = LoggerFactory.getLogger(P2PPSearchRequest.class);

    private final MetHash[] requestedHashes;

    private P2PPSearchResponseHandler responseHandler;

    private SearchOperation operation;

    /**
     * Default constructor.
     *
     * @param p2ppClient the peer-to-peer protocol client
     * @param hashes the hashes to search for
     */
    public P2PPSearchRequest(final P2PPClient p2ppClient, final MetHash... hashes) {
        super(P2PPCommand.SEARCH, p2ppClient);
        this.requestedHashes = hashes;
        this.responseHandler = new P2PPSearchResponseHandler(this);
        this.operation = new SearchOperation();
    }

    @Override
    public P2PPSearchResponseHandler getResponseHandler() {
        return this.responseHandler;
    }

    @Override
    public boolean build(final char requestToken) {
        this.token = requestToken;
        int requestSize = P2PPConstants.REQUEST_HEADER_SIZE + Short.BYTES
                + (requestedHashes.length * (Short.BYTES + MetHash.BYTE_ARRAY_SIZE));

        logger.debug("Request size = " + requestSize);
        if (requestSize > P2PPConstants.MAX_REQUEST_DATA_SIZE) {
            return false;
        }
        this.buffer = BufferManager.aquireDirectBuffer(requestSize);
        try {
            this.buffer.putShort((short) token);
            this.buffer.put(this.commandId.getValue());
            this.buffer.putInt(requestSize - P2PPConstants.REQUEST_HEADER_SIZE);
            this.buffer.putShort((short) this.requestedHashes.length);
            for (MetHash hash : requestedHashes) {
                this.buffer.putShort((short) MetHash.BYTE_ARRAY_SIZE);
                this.buffer.put(hash.toByteArray());
            }
        } catch (BufferOverflowException ex) {
            logger.debug("buffer error while building request", ex);
            return false;
        }
        this.buffer.rewind();
        return true;
    }

    @Override
    public void finish() {
        if (!this.responseHandler.parse()) {
            this.operation.setFailed("Failed to parse response");
        } else {
            this.operation.setResults(this.responseHandler.getResults());
            this.operation.complete();
        }
        BufferManager.release(buffer);
        BufferManager.release(this.responseHandler.getPayloadBuffer());
        this.operation = null;
        this.responseHandler = null;
    }

    @Override
    public void setFailed(final String failedReason) {
        if (this.buffer != null) {
            BufferManager.release(buffer);
        }
        if (this.responseHandler.getPayloadBuffer() != null) {
            BufferManager.release(this.responseHandler.getPayloadBuffer());
        }
        this.operation.setFailed(failedReason);
    }

    @Override
    public void setFailed(final Throwable thrwbl) {
        if (this.buffer != null) {
            BufferManager.release(buffer);
        }
        if (this.responseHandler.getPayloadBuffer() != null) {
            BufferManager.release(this.responseHandler.getPayloadBuffer());
        }
        this.operation.setFailed(thrwbl);
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public SearchOperation getOperation() {
        return this.operation;
    }

}
