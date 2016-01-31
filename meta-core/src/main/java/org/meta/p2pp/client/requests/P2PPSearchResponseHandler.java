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
import java.util.HashSet;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.DataType;
import org.meta.p2pp.client.P2PPRequest;
import org.meta.p2pp.client.P2PPResponseHandler;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The response handler for the {@link P2PPConstants.P2PPCommand.SEARCH} command.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class P2PPSearchResponseHandler extends P2PPResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPSearchResponseHandler.class);

    /**
     * The final results once extracted.
     */
    protected Set<Data> results;

    /**
     * <p>Constructor for P2PPSearchResponseHandler.</p>
     *
     * @param req the request
     */
    public P2PPSearchResponseHandler(final P2PPRequest req) {
        super(req);
    }

    /**
     * Extracts the data type from the given buffer.
     *
     * @param buf the buffer
     * @param sizeofDataType the size of the data to extract from the buffer
     * @return the extracted data type
     */
    protected DataType getDataType(final ByteBuffer buf, final short sizeofDataType) {
        ByteBuffer roBuffer = buf.asReadOnlyBuffer();

        roBuffer.limit(buf.position() + sizeofDataType);
        DataType type = new DataType(SerializationUtils.decodeUTF8(roBuffer));
        buf.position(roBuffer.position());
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public boolean parse(final ByteBuffer buf) {
        buf.rewind();
        int nbResults = buf.getInt();
        this.results = new HashSet<>(nbResults);
        if (nbResults == 0) {
            return true;
        }
        short sizeofDataType;
        DataType dataType;
        int dataSize;
        short sizeofHash;
        MetHash hash;
        Data dataResult;

        for (int i = 0; i < nbResults; ++i) {
            sizeofDataType = buf.getShort();
            dataType = getDataType(buf, sizeofDataType);
            dataSize = buf.getInt();
            sizeofHash = buf.getShort();
            hash = new MetHash(buf, sizeofHash);
            dataResult = this.request.getClient().getModelFactory().getData();
            dataResult.setType(dataType);
            dataResult.setHash(hash);
            dataResult.setSize(dataSize);
            this.results.add(dataResult);
        }
        return true;
    }

    /**
     * <p>Getter for the field <code>results</code>.</p>
     *
     * @return the extracted results, if any, otherwise null
     */
    public Set<Data> getResults() {
        return this.results;
    }

}
