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
package org.meta.dht.tomp2p;

import java.util.ArrayList;
import java.util.Collection;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.meta.api.common.MetaPeer;
import org.meta.api.dht.FindPeersOperation;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomp2p implementation of the find peers for hash operation.
 *
 * @author nico
 * @version $Id: $
 */
public class TomP2pFindPeersOperation extends FindPeersOperation {

    private static final Logger logger = LoggerFactory.getLogger(TomP2pFindPeersOperation.class);

    private final TomP2pDHT dht;
    private final Number160 hash;

    /**
     * <p>Constructor for TomP2pFindPeersOperation.</p>
     *
     * @param dhtNode the dht node.
     * @param queryHash the hash to find peers for
     */
    public TomP2pFindPeersOperation(final TomP2pDHT dhtNode, final Number160 queryHash) {
        this.dht = dhtNode;
        this.hash = queryHash;
        this.peers = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        FutureGet futureGet = this.dht.getPeerDHT().get(this.hash).all().start();
        futureGet.addListener(new BaseFutureListener<FutureGet>() {

            @Override
            public void operationComplete(final FutureGet getOperation) throws Exception {
                if (getOperation.isFailed()) {
                    logger.debug("Failed to find peers from dht: " + getOperation.failedReason());
                    TomP2pFindPeersOperation.this.setFailed("Failed to find peers from dht: "
                            + getOperation.failedReason());
                } else if (getOperation.isSuccess()) {
                    Collection<Data> datas = getOperation.dataMap().values();
                    if (datas.isEmpty()) {
                        logger.debug("No peers found for hash: {}", hash);
                    } else {
                        for (Data data : datas) {
                            long timestamp = System.currentTimeMillis();
                            if (data.expirationMillis() < timestamp) {
                                logger.debug("TEST: EXPIRED DATA RECEIVED!");
                            }
                            MetaPeer newPeer = SerializationUtils.peerFromData(data.toBytes());
                            if (newPeer != null) {
                                logger.debug("Peer found! : " + newPeer);
                                TomP2pFindPeersOperation.this.peers.add(newPeer);
                            }
                        }
                    }
                    TomP2pFindPeersOperation.this.complete();
                }
            }

            @Override
            public void exceptionCaught(final Throwable t) throws Exception {
                logger.debug("Tomp2p find peers operation failed", t);
                TomP2pFindPeersOperation.this.setFailed(t);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void finish() {
    }

}
