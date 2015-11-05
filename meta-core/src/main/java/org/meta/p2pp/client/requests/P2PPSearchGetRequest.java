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
public class P2PPSearchGetRequest extends P2PPRequest {

    private final MetHash[] requestedHashes;

    private final Set<String> keys;

    private final P2PPSearchGetResponseHandler responseHandler;

    private final SearchOperation operation;

    /**
     * Default constructor.
     *
     * @param p2ppClient the peer-to-peer protocol client
     * @param metaDataKeys the meta data keys to get for each results. Can be null or empty.
     * @param hashes the hashes to search for
     */
    public P2PPSearchGetRequest(final P2PPClient p2ppClient, final Set<String> metaDataKeys,
            final MetHash... hashes) {
        super(P2PPCommand.SEARCH_GET, p2ppClient);
        this.keys = metaDataKeys;
        this.requestedHashes = hashes;
        this.responseHandler = new P2PPSearchGetResponseHandler(this);
        this.operation = new SearchOperation();
    }

    @Override
    public P2PPSearchGetResponseHandler getResponseHandler() {
        return this.responseHandler;
    }

    @Override
    public boolean build(final short requestToken) {
        this.token = requestToken;
        int requestSize = P2PPConstants.REQUEST_HEADER_SIZE + Short.BYTES + Short.BYTES
                + (requestedHashes.length * (MetHash.BYTE_ARRAY_SIZE + Short.BYTES));

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
        if (requestSize > P2PPConstants.MAX_REQUEST_DATA_SIZE) {
            return false;
        }
        this.buffer = BufferManager.aquireDirectBuffer(requestSize);
        //Header
        this.buffer.putShort(token);
        this.buffer.put(this.commandId.getValue());
        this.buffer.putInt(requestSize - P2PPConstants.REQUEST_HEADER_SIZE);
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
            return;
        }
        this.operation.setResults(this.responseHandler.getResults());
        this.operation.complete();
    }

    @Override
    public void setFailed(final String failedReason) {
        this.operation.setFailed(failedReason);
    }

    @Override
    public void setFailed(final Throwable thrwbl) {
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
