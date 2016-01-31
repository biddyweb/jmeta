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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.Search;
import org.meta.model.ModelUtils;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.server.P2PPCommandHandler;
import org.meta.p2pp.server.P2PPServer;
import org.meta.p2pp.server.P2PPServerClientContext;
import org.meta.p2pp.server.P2PPServerRequestContext;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>P2PPSearchHandler class.</p>
 *
 * @author dyslesiq
 * @version $Id: $
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
     */
    protected HashMap<String, String> metaDataFilters;

    /**
     * <p>Constructor for P2PPSearchHandler.</p>
     *
     * @param p2ppServer the p2pp server
     */
    public P2PPSearchHandler(final P2PPServer p2ppServer) {
        super(p2ppServer);
        this.metaDataFilters = new HashMap<String, String>();
    }

    /** {@inheritDoc} */
    @Override
    public void handle(final P2PPServerRequestContext req) {
        this.request = req;
        if (!this.parse()) {
            logger.debug("Failed to parse request.");
            this.request.setStatus(P2PPConstants.ServerRequestStatus.DISCARDED);
        } else {
            this.prepareResponse();
            this.buildResponse();
        }
    }

    /**
     * <p>parse</p>
     *
     * @return true on success, false otherwise
     */
    protected boolean parse() {
        ByteBuffer buf = request.getDataBuffer();
        buf.rewind();
        //filters
        extractMetaDataFilters(buf);
        //Hash
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
     * <p>extractMetaDataFilters</p>
     *
     * @param buf a {@link java.nio.ByteBuffer} object.
     */
    protected void extractMetaDataFilters(final ByteBuffer buf) {
        short nbFilters = buf.getShort();
        logger.debug("Search request handler: nb Filters = " + nbFilters);
        for (int i = 0; i < nbFilters; i++) {
            short keySize = buf.getShort();
            ByteBuffer tmp = buf.asReadOnlyBuffer();
            // avoid memory duplication using read only buffer
            tmp.limit(buf.position() + keySize);
            buf.position(tmp.limit());
            String keyValue = SerializationUtils.decodeUTF8(tmp);
            logger.debug("Received key: " + keyValue.toString());
            short valueSize = buf.getShort();
            tmp = buf.asReadOnlyBuffer();
            // avoid memory duplication using read only buffer
            tmp.limit(buf.position() + valueSize);
            buf.position(tmp.limit());
            String valueValue = SerializationUtils.decodeUTF8(tmp);
            logger.debug("Received value: " + valueValue.toString());
            metaDataFilters.put(keyValue, valueValue);
        }
    }

    /**
     * Retrieves search results from model.
     */
    protected void prepareResponse() {
        this.results = new HashSet<>();
        Search tmpSearch;
        for (MetHash hash : this.requestedHashes) {
            tmpSearch = this.server.getStorage().getSearch(hash);
            if (tmpSearch != null) {
                for (Data data : tmpSearch.getResults()) {
                    //Once a search is found, apply the filters on, each
                    //results
                    if (ModelUtils.matchDataMetaData(data, metaDataFilters)) {
                        this.results.add(data);
                    }
                }
            }
        }
    }

    /**
     * <p>prepareDataTypeResponse</p>
     *
     * @param data the data
     * @return the size in bytes to store the data type
     */
    protected int prepareDataTypeResponse(final Data data) {
        ByteBuffer tmp = SerializationUtils.encodeUTF8(data.getType().toString());
        dataTypes.add(tmp);
        logger.debug("Build response, data type = " + data.getType().toString());
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
        responseBuffer = BufferManager.aquireDirectBuffer(P2PPConstants.RESPONSE_HEADER_SIZE + responseSize);
        responseBuffer.putShort((short) this.request.getToken());
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
        //this.request.setStatus(P2PPConstants.ServerRequestStatus.RESPONSE_READY);
    }

}
