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
package org.meta.p2pp.server.handlers;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.ModelStorage;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.server.P2PPServerClientContext;
import org.meta.p2pp.server.P2PPServerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class P2PPSearchGetHandler extends P2PPSearchMetaHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPSearchGetHandler.class);

    /**
     *
     * @param modelStorage the model storage
     */
    public P2PPSearchGetHandler(final ModelStorage modelStorage) {
        super(modelStorage);
    }

    @Override
    public void handle(final P2PPServerClientContext clientCtx, final P2PPServerRequestContext req) {
        this.request = req;
        logger.debug("Handling client request...");
        if (!this.parse()) {
            logger.debug("Failed to parse request.");
            req.setStatus(P2PPConstants.ServerRequestStatus.DISCARDED);
        } else {
            this.prepareResponse();
            this.buildResponse();
        }
        clientCtx.handlerComplete(req);
    }

    /**
     * Build the response from results.
     */
    private void buildResponse() {
        this.dataTypes = new LinkedList<>();
        this.mdKeys = new HashMap<>();
        this.mdValues = new HashMap<>();

        int responseSize = Integer.BYTES + (this.results.size()
                * (Short.BYTES + Short.BYTES + Integer.BYTES + MetHash.BYTE_ARRAY_SIZE));

        ByteBuffer tmp;
        for (Data data : this.results) {
            responseSize += prepareDataTypeResponse(data);
            responseSize += prepareMetaDataResponse(data);
            responseSize += data.getSize();
        }
        logger.debug("Response size = " + responseSize);
        responseBuffer = ByteBuffer.allocateDirect(P2PPConstants.RESPONSE_HEADER_SIZE + responseSize);
        responseBuffer.putShort(this.request.getToken());
        responseBuffer.put((byte) 0); //Remaining frames, unused for now
        responseBuffer.putInt(responseSize);
        responseBuffer.putInt(this.results.size());
        for (Data data : this.results) {
            putMetaData(data);
            tmp = dataTypes.poll();
            responseBuffer.putShort((short) tmp.limit());
            responseBuffer.put(tmp);
            responseBuffer.putInt(data.getSize());
            responseBuffer.putShort((short) MetHash.BYTE_ARRAY_SIZE);
            responseBuffer.put(data.getHash().toByteArray());
            responseBuffer.put(data.getBuffer());
        }
        this.responseBuffer.rewind();
        this.request.setResponseBuffer(responseBuffer);
        this.request.handlerComplete();
    }

}
