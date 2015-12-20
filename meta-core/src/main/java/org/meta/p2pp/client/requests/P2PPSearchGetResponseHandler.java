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
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.DataType;
import org.meta.api.model.MetaDataMap;
import org.meta.p2pp.client.P2PPRequest;

/**
 * The response handler for the {@link P2PPConstants.P2PPCommand.SEARCH_GET} command.
 *
 * @author dyslesiq
 */
public class P2PPSearchGetResponseHandler extends P2PPSearchMetaResponseHandler {

    /**
     *
     * @param req the request
     */
    public P2PPSearchGetResponseHandler(final P2PPRequest req) {
        super(req);
    }

    /**
     * Extract the data of the given size from the given buffer.
     *
     * @param buf the buffer
     * @param sizeofData the data size
     * @return the extracted ByteBuffer
     */
    protected ByteBuffer getDataContent(final ByteBuffer buf, final int sizeofData) {
        byte[] data = new byte[sizeofData];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        buf.get(data);
        return dataBuffer;
    }

    @Override
    public boolean parse() {
        ByteBuffer buf = this.payloadBuffer;
        buf.rewind();
        int nbResults = buf.getInt();
        if (nbResults == 0) {
            return true;
        }
        this.results = new HashSet<>(nbResults);
        MetaDataMap metaData = null;
        short sizeofDataType;
        DataType dataType;
        int dataSize;
        short sizeofHash;
        MetHash hash;
        ByteBuffer dataContent;
        Data dataResult;
        for (int i = 0; i < nbResults; ++i) {
            metaData = this.extractMetaData(buf);
            sizeofDataType = buf.getShort();
            dataType = this.getDataType(buf, sizeofDataType);
            dataSize = buf.getInt();
            sizeofHash = buf.getShort();
            hash = new MetHash(buf, sizeofHash);
            dataContent = this.getDataContent(buf, dataSize);
            dataResult = this.request.getClient().getModelFactory().getData(dataContent);
            dataResult.setType(dataType);
            dataResult.setHash(hash);
            dataResult.setSize(dataSize);
            dataResult.setMetaData(metaData);
            this.results.add(dataResult);
        }
        return true;
    }
}
