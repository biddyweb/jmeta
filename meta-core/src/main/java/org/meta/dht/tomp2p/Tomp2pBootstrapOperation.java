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
import java.util.ArrayList;
import java.util.Collection;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureAnnounce;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.builder.AnnounceBuilder;
import net.tomp2p.p2p.builder.DiscoverBuilder;
import net.tomp2p.peers.PeerAddress;
import org.meta.api.common.Identity;
import org.meta.api.dht.BootstrapOperation;
import org.meta.api.dht.MetaPeer;
import org.meta.configuration.DHTConfigurationImpl;
import org.meta.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nico
 */
public class Tomp2pBootstrapOperation extends BootstrapOperation {

    private static final Logger logger = LoggerFactory.getLogger(Tomp2pBootstrapOperation.class);
    private final TomP2pDHT dht;
    private final Collection<MetaPeer> knownPeers;
    private final Collection<MetaPeer> localPeers;
    private final Collection<MetaPeer> publicPeers;
    private final boolean broadcast;
    private Tomp2pFutureDiscoverListener discoverListener;
    private Tomp2pFutureBootstrapListener bootstrapListener;

    /**
     * Create the bootstrap operation with given arguments.
     *
     * @param dhtNode The dht.
     * @param peers The list of peers to bootstrap to.
     * @param bootstrapBroadcast If we broadcast or not.
     */
    public Tomp2pBootstrapOperation(final TomP2pDHT dhtNode, final Collection<MetaPeer> peers,
            final boolean bootstrapBroadcast) {
        this.dht = dhtNode;
        this.knownPeers = peers;
        this.broadcast = bootstrapBroadcast;
        this.publicPeers = NetworkUtils.getPublicPeers(peers);
        this.localPeers = NetworkUtils.getLocalPeers(peers);
    }

    @Override
    public void start() {
        if (!broadcast && (knownPeers == null || knownPeers.isEmpty())) {
            //Finish early as we have no one to bootstrap to...
            //Later we might want to find peers another way ?
            logger.warn("Empty bootstrap peers.");
            this.finish();
            return;
        }
        this.discoverListener = new Tomp2pFutureDiscoverListener(
                this.knownPeers.size(), this.dht.getPeer().peerAddress().inetAddress());
        this.bootstrapListener = new Tomp2pFutureBootstrapListener(
                broadcast ? this.knownPeers.size() + 1 : this.knownPeers.size());
        this.setState(OperationState.WAITING);

        if (dht.getConfiguration().isDhtLocalOnly()) {
            logger.debug("Bootstraping dht in local-only mode.");
            //If local network only, do no discover first
            this.startBootstrap();
        } else {
            //Discovery is only to find our public IP using one of the bootstrap peers.
            this.startDiscover();
        }
    }

    @Override
    public final void finish() {
        if (this.bootstrapListener != null) {
            for (FutureBootstrap bootstrapFuture : this.bootstrapListener.getOperations()) {
                if (bootstrapFuture.isFailed()) {
                    logger.debug("Bootstrap Future failure : " + bootstrapFuture.failedReason());
                    continue;
                }
                for (PeerAddress addr : bootstrapFuture.bootstrapTo()) {
                    MetaPeer peer = new MetaPeer();
                    peer.setAddress(addr.inetAddress());
                    peer.setPort((short) addr.udpPort());
                    peer.setId(new Identity(TomP2pUtils.toMetHash(addr.peerId())));
                    this.bootstrapTo.add(peer);
                    logger.debug("DHT bootstraped to a peer : " + peer.toString());
                }
            }
        }
        if (this.bootstrapTo.isEmpty()) {
            logger.warn("DHT bootstrap failed. Waiting for friends to come :)");
            //Do not consider an empty bootstrap list a failure, if we are alone in the world
            // we will just wait for someone to come!
        }
        this.setState(OperationState.COMPLETE);
        this.notifyListeners();
    }

    /**
     * Called by the Discovery listener once operation has finished successfully or directly if no discovery
     * is needed.
     *
     * The actual bootstrap starts here.
     */
    private void startBootstrap() {
        if (broadcast) {
            logger.debug("Broadcasting to find peers.");
            AnnounceBuilder announceBuilder = new AnnounceBuilder(this.dht.getPeerDHT().peer());
            FutureAnnounce announce = announceBuilder.port(DHTConfigurationImpl.DEFAULT_DHT_PORT).start();
            announce.addListener(this.bootstrapListener);
        }
        Collection<MetaPeer> bootstrapPeers;
        if (this.dht.getConfiguration().isDhtLocalOnly()) {
            bootstrapPeers = localPeers;
        } else {
            bootstrapPeers = publicPeers;
        }
        if (!broadcast && bootstrapPeers.isEmpty()) {
            logger.warn("No one to bootstrap to, bootstrap failed.");
            this.finish();
            return;
        }
        for (MetaPeer peer : bootstrapPeers) {
            //Start the bootstrap operation on 'peer'
            logger.debug("Bootstraping to peer : " + peer);
            this.dht.getPeerDHT().peer().bootstrap().inetAddress(peer.getAddress()).ports(peer.getPort())
                    .start().addListener(this.bootstrapListener);
        }
    }

    /**
     * Start the discovery of our public address using any public peers in the configuration.
     */
    private void startDiscover() {
        if (publicPeers.isEmpty()) {
            logger.warn("Failed to discover our public address: no public peers specified in configuration.");
            this.finish();
        }
        for (MetaPeer peer : publicPeers) {
            DiscoverBuilder db = this.dht.getPeer().discover();
            //db.discoverTimeoutSec(100);
            //db.setExpectManualForwarding(true);
            db.inetAddress(peer.getAddress()).ports(peer.getPort());
            db.start().addListener(this.discoverListener);
        }
    }

    /**
     * Nested class implementation for the Tomp2p discovery future operation listener.
     *
     * Bridge between tomp2p future and our async operation.
     *
     * Wait for all tomp2p operations (we might use several peers for discovery to finish.
     */
    private class Tomp2pFutureDiscoverListener implements BaseFutureListener<FutureDiscover> {

        private int nbOperations;
        private boolean discoveryDone = false;
        private final InetAddress origAddress;

        /**
         * Initializes the listener and specify the number of operations to wait for before notifying.
         */
        Tomp2pFutureDiscoverListener(final int operations, final InetAddress peerAddr) {
            this.nbOperations = operations;
            this.origAddress = peerAddr;
            logger.debug("Discovery listener with original peer address :" + peerAddr);
        }

        @Override
        public void operationComplete(final FutureDiscover future) throws Exception {
            //We don't rely on isSuccess or isFailed here because the tomp2p Discovery is a bit broken...
            --nbOperations;
            PeerAddress peerAddr = future.peerAddress();
            if (peerAddr != null && !peerAddr.inetAddress().equals(origAddress)) {
                //We guess it is an operation success.
                discoveryDone = true;
                logger.debug("Discovery reported our peer address ?:" + future.peerAddress().inetAddress());
                Tomp2pBootstrapOperation.this.startBootstrap();
            } else if (peerAddr == null || peerAddr.inetAddress().equals(origAddress)) {
                //To operation has failed.
                if (discoveryDone) {
                    return;
                }
                if (nbOperations == 0) {
                    logger.info("Discovery failed.");
                    Tomp2pBootstrapOperation.this.finish();
                }
            }
        }

        @Override
        public void exceptionCaught(final Throwable t) throws Exception {
            logger.error("Exception caught while trying to discover our public address.", t);
        }

    }

    /**
     * Nested class implementation for the Tomp2p bootstrap future operation listener.
     *
     * Bridge between tomp2p future and our async operation.
     *
     * Wait for all tomp2p operations (we might bootstrap to several peers or also broadcast) to finish before
     * notifying our operation.
     */
    private class Tomp2pFutureBootstrapListener implements BaseFutureListener<FutureBootstrap> {

        private final int nbOperations;
        private final Collection<FutureBootstrap> operations;

        /**
         * Initializes the listener and specify the number of operations to wait for before notifying.
         */
        public Tomp2pFutureBootstrapListener(final int operationsNb) {
            this.nbOperations = operationsNb;
            this.operations = new ArrayList<>();
        }

        @Override
        public void operationComplete(final FutureBootstrap future) throws Exception {
            synchronized (this) { //We might be called from multiple threads.
                this.operations.add(future);
                if (this.operations.size() == this.nbOperations) {
                    Tomp2pBootstrapOperation.this.finish();
                }
            }
        }

        @Override
        public void exceptionCaught(final Throwable t) throws Exception {
            logger.error("Exception caught while waiting for bootstrap.");
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
