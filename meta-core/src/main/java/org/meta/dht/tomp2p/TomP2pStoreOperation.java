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
import java.util.NavigableMap;
import java.util.TreeMap;
import net.tomp2p.dht.AddBuilder;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerSocketAddress;
import net.tomp2p.storage.Data;
import org.meta.api.dht.StoreOperation;
import org.meta.configuration.MetaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomp2p implementation of the store operation.
 *
 * @author nico
 */
public class TomP2pStoreOperation extends StoreOperation {

    private final TomP2pDHT dht;
    private final Number160 hash;
    private static final Logger logger = LoggerFactory.getLogger(TomP2pStoreOperation.class);

    /**
     * Create the store operation with given arguments.
     *
     * @param dhtNode The dht node.
     * @param storeHash The hash to store in the DHT.
     */
    public TomP2pStoreOperation(final TomP2pDHT dhtNode, final Number160 storeHash) {
        this.dht = dhtNode;
        this.hash = storeHash;
    }

    /**
     * Serialize an ip/port couple into a byte array.
     *
     * @param port The udp port
     * @param addr The ipv4/ipv6 address
     *
     * @return the serialized ip/port couple
     */
    private byte[] serializeAddress(final Short port, final InetAddress addr) {
        byte[] addrBytes = addr.getAddress();
        short dataSize = (short) (2 + addrBytes.length);
        byte[] data = new byte[dataSize];

        data[0] = (byte) (port & 0x00ff);
        data[1] = (byte) ((port >> 8) & 0x00ff);
        for (short i = 2; i < dataSize; ++i) {
            data[i] = addrBytes[i - 2];
        }
        return data;
    }

    @Override
    public void start() {
        PeerSocketAddress peerAddr = this.dht.getPeer().peerAddress().peerSocketAddress();
        Short port = MetaConfiguration.getAmpConfiguration().getAmpPort();
        byte[] data = serializeAddress(port, peerAddr.inetAddress());

        NavigableMap<Number160, Data> dataMap = new TreeMap<>();
        dataMap.put(hash, new Data(data));

        AddBuilder addBuilder = new AddBuilder(this.dht.getPeerDHT(), hash);
        Data toAdd = new Data(data);
        toAdd.ttlSeconds(100);
        logger.debug("Data expiration timestamp = " + toAdd.expirationMillis());
        addBuilder.data(toAdd).start().addListener(new BaseFutureListener<FuturePut>() {

            @Override
            public void operationComplete(final FuturePut future) throws Exception {
                if (future.isSuccess() || future.isSuccessPartially()) {
                    logger.debug("Store operation complete.");
                    TomP2pStoreOperation.this.setState(OperationState.COMPLETE);
                } else {
                    logger.debug("Store operation failed.");
                    TomP2pStoreOperation.this.setState(OperationState.FAILED);
                }
                TomP2pStoreOperation.this.finish();
            }

            @Override
            public void exceptionCaught(final Throwable t) throws Exception {
                logger.error("Exception caught in store operation!", t);
                TomP2pStoreOperation.this.setFailed(t.getMessage());
            }
        });
    }

    @Override
    public void finish() {
        this.notifyListeners();
    }
}
