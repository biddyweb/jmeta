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
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.server.P2PPServer;
import org.meta.p2pp.server.P2PPServerClientContext;
import org.meta.p2pp.server.P2PPServerRequestContext;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class P2PPSearchMetaHandler extends P2PPSearchHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPSearchMetaHandler.class);

    protected Set<String> requestedMetaKeys;

    protected Map<Data, Queue<ByteBuffer>> mdKeys;

    protected Map<Data, Queue<ByteBuffer>> mdValues;

    /**
     *
     * @param p2ppServer the p2pp server
     */
    public P2PPSearchMetaHandler(final P2PPServer p2ppServer) {
        super(p2ppServer);
    }

    @Override
    public void run() {
        if (!this.parse()) {
            logger.debug("Failed to parse request.");
            this.request.setStatus(P2PPConstants.ServerRequestStatus.DISCARDED);
        } else {
            this.prepareResponse();
            this.buildResponse();
        }
        server.handlerComplete(clientContext, request);
    }

    @Override
    public void handle(final P2PPServerClientContext clientCtx, final P2PPServerRequestContext req) {
        this.clientContext = clientCtx;
        this.request = req;
    }

    /**
     *
     * @param buf the buffer from which to extract meta-data
     */
    protected void extractMetaKeys(final ByteBuffer buf) {
        this.requestedMetaKeys = new HashSet<>();
        short metaDataNumber = buf.getShort();
        logger.debug("Search meta request handler: metaKeysNumber = " + metaDataNumber);
        short sizeofMetaKey;
        String metaKey;
        ByteBuffer tmp;
        while (metaDataNumber-- != 0) {
            sizeofMetaKey = buf.getShort();
            tmp = buf.asReadOnlyBuffer();
            tmp.limit(buf.position() + sizeofMetaKey);
            metaKey = SerializationUtils.decodeUTF8(tmp);
            buf.position(tmp.limit());
            logger.debug("Requested meta key = " + metaKey);
            this.requestedMetaKeys.add(metaKey);
        }
    }

    @Override
    protected boolean parse() {
        ByteBuffer buf = request.getDataBuffer();
        buf.rewind();
        extractMetaKeys(buf);
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
     *
     * @param data the data
     * @return the size in bytes used to store requested meta data for the given data
     */
    protected int prepareMetaDataResponse(final Data data) {
        int usedSize = Short.BYTES;

        if (data.getMetaDataMap().isEmpty() || this.requestedMetaKeys.isEmpty()) {
            return usedSize;
        }
        ByteBuffer tmpBuffer;
        for (String key : this.requestedMetaKeys) {
            MetaData md = data.getMetaData(key);
            if (md != null) {
                logger.debug("Match found for meta-data key:" + key);
                usedSize += (Short.BYTES * 2);
                tmpBuffer = SerializationUtils.encodeUTF8(key);
                if (this.mdKeys.get(data) == null) {
                    this.mdKeys.put(data, new LinkedList<>());
                }
                this.mdKeys.get(data).add(tmpBuffer);
                usedSize += tmpBuffer.limit();
                tmpBuffer = SerializationUtils.encodeUTF8(md.getValue());
                if (this.mdValues.get(data) == null) {
                    this.mdValues.put(data, new LinkedList<>());
                }
                this.mdValues.get(data).add(tmpBuffer);
                usedSize += tmpBuffer.limit();
            }
        }
        return usedSize;
    }

    /**
     * Put the requested meta-data for the given data to the response buffer.
     *
     * @param data the data
     */
    protected void putMetaData(final Data data) {
        Queue<ByteBuffer> metaKeysBuffers = this.mdKeys.get(data);
        int size = 0;
        if (metaKeysBuffers != null) {
            size = metaKeysBuffers.size();
        }
        logger.debug("put matched meta-data size = " + size);
        responseBuffer.putShort((short) size);
        Queue<ByteBuffer> mdValuesBuffers = null;
        if (size > 0) {
            mdValuesBuffers = this.mdValues.get(data);
        }
        ByteBuffer tmp;
        for (int i = 0; i < size; ++i) {
            tmp = metaKeysBuffers.poll();
            responseBuffer.putShort((short) tmp.limit());
            responseBuffer.put(tmp);
            tmp = mdValuesBuffers.poll();
            responseBuffer.putShort((short) tmp.limit());
            responseBuffer.put(tmp);
        }
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
        logger.debug("BUILD RESPONSE, ReSULTS = " + this.results.size());
        for (Data data : this.results) {
            responseSize += prepareDataTypeResponse(data);
            responseSize += prepareMetaDataResponse(data);
        }
        logger.debug("Response size = " + responseSize);
        responseBuffer = ByteBuffer.allocateDirect(P2PPConstants.RESPONSE_HEADER_SIZE + responseSize);
        responseBuffer.putShort((short) this.request.getToken());
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
        }
        this.responseBuffer.rewind();
        this.request.setResponseBuffer(responseBuffer);
        //this.request.setStatus(P2PPConstants.ServerRequestStatus.RESPONSE_READY);
    }

}
