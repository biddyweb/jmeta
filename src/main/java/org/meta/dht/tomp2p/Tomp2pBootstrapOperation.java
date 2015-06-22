/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Nicolas Michon
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.dht.tomp2p;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.builder.AnnounceBuilder;
import net.tomp2p.peers.PeerAddress;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.DHTConfiguration;
import org.meta.dht.MetaPeer;

/**
 * @author nico
 */
public class Tomp2pBootstrapOperation extends BootstrapOperation {

    private static final Logger logger = Logger.getLogger(Tomp2pBootstrapOperation.class.getName());
    private TomP2pDHT dht;
    private Collection<MetaPeer> knownPeers;
    private boolean broadcast;
    private Tomp2pFutureDiscoverListener discoverListener;
    private Tomp2pFutureBootstrapListener bootstrapListener;

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
            this.knownPeers = new ArrayList<MetaPeer>();
        }
        if (this.knownPeers.isEmpty()) {
            this.broadcast = true; //force broadcast if empty known peers...
        }
    }

    @Override
    public void start() {
        int nbOperations = broadcast ? 1 + this.knownPeers.size() : this.knownPeers.size();
        discoverListener = new Tomp2pFutureDiscoverListener(this.knownPeers.size());
        this.bootstrapListener = new Tomp2pFutureBootstrapListener(this, nbOperations);
        this.state = OperationState.WAITING;

        for (MetaPeer peer : knownPeers) {
            //Start the discovery operation
            logger.log(Level.WARNING, "Starting discovery to peer : {0}", peer);
            this.dht.getPeerDHT().peer().discover().inetAddress(peer.getAddress()).ports(peer.getPort())
                    .start().addListener(discoverListener);
        }
        if (broadcast) {
            logger.log(Level.WARNING, "Broadcasting to find peers.");
            AnnounceBuilder announceBuilder = new AnnounceBuilder(this.dht.getPeerDHT().peer());
            announceBuilder.port(DHTConfiguration.DEFAULT_DHT_PORT).start().addListener(this.bootstrapListener);
            //this.dht.getPeerDHT().peer().bootstrap().broadcast().setPorts(DHTConfiguration.DEFAULT_DHT_PORT)
               //     .start().addListener(this.bootstrapListener);
        }
    }

    @Override
    public void finish() {
        for (FutureBootstrap bootstrapFuture : this.bootstrapListener.getOperations()) {
            if (bootstrapFuture.isFailed()) {
                logger.log(Level.WARNING, "BootstrapFuture failure : {0}", bootstrapFuture.failedReason());
                continue;
            }
            for (PeerAddress addr : bootstrapFuture.bootstrapTo()) {
                MetaPeer peer = new MetaPeer();
                peer.setAddress(addr.inetAddress());
                peer.setPort((short) addr.udpPort());
                this.bootstrapTo.add(peer);
                logger.log(Level.WARNING, "DHT bootstraped to a peer! {0}", peer.toString());
            }
        }
        if (this.bootstrapTo.isEmpty()) {
            //Consider an empty bootstrap list as a failure...
            this.setState(OperationState.FAILED);
        } else {
            this.setState(OperationState.COMPLETE);
        }
        this.notifyListeners();
    }

    /**
     * Called by the Discovery listener once operation has finished successfully. Start the actual bootstraping here.
     */
    private void discoveryFinished() {
        for (MetaPeer peer : knownPeers) {
            this.dht.getPeerDHT().peer().bootstrap().inetAddress(peer.getAddress()).ports(peer.getPort())
                    .start().addListener(this.bootstrapListener);
        }
    }

    /**
     * Called by the Discovery listener if the operation failed.
     *
     * Mark this operation as failed and notify listeners...
     */
    private void discoveryFailed() {
        this.state = OperationState.FAILED;
        this.notifyListeners();
    }

    /**
     * Nested class implementation for the Tomp2p discover future operation listener.
     *
     * If the discovery operation succeeded, launch the bootstrap operation on the newly found address, otherwise try
     * the original address.
     */
    private class Tomp2pFutureDiscoverListener extends net.tomp2p.futures.BaseFutureAdapter<FutureDiscover> {

        private Boolean succeeddedOnce;
        private int nbOperations;

        public Tomp2pFutureDiscoverListener(final int nbOperations) {
            this.nbOperations = nbOperations;
            this.succeeddedOnce = false;
        }

        @Override
        public void operationComplete(FutureDiscover future) throws Exception {
            nbOperations--;
            if (succeeddedOnce) {
                return;
            } else {
                if (future.isSuccess()) {
                    succeeddedOnce = true;
                    logger.log(Level.WARNING, "DISCOVERY SUCCESS");
                    Tomp2pBootstrapOperation.this.discoveryFinished();
                } else if (future.isFailed()) {
                    logger.log(Level.WARNING, "DISCOVERY FAILURE");
                }
                if (nbOperations == 0 && !succeeddedOnce) {
                    Tomp2pBootstrapOperation.this.discoveryFailed();
                }
            }
        }
    }

    /**
     * Nested class implementation for the Tomp2p bootstrap future operation listener.
     *
     * Bridge between tomp2p future and our async operation.
     *
     * Wait for all tomp2p operations (we might bootstrap to several peers or also broadcast) to finish before notifying
     * our operation.
     */
    private class Tomp2pFutureBootstrapListener extends BaseFutureAdapter<FutureBootstrap> {

        /**
         * The target operation to notify when all is done.
         */
        private Tomp2pBootstrapOperation targetOperation;
        private int nbOperations;
        private Collection<FutureBootstrap> operations;

        /**
         * Initializes the listener and specify the number of operations to wait for before notifying.
         */
        public Tomp2pFutureBootstrapListener(Tomp2pBootstrapOperation targetOperation, int nbOperations) {
            this.targetOperation = targetOperation;
            this.nbOperations = nbOperations;
            this.operations = new ArrayList<FutureBootstrap>();
        }

        @Override
        public void operationComplete(FutureBootstrap future) throws Exception {
            synchronized (this) { //We might be called from multiple threads.
                this.operations.add(future);
                if (this.operations.size() == this.nbOperations) {
                    this.targetOperation.finish();
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
