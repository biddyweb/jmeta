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
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaDataMap;
import org.meta.p2pp.client.P2PPRequest;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The response handler for the {@link P2PPConstants.P2PPCommand.SEARCH_META} command.
 *
 * @author dyslesiq
 */
public class P2PPSearchMetaResponseHandler extends P2PPSearchResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPSearchMetaResponseHandler.class);

    /**
     *
     * @param req the request
     */
    public P2PPSearchMetaResponseHandler(final P2PPRequest req) {
        super(req);
    }

    @Override
    public boolean parse(final ByteBuffer buf) {
        buf.rewind();
        int nbResults = buf.getInt();
        this.results = new HashSet<>(nbResults);
        if (nbResults == 0) {
            return true;
        }
        MetaDataMap metaData = null;
        short sizeofDataType;
        DataType dataType;
        int dataSize;
        short sizeofHash;
        MetHash hash;
        Data dataResult;

        logger.debug("P2PPSearchMetaResponseHandler: received results nb = " + nbResults);
        for (int i = 0; i < nbResults; ++i) {
            metaData = this.extractMetaData(buf);
            sizeofDataType = buf.getShort();
            dataType = getDataType(buf, sizeofDataType);
            dataSize = buf.getInt();
            sizeofHash = buf.getShort();
            hash = new MetHash(buf, sizeofHash);
            dataResult = this.request.getClient().getModelFactory().getData();
            if (dataResult == null) {
                return false;
            }
            dataResult.setType(dataType);
            dataResult.setHash(hash);
            dataResult.setSize(dataSize);
            dataResult.setMetaData(metaData);
            this.results.add(dataResult);
        }
        return true;
    }

    /**
     * Extracts meta-data as defined in the protocol from the given buffer.
     *
     * @param buf data buffer from which to extract meta-datas
     * @return the Set of created meta-data. Might be empty.
     */
    protected MetaDataMap extractMetaData(final ByteBuffer buf) {
        short metaDataNumber = buf.getShort();
        logger.debug("meta-data number = " + metaDataNumber);
        MetaDataMap metaData = new MetaDataMap();

        if (metaDataNumber == 0) {
            return metaData;
        }
        short keySize;
        short valueSize;
        MetaData prop;
        ByteBuffer roBuffer = buf.asReadOnlyBuffer();
        for (int i = 0; i < metaDataNumber; ++i) {
            prop = new MetaData();
            keySize = buf.getShort();
            logger.debug("meta-data key size = " + keySize);
            roBuffer.limit(buf.position() + keySize);
            roBuffer.position(buf.position());
            prop.setKey(SerializationUtils.decodeUTF8(roBuffer));
            logger.debug("extracted meta-data key: " + prop.getKey());
            buf.position(roBuffer.limit());
            valueSize = buf.getShort();
            logger.debug("meta-data value size = " + valueSize);
            roBuffer.limit(buf.position() + valueSize);
            roBuffer.position(buf.position());
            prop.setValue(SerializationUtils.decodeUTF8(roBuffer));
            logger.debug("extracted meta-data value: " + prop.getValue());
            buf.position(roBuffer.limit());
            metaData.put(prop);
        }
        return metaData;
    }

}
