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

import java.io.IOException;
import java.net.InetAddress;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDone;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import org.meta.common.Identity;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.configuration.MetaConfiguration;
import org.meta.configuration.NetworkConfiguration;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.StoreOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Tomp2p implementation of MetaDHT.
 *
 * Uses the tomp2p library as a backend for DHT operations.
 *
 * It is a first implementation and will improve with time.
 *
 */
public class TomP2pDHT extends MetaDHT {

    private final Logger logger = LoggerFactory.getLogger(TomP2pDHT.class);

    /**
     * The tomp2p peer representing our node.
     */
    private PeerDHT peerDHT;

    /**
     *
     */
    private Peer peer;

    /**
     * Empty constructor (should not be called directly)
     */
    public TomP2pDHT() {
    }

    /**
     * @return The {@link net.tomp2p.dht.PeerDHT} representing our node.
     */
    public PeerDHT getPeerDHT() {
        return this.peerDHT;
    }

    /**
     *
     * @return The {@link net.tomp2p.p2p.Peer} representing our node.
     */
    public Peer getPeer() {
        return peer;
    }

    @Override
    public void start() throws IOException {
        this.startAndListen();
    }

    /**
     * Create the bindings for tomp2p based on the dht configuration.
     *
     * If Network configuration has empty addresses or interfaces, it will bind to
     * everything.
     *
     * @return The bindings.
     */
    private Bindings configureBindings() {
        NetworkConfiguration nwConfig = this.configuration.getNetworkConfig();
        Bindings b = new Bindings();

        for (InetAddress addr : nwConfig.getAddresses()) {
            logger.debug("DHT binding to address: " + addr);
            b.addAddress(addr);
        }
        for (String iface : nwConfig.getInterfaces()) {
            logger.debug("DHT binding to interface: " + iface);
            b.addInterface(iface);
        }
        return b;
    }

    /**
     * Initializes tomp2p2 and starts listening on the DHT.
     */
    private void startAndListen() throws IOException {
        if (this.configuration.getIdentity() == null) {
            Identity id = new Identity(MetamphetUtils.createRandomHash());
            this.configuration.setIdentity(id);
        }
        Number160 peerId = TomP2pUtils.toNumber160(this.configuration.getIdentity());

        //PeerBuilderDHT peerBuilderDHT = new PeerBuilderDHT(null)
        PeerBuilder peerBuilder = new PeerBuilder(peerId);
        peerBuilder.ports(MetaConfiguration.getDHTConfiguration().getNetworkConfig().getPort());
        peerBuilder.bindings(configureBindings());
        this.peer = peerBuilder.start();

        this.peerDHT = new PeerBuilderDHT(peer).start();
        //Here define custom storage layer for routing table etc...
    }

    @Override
    public BootstrapOperation bootstrap() {
        Tomp2pBootstrapOperation b = new Tomp2pBootstrapOperation(this, this.configuration.getKnownPeers(),
                this.configuration.isBootstrapBroadcast());
        b.start();
        return b;
    }

    @Override
    public FindPeersOperation findPeers(MetHash hash) {
        Number160 contentHash = TomP2pUtils.toNumber160(hash);
        TomP2pFindPeersOperation operation = new TomP2pFindPeersOperation(this, contentHash);

        operation.start();
        return operation;
    }

    @Override
    public StoreOperation store(MetHash hash) {
        Number160 tomp2pHash = TomP2pUtils.toNumber160(hash);
        TomP2pStoreOperation storeOperation = new TomP2pStoreOperation(this, tomp2pHash);

        storeOperation.start();
        return storeOperation;
    }

    @Override
    public void stop() {
        if (peer == null) {
            return;
        }
        FutureDone<Void> shutdownAnnounce = this.peer.announceShutdown().start();
        shutdownAnnounce.addListener(new BaseFutureAdapter<BaseFuture>() {

            @Override
            public void operationComplete(BaseFuture future) throws Exception {
                logger.debug("Successfully announced to peers we are shutting down");
            }
        });
        shutdownAnnounce.awaitUninterruptibly();

        BaseFuture shutDownOperation = TomP2pDHT.this.peer.shutdown();
        shutDownOperation.addListener(new BaseFutureAdapter<BaseFuture>() {

            @Override
            public void operationComplete(BaseFuture future) throws Exception {
                TomP2pDHT.this.logger.info("Meta DHT has shut down.");
            }
        });
        shutDownOperation.awaitUninterruptibly();
    }
}
