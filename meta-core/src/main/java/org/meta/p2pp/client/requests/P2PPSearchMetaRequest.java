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
import java.util.Map;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.plugin.SearchOperation;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.P2PPCommand;
import org.meta.p2pp.client.P2PPClient;
import org.meta.p2pp.client.P2PPRequest;
import org.meta.utils.SerializationUtils;

/**
 *
 * @author dyslesiq
 */
public class P2PPSearchMetaRequest extends P2PPRequest {

    private final MetHash[] requestedHashes;

    private final Set<String> keys;

    private P2PPSearchResponseHandler responseHandler;

    private SearchOperation operation;

    private Map<String, String> metaDataFilters;

    /**
     * Default constructor.
     *
     * @param p2ppClient the peer-to-peer protocol client
     * @param metaDataFilters 
     * @param metaDataKeys the meta data keys to get for each results. Can be null or empty.
     * @param hashes the hashes to search for
     */
    public P2PPSearchMetaRequest(final P2PPClient p2ppClient, final Map<String, String> metaDataFilters, final Set<String> metaDataKeys,
            final MetHash... hashes) {
        super(P2PPCommand.SEARCH_META, p2ppClient);
        this.metaDataFilters = metaDataFilters;
        this.keys = metaDataKeys;
        this.requestedHashes = hashes;
        this.responseHandler = new P2PPSearchMetaResponseHandler(this);
        this.operation = new SearchOperation();
    }

    @Override
    public P2PPSearchResponseHandler getResponseHandler() {
        return this.responseHandler;
    }

    @Override
    public boolean build(final char requestToken) {
        this.token = requestToken;
        int requestSize = P2PPConstants.REQUEST_HEADER_SIZE + Short.BYTES + Short.BYTES
                + (requestedHashes.length * (MetHash.BYTE_ARRAY_SIZE + Short.BYTES)+ Short.BYTES);

        int nbKeys = this.keys != null ? this.keys.size() : 0;
        ByteBuffer[] keyBuffers = null;
        if (nbKeys > 0) {
            keyBuffers = new ByteBuffer[nbKeys];
            int i = 0;
            for (String key : this.keys) {
                keyBuffers[i] = SerializationUtils.encodeUTF8(key);
                requestSize += (Short.BYTES + keyBuffers[i].limit());
                ++i;
            }
        }
        
        int nbFilters = this.metaDataFilters != null ? this.metaDataFilters.size() : 0;
        ByteBuffer[] filters = null;
        if (nbFilters > 0) {
            filters = new ByteBuffer[nbFilters*2];
            int i = 0;
            for (String filter : this.metaDataFilters.keySet()) {
                filters[i]   = SerializationUtils.encodeUTF8(filter);
                filters[i+1] = SerializationUtils.encodeUTF8(this.metaDataFilters.get(filter));
                requestSize += (Short.BYTES + filters[i].limit());
                requestSize += (Short.BYTES + filters[i+1].limit());
                i+=2;
            }
        }
        
        if (requestSize > P2PPConstants.MAX_REQUEST_DATA_SIZE) {
            return false;
        }
        this.buffer = ByteBuffer.allocateDirect(requestSize);
        //Header
        this.buffer.putShort((short) token);
        this.buffer.put(this.commandId.getValue());
        this.buffer.putInt(requestSize - P2PPConstants.REQUEST_HEADER_SIZE);
        //Meta-Data Filters
        this.buffer.putShort((short) (nbFilters));
        for (int i = 0; i < nbFilters*2; ++i) {
            this.buffer.putShort((short) filters[i].limit());
            this.buffer.put(filters[i]);
        }
        //Meta-Data Keys
        this.buffer.putShort((short) nbKeys);
        for (int i = 0; i < nbKeys; ++i) {
            this.buffer.putShort((short) keyBuffers[i].limit());
            this.buffer.put(keyBuffers[i]);
        }
        //Hashes
        this.buffer.putShort((short) this.requestedHashes.length);
        for (MetHash hash : requestedHashes) {
            this.buffer.putShort((short) MetHash.BYTE_ARRAY_SIZE);
            this.buffer.put(hash.toByteArray());
        }
        this.buffer.rewind();
        return true;
    }

    @Override
    public void finish() {
        if (!this.responseHandler.parse()) {
            this.operation.setFailed("Failed to parse response");
        } else {
            this.operation.setResults(this.responseHandler.getResults());
            this.operation.complete();
        }
        BufferManager.release(buffer);
        BufferManager.release(this.responseHandler.getPayloadBuffer());
        this.operation = null;
        this.responseHandler = null;
    }

    @Override
    public void setFailed(final String failedReason) {
        if (this.buffer != null) {
            BufferManager.release(buffer);
        }
        if (this.responseHandler.getPayloadBuffer() != null) {
            BufferManager.release(this.responseHandler.getPayloadBuffer());
        }
        this.operation.setFailed(failedReason);
    }

    @Override
    public void setFailed(final Throwable thrwbl) {
        if (this.buffer != null) {
            BufferManager.release(buffer);
        }
        if (this.responseHandler.getPayloadBuffer() != null) {
            BufferManager.release(this.responseHandler.getPayloadBuffer());
        }
        this.operation.setFailed(thrwbl);
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public SearchOperation getOperation() {
        return this.operation;
    }

}
