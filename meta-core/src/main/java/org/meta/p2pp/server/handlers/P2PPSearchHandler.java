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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.ModelStorage;
import org.meta.api.model.Search;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ServerRequestStatus;
import org.meta.p2pp.server.P2PPCommandHandler;
import org.meta.p2pp.server.P2PPServerClientContext;
import org.meta.p2pp.server.P2PPServerRequestContext;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class P2PPSearchHandler extends P2PPCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPSearchHandler.class);

    /**
     *
     */
    protected P2PPServerClientContext clientContext;

    /**
     *
     */
    protected P2PPServerRequestContext request;

    /**
     *
     */
    protected Set<MetHash> requestedHashes;

    /**
     *
     */
    protected Set<Data> results;

    /**
     *
     */
    protected ByteBuffer responseBuffer;

    /**
     *
     */
    protected Queue<ByteBuffer> dataTypes;

    /**
     *
     * @param modelStorage the model storage
     */
    public P2PPSearchHandler(final ModelStorage modelStorage) {
        super(modelStorage);
    }

    @Override
    public void handle(final P2PPServerClientContext clientCtx, final P2PPServerRequestContext req) {
        this.request = req;
        if (!this.parse()) {
            logger.debug("Failed to parse request.");
            req.setStatus(ServerRequestStatus.DISCARDED);
        } else {
            this.prepareResponse();
            this.buildResponse();
        }
        clientCtx.handlerComplete(req);
    }

    protected boolean parse() {
        ByteBuffer buf = request.getDataBuffer();
        buf.rewind();
        short hashNumber = buf.getShort();
        logger.debug("Search request handler: hashNumber = " + hashNumber);
        this.requestedHashes = new HashSet<>((int) hashNumber);
        short tmpHashSize;
        MetHash tmpHash;
        while (hashNumber-- != 0) {
            tmpHashSize = buf.getShort();
            tmpHash = new MetHash(buf, tmpHashSize);
            logger.debug("Received hash: " + tmpHash.toString());
            requestedHashes.add(tmpHash);
        }
        return true;
    }

    /**
     * Retrieves search results from model.
     */
    protected void prepareResponse() {
        this.results = new HashSet<>();
        Search tmpSearch;
        for (MetHash hash : this.requestedHashes) {
            logger.debug("Getting search results from db...");
            tmpSearch = P2PPSearchHandler.this.storage.getSearch(hash);
            if (tmpSearch != null) {
                logger.debug("Adding response results: " + tmpSearch.getResults().size());
                this.results.addAll(tmpSearch.getResults());
            }
        }
    }

    /**
     *
     * @param data the data
     * @return the size in bytes to store the data type
     */
    protected int prepareDataTypeResponse(final Data data) {
        ByteBuffer tmp = SerializationUtils.encodeUTF8(data.getType().toString());
        dataTypes.add(tmp);
        logger.debug("Build response, add data type = " + data.getType().toString());
        return tmp.limit() + Short.BYTES;
    }

    /**
     * Build the response from results.
     */
    private void buildResponse() {
        this.dataTypes = new LinkedList<>();

        int responseSize = Integer.BYTES + (this.results.size()
                * (Short.BYTES + Short.BYTES + Integer.BYTES + MetHash.BYTE_ARRAY_SIZE));

        ByteBuffer tmp;
        for (Data data : this.results) {
            responseSize += prepareDataTypeResponse(data);
        }
        logger.debug("Response size = " + responseSize);
        responseBuffer = BufferManager.createDirectBuffer(P2PPConstants.RESPONSE_HEADER_SIZE + responseSize);
        responseBuffer.putShort(this.request.getToken());
        responseBuffer.put((byte) 0); //Remaining frames, unused for now
        responseBuffer.putInt(responseSize);
        responseBuffer.putInt(this.results.size());
        for (Data data : this.results) {
            tmp = dataTypes.poll();
            responseBuffer.putShort((short) tmp.limit());
            responseBuffer.put(tmp);
            responseBuffer.putInt(data.getSize());
            responseBuffer.putShort((short) MetHash.BYTE_ARRAY_SIZE);
            responseBuffer.put(data.getHash().toByteArray());
        }
        this.responseBuffer.rewind();
        this.request.setResponseBuffer(responseBuffer);
        this.request.handlerComplete();
    }

}
