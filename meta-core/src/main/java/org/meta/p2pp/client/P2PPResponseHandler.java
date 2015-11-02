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

import java.nio.ByteBuffer;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ClientRequestStatus;

/**
 * Response handler (client-side) for a peer-to-peer protocol request.
 *
 * @author dyslesiq
 */
public abstract class P2PPResponseHandler {

    /**
     * The request that created this response handler.
     */
    protected final P2PPRequest request;

    /**
     * The payload buffer.
     */
    protected ByteBuffer payloadBuffer;

    /**
     * The payload size, as received in the response header.
     */
    protected int payloadSize;

    /**
     * Default constructor for a response handler, with associated request.
     *
     * @param req the request
     */
    public P2PPResponseHandler(final P2PPRequest req) {
        this.request = req;
        this.payloadSize = 0;
    }

    /**
     * Extract the response header from the given buffer.
     *
     * @param headerBuffer the buffer containing header
     * @return true is successfully parsed and valid, false otherwise
     */
    protected boolean parseHeader(final ByteBuffer headerBuffer) {
        headerBuffer.rewind();
        short responseToken = headerBuffer.getShort();
        if (responseToken != this.request.getToken()) {
            return false;
        }
        //Unused for now. Should be the remaining frames to complete the response.
        headerBuffer.get();
        this.payloadSize = headerBuffer.getInt();
        if (this.payloadSize > P2PPConstants.MAX_RESPONSE_SIZE) {
            return false;
        }
        if (payloadSize > 0) {
            this.payloadBuffer = BufferManager.createDirectBuffer(payloadSize);
        }
        return true;
    }

    /**
     *
     * @return The request
     */
    public P2PPRequest getRequest() {
        return this.request;
    }

    /**
     *
     * @return the data buffer used to store the protocol response data read from the server
     */
    public final ByteBuffer getPayloadBuffer() {
        return this.payloadBuffer;
    }

    /**
     * Called by the async completion handler to notify us that data has been received.
     *
     * @return the status after update
     */
    public abstract ClientRequestStatus dataReceived();

    /**
     * Used to extract usable data from the protocol response data.
     *
     * @return true is successfully parsed, false otherwise
     */
    public abstract boolean parse();

}
