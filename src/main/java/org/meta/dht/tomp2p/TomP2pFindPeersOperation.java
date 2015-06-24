/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Nicolas Michon
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

import java.util.ArrayList;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerSocketAddress;
import org.meta.common.Identity;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomp2p implementation of the find peers for hash operation.
 *
 * @author nico
 */
public class TomP2pFindPeersOperation extends FindPeersOperation {

    private static final Logger logger = LoggerFactory.getLogger(TomP2pFindPeersOperation.class);

    private TomP2pDHT dht;
    private Number160 hash;

    public TomP2pFindPeersOperation(TomP2pDHT dht, Number160 hash) {
        this.dht = dht;
        this.hash = hash;
    }

    @Override
    public void start() {
        FutureGet futureGet = this.dht.getPeerDHT().get(this.hash).all().start();
        futureGet.addListener(new BaseFutureListener<FutureGet>() {

            @Override
            public void operationComplete(FutureGet getOp) throws Exception {
                if (getOp.isFailed()) {
                    logger.debug("Failed to get hash from dht." + getOp.failedReason());
                    TomP2pFindPeersOperation.this.setFailed("Failed to get hash from dht." + getOp.failedReason());
                } else if (getOp.isSuccess()) {
                    PeerSocketAddress data = (PeerSocketAddress) getOp.data().object();
                    logger.debug("YEAHHH. Got data: " + data);
                    TomP2pFindPeersOperation.this.peersFromData(data);
                    TomP2pFindPeersOperation.this.setState(OperationState.COMPLETE);
                    TomP2pFindPeersOperation.this.finish();
                }
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                logger.debug("Tomp2p find peers operation failed", t);
                TomP2pFindPeersOperation.this.setFailed(t);
            }
        });
    }

    /**
     * @param data 
     */
    private void peersFromData(PeerSocketAddress data) {
        logger.debug("Initializing peers form received data.");
        this.peers = new ArrayList<>();
        //Only for test!!
        this.peers.add(new MetaPeer(null, data.inetAddress(), (short) data.udpPort()));
    }

    @Override
    public void finish() {
        this.notifyListeners();
    }

}
