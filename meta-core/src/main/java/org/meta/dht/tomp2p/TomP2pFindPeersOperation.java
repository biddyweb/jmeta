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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.meta.api.dht.FindPeersOperation;
import org.meta.api.common.MetaPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomp2p implementation of the find peers for hash operation.
 *
 * @author nico
 */
public class TomP2pFindPeersOperation extends FindPeersOperation {

    private static final Logger logger = LoggerFactory.getLogger(TomP2pFindPeersOperation.class);

    private final TomP2pDHT dht;
    private final Number160 hash;

    /**
     *
     * @param dhtNode the dht node.
     * @param queryHash the hash to find peers for
     */
    public TomP2pFindPeersOperation(final TomP2pDHT dhtNode, final Number160 queryHash) {
        this.dht = dhtNode;
        this.hash = queryHash;
        this.peers = new ArrayList<>();
    }

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
                        logger.debug("No peers found for hash: ", TomP2pFindPeersOperation.this.hash);
                    } else {
                        for (Data data : datas) {
                            long timestamp = System.currentTimeMillis();
                            if (data.expirationMillis() < timestamp) {
                                logger.debug("EXPIRED DATA RECEIVED!");
                            }
                            MetaPeer newPeer = peerFromData(data.toBytes());
                            if (newPeer != null) {
                                logger.debug("Peer found! : " + newPeer);
                                TomP2pFindPeersOperation.this.peers.add(newPeer);
                            }
                        }
                    }
                    TomP2pFindPeersOperation.this.setState(OperationState.COMPLETE);
                    TomP2pFindPeersOperation.this.finish();
                }
            }

            @Override
            public void exceptionCaught(final Throwable t) throws Exception {
                logger.debug("Tomp2p find peers operation failed", t);
                TomP2pFindPeersOperation.this.setFailed(t);
            }
        });
    }

    /**
     * De-serialize the Ip/port couple from the given data into a {@link  MetaPeer}.
     *
     * @param data The serialized ip:port couple.
     *
     * //TODO move addr part to utility class
     * @return the created peer or null if invalid data.
     */
    private MetaPeer peerFromData(final byte[] data) {
        MetaPeer peer = new MetaPeer();
        short addrSize = (short) (data.length - 2);
        byte[] addrBytes = new byte[addrSize];
        short port = (short) (((data[1] & 0xFF) << 8) | (data[0] & 0xFF));

        //peer.setPort(port);
        for (int i = 0; i < addrSize; ++i) {
            addrBytes[i] = data[i + 2];
        }
        try {
            InetAddress inetAddr = InetAddress.getByAddress(addrBytes);
            if (inetAddr == null) {
                logger.error("Failed to create inet address from data.");
                return null;
            }
            peer.setAddress(new InetSocketAddress(inetAddr, port));
        } catch (UnknownHostException ex) {
            logger.error("Failed to create inet address from data.", ex);
            return null;
        }
        return peer;
    }

    @Override
    public void finish() {
        this.notifyListeners();
    }

}
