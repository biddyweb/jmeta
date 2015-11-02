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
package org.meta.p2pp.client;

import java.util.Set;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.p2pp.GetOperation;
import org.meta.api.p2pp.PluginP2PPClient;
import org.meta.api.plugin.SearchOperation;
import org.meta.p2pp.client.requests.P2PPGetRequest;
import org.meta.p2pp.client.requests.P2PPKeepAliveRequest;
import org.meta.p2pp.client.requests.P2PPSearchGetRequest;
import org.meta.p2pp.client.requests.P2PPSearchMetaRequest;
import org.meta.p2pp.client.requests.P2PPSearchRequest;

/**
 *
 * Implementation of the P2PP client commands accessor.
 *
 * @author dyslesiq
 */
public class MetaP2PPClient implements PluginP2PPClient {

    private final P2PPClient client;

    /**
     *
     * @param p2ppClient the peer-to-peer protocol client
     */
    public MetaP2PPClient(final P2PPClient p2ppClient) {
        this.client = p2ppClient;
    }

    @Override
    public AsyncOperation keepAlive(final MetaPeer peer) {
        P2PPKeepAliveRequest req = new P2PPKeepAliveRequest();

        this.client.submitRequest(peer, req);
        return req.getOperation();
    }

    @Override
    public SearchOperation search(final MetaPeer peer, final MetHash hash) {
        return search(peer, new MetHash[]{hash});
    }

    @Override
    public SearchOperation search(final MetaPeer peer, final MetHash... hashes) {
        P2PPSearchRequest req = new P2PPSearchRequest(client, hashes);

        this.client.submitRequest(peer, req);
        return req.getOperation();
    }

    @Override
    public SearchOperation searchMeta(final MetaPeer peer,
            final Set<String> metaDataKeys, final MetHash hash) {
        return searchMeta(peer, metaDataKeys, new MetHash[]{hash});
    }

    @Override
    public SearchOperation searchMeta(final MetaPeer peer,
            final Set<String> metaDataKeys, final MetHash... hashes) {
        P2PPSearchMetaRequest req = new P2PPSearchMetaRequest(client, metaDataKeys, hashes);

        this.client.submitRequest(peer, req);
        return req.getOperation();
    }

    @Override
    public SearchOperation searchGet(final MetaPeer peer,
            final Set<String> metaDataKeys, final MetHash hash) {
        P2PPSearchGetRequest req = new P2PPSearchGetRequest(client, metaDataKeys, new MetHash[]{hash});

        this.client.submitRequest(peer, req);
        return req.getOperation();
    }

    @Override
    public SearchOperation searchGet(final MetaPeer peer, final Set<String> metaDataKeys,
            final MetHash... hashes) {
        P2PPSearchGetRequest req = new P2PPSearchGetRequest(client, metaDataKeys, hashes);

        this.client.submitRequest(peer, req);
        return req.getOperation();
    }

    @Override
    public GetOperation get(final MetaPeer peer, final MetHash hash, final int pieceIndex,
            final int byteOffset, final int length) {
        P2PPGetRequest req = new P2PPGetRequest(client, hash, pieceIndex, byteOffset, length);

        this.client.submitRequest(peer, req);
        return req.getOperation();
    }

}
