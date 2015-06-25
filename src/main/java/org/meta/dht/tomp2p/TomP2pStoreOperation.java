/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 JMeta
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.dht.tomp2p;

import java.net.InetAddress;
import net.tomp2p.dht.AddBuilder;
import net.tomp2p.dht.FuturePut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerSocketAddress;
import net.tomp2p.storage.Data;
import org.meta.dht.StoreOperation;

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
     * Create the bootstrap operation with given arguments.
     *
     * @param dht The dht.
     * @param hash The hash to store in the DHT.
     */
    public TomP2pStoreOperation(TomP2pDHT dht, Number160 hash) {
        this.dht = dht;
        this.hash = hash;
    }

    /**
     *
     * Serialize a ip/port couple into a byte array.
     * 
     * @param port The udp port
     * @param addr The ipv4/ipv6 address
     * 
     * @return the serialized ip/port couple
     */
    private byte[] serializeAddress(Short port, InetAddress addr) {
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
        PeerSocketAddress peerAddr = this.dht.getPeerDHT().peerAddress().peerSocketAddress();

        logger.debug("StoreOperation: pushing peer addr = ", peerAddr.inetAddress().toString() + ":" + peerAddr.udpPort());
        byte[] data = serializeAddress((short) peerAddr.udpPort(), peerAddr.inetAddress());
        AddBuilder addBuilder = new AddBuilder(this.dht.getPeerDHT(), hash);

        addBuilder.data(new Data(data)).start().addListener(new BaseFutureListener<FuturePut>() {

            @Override
            public void operationComplete(FuturePut future) throws Exception {
                if (future.isSuccess() || future.isSuccessPartially()) {
                    logger.debug("Store operation complete!!!");
                    TomP2pStoreOperation.this.setState(OperationState.COMPLETE);
                } else {
                    logger.debug("Store operation failed!!!");
                    TomP2pStoreOperation.this.setState(OperationState.FAILED);
                }
                TomP2pStoreOperation.this.finish();
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
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
