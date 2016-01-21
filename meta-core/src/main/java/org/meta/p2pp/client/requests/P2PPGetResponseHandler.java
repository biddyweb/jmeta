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

import java.nio.ByteBuffer;
import org.meta.api.common.MetHash;
import org.meta.p2pp.client.P2PPRequest;
import org.meta.p2pp.client.P2PPResponseHandler;

/**
 *
 * The response handler for the Get request.
 *
 * @author dyslesiq
 */
public class P2PPGetResponseHandler extends P2PPResponseHandler {

    private MetHash pieceHash;

    private ByteBuffer data;

    private final int requestedLength;

    /**
     *
     * @param req the request
     * @param dataLength the request data length
     */
    public P2PPGetResponseHandler(final P2PPRequest req, final int dataLength) {
        super(req);
        this.requestedLength = dataLength;
    }

    @Override
    public boolean parse() {
        ByteBuffer buf = this.payloadBuffer;
        buf.rewind();
        short sizeofHash = buf.getShort();
        pieceHash = new MetHash(buf, sizeofHash);
        if (payloadBuffer.remaining() != requestedLength) {
            return false;
        }
        this.data = payloadBuffer.asReadOnlyBuffer();
        return true;
    }

    /**
     *
     * @return the fetched data
     */
    public ByteBuffer getData() {
        return data;
    }

    /**
     *
     * @return the piece hash
     */
    public MetHash getPieceHash() {
        return pieceHash;
    }

}
