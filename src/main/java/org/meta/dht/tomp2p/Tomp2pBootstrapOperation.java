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
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.builder.AnnounceBuilder;
import net.tomp2p.peers.PeerAddress;
import org.meta.dht.BootstrapOperation;
import org.meta.configuration.DHTConfiguration;
import org.meta.dht.MetaPeer;

/**
 * @author nico
 */
public class Tomp2pBootstrapOperation extends BootstrapOperation {

    private static final Logger logger = LoggerFactory.getLogger(Tomp2pBootstrapOperation.class);
    private final TomP2pDHT dht;
    private Collection<MetaPeer> knownPeers;
    private final boolean broadcast;
    private final Tomp2pFutureBootstrapListener bootstrapListener;

    /**
     * Create the bootstrap operation with given arguments.
     *
     * @param dht The dht.
     * @param knownPeers The list of peers to bootstrap to.
     * @param broadcast If we broadcast or not.
     */
    public Tomp2pBootstrapOperation(TomP2pDHT dht, Collection<MetaPeer> knownPeers, boolean broadcast) {
        this.dht = dht;
        this.knownPeers = knownPeers;
        this.broadcast = broadcast;
        if (this.knownPeers == null) {
            this.knownPeers = new ArrayList<>();
        }
        this.bootstrapListener = new Tomp2pFutureBootstrapListener(broadcast ? this.knownPeers.size() + 1 : this.knownPeers.size());
    }

    @Override
    public void start() {
        this.setState(OperationState.WAITING);
        PeerAddress localAddr = this.dht.getPeerDHT().peer().peerAddress();

        if (dht.getConfiguration().isDhtLocalOnly()) {
            logger.debug("Bootstraping dht in local-only mode.");
            //If local network only, do no discover first
            this.startBootstrap();
        } else {
            this.dht.getPeerDHT().peer().discover().inetAddress(localAddr.inetAddress()).ports(localAddr.udpPort())
                    .start().addListener(new BaseFutureAdapter<FutureDiscover>() {

                        @Override
                        public void operationComplete(FutureDiscover future) throws Exception {
                            if (future.isFailed()) {
                                logger.error("Failed to discover our public address.");
                                Tomp2pBootstrapOperation.this.discoveryFailed();
                                return;
                            }
                            logger.debug("Discovery finished! Our peer public address: {0}", future.peerAddress());
                            Tomp2pBootstrapOperation.this.startBootstrap();
                        }
                    });
        }
    }

    @Override
    public void finish() {
        for (FutureBootstrap bootstrapFuture : this.bootstrapListener.getOperations()) {
            if (bootstrapFuture.isFailed()) {
                logger.debug("BootstrapFuture failure : {0}", bootstrapFuture.failedReason());
                continue;
            }
            for (PeerAddress addr : bootstrapFuture.bootstrapTo()) {
                MetaPeer peer = new MetaPeer();
                peer.setAddress(addr.inetAddress());
                peer.setPort((short) addr.udpPort());
                this.bootstrapTo.add(peer);
                logger.debug("DHT bootstraped to a peer! {0}", peer.toString());
            }
        }
        if (this.bootstrapTo.isEmpty()) {
            logger.warn("DHT bootstrap fail. Waiting for friends to come :)");
            //Do not consider an empty bootstrap list a failure, if we are alone in the world
            // we will just wait for someone to come!
        }
        this.setState(OperationState.COMPLETE);
        this.notifyListeners();
    }

    /**
     * Called by the Discovery listener once operation has finished successfully
     * or if it is not needed.
     *
     * The actual bootstrap starts here.
     */
    private void startBootstrap() {
        if (broadcast) {
            logger.debug("Broadcasting to find peers.");
            AnnounceBuilder announceBuilder = new AnnounceBuilder(this.dht.getPeerDHT().peer());
            announceBuilder.port(DHTConfiguration.DEFAULT_DHT_PORT).start().addListener(this.bootstrapListener);
        }
        if (!broadcast && knownPeers.isEmpty()) {
            //Finish early as we have no one to bootstrap to...
            this.finish();
        } else {
            for (MetaPeer peer : knownPeers) {
                //Start the discovery operation
                logger.debug("Starting bootstrap to peer : {0}", peer);
                this.dht.getPeerDHT().peer().bootstrap().inetAddress(peer.getAddress()).ports(peer.getPort())
                        .start().addListener(this.bootstrapListener);
            }
        }
    }

    /**
     * Called by the Discovery listener if the operation failed.
     *
     * If this operation failed we cannot continue so we notify listeners and
     * exit...
     */
    private void discoveryFailed() {
        this.setFailed("Discovery operation failed");
    }

    /**
     * Nested class implementation for the Tomp2p bootstrap future operation
     * listener.
     *
     * Bridge between tomp2p future and our async operation.
     *
     * Wait for all tomp2p operations (we might bootstrap to several peers or
     * also broadcast) to finish before notifying our operation.
     */
    private class Tomp2pFutureBootstrapListener extends BaseFutureAdapter<FutureBootstrap> {

        private final int nbOperations;
        private final Collection<FutureBootstrap> operations;

        /**
         * Initializes the listener and specify the number of operations to wait
         * for before notifying.
         */
        public Tomp2pFutureBootstrapListener(int nbOperations) {
            this.nbOperations = nbOperations;
            this.operations = new ArrayList<>();
        }

        @Override
        public void operationComplete(FutureBootstrap future) throws Exception {
            synchronized (this) { //We might be called from multiple threads.
                this.operations.add(future);
                if (this.operations.size() == this.nbOperations) {
                    Tomp2pBootstrapOperation.this.finish();
                }
            }
        }

        /**
         *
         * @return The operations.
         */
        public Collection<FutureBootstrap> getOperations() {
            return this.operations;
        }
    }
}
