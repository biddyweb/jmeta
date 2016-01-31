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
import org.slf4j.LoggerFactory;

/**
 *
 * The response handler for the Get request.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class P2PPGetResponseHandler extends P2PPResponseHandler {

    private MetHash pieceHash;

    private ByteBuffer data;

    private final int requestedLength;

    /**
     * <p>Constructor for P2PPGetResponseHandler.</p>
     *
     * @param req the request
     * @param dataLength the request data length
     */
    public P2PPGetResponseHandler(final P2PPRequest req, final int dataLength) {
        super(req);
        this.requestedLength = dataLength;
    }

    /** {@inheritDoc} */
    @Override
    public boolean parse(final ByteBuffer buf) {
        buf.rewind();
        short sizeofHash = buf.getShort();
        pieceHash = new MetHash(buf, sizeofHash);
        if (buf.remaining() != requestedLength) {
            LoggerFactory.getLogger(P2PPGetResponseHandler.class).error("P2PPGetResponseHandler: buf.remaining() != requestedLength. " + buf.remaining() + " != " + requestedLength);
            return false;
        }
        this.data = buf.asReadOnlyBuffer();
        return true;
    }

    /**
     * <p>Getter for the field <code>data</code>.</p>
     *
     * @return the fetched data
     */
    public ByteBuffer getData() {
        return data;
    }

    /**
     * <p>Getter for the field <code>pieceHash</code>.</p>
     *
     * @return the piece hash
     */
    public MetHash getPieceHash() {
        return pieceHash;
    }

}
