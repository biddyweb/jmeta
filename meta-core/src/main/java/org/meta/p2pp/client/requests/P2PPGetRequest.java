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

import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.p2pp.GetOperation;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.P2PPCommand;
import org.meta.p2pp.client.P2PPClient;
import org.meta.p2pp.client.P2PPRequest;

/**
 * Client-side request for the Get command.
 *
 * @author dyslesiq
 */
public class P2PPGetRequest extends P2PPRequest {

    private P2PPGetResponseHandler responseHandler;

    private GetOperation operation;

    /**
     *
     * @param p2ppClient the peer-to-peer protocol client
     * @param hash the hash of the data to get
     * @param pieceIdx the piece index
     * @param offset the byte offset inside the piece
     * @param length the requested data length
     */
    public P2PPGetRequest(final P2PPClient p2ppClient, final MetHash hash, final int pieceIdx,
            final int offset, final int length, MetaPeer peer) {
        super(P2PPCommand.GET, p2ppClient, peer);
        this.operation = new GetOperation(hash, pieceIdx, offset, length);
        this.responseHandler = new P2PPGetResponseHandler(this, length);
    }

    @Override
    public P2PPGetResponseHandler getResponseHandler() {
        return this.responseHandler;
    }

    @Override
    public GetOperation getOperation() {
        return this.operation;
    }

    @Override
    public boolean build(final char requestToken) {
        this.token = requestToken;
        int requestSize = P2PPConstants.REQUEST_HEADER_SIZE + (3 * Integer.BYTES)
                + Short.BYTES + MetHash.BYTE_ARRAY_SIZE;

        if (requestSize > P2PPConstants.MAX_REQUEST_DATA_SIZE) {
            return false;
        }
        this.buffer = BufferManager.aquireDirectBuffer(requestSize);
        //Header
        this.buffer.putShort((short) token);
        this.buffer.put(this.commandId.getValue());
        this.buffer.putInt(requestSize - P2PPConstants.REQUEST_HEADER_SIZE);
        //Get req content
        this.buffer.putShort((short) MetHash.BYTE_ARRAY_SIZE);
        this.buffer.put(this.operation.getTargetDataHash().toByteArray());
        this.buffer.putInt(this.operation.getPieceIndex());
        this.buffer.putInt(this.operation.getByteOffset());
        this.buffer.putInt(this.operation.getDataLength());
        this.buffer.rewind();
        return true;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public void finish() {
        if (!this.responseHandler.parse()) {
            this.operation.setFailed("Failed to parse response");
        } else {
            this.operation.setPieceHash(this.responseHandler.getPieceHash());
            this.operation.setData(this.responseHandler.getData());
            this.operation.complete();
        }
        BufferManager.release(buffer);
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

}
