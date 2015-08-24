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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConfiguration;
import net.tomp2p.connection.Ports;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDone;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import org.meta.api.common.Identity;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.dht.BootstrapOperation;
import org.meta.api.dht.FindPeersOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.dht.StoreOperation;
import org.meta.configuration.DHTConfigurationImpl;
import org.meta.utils.NetworkUtils;
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
public final class TomP2pDHT extends MetaDHT {

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
     * Create the tomp2p DHT with given configuration.
     *
     * @param config The DHT configuration to use.
     */
    public TomP2pDHT(final DHTConfigurationImpl config) {
        super(config);
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
     * If Network configuration has empty addresses or interfaces, it will bind to everything.
     *
     * @return The bindings.
     */
    private Bindings configureBindings() {
        NetworkConfiguration nwConfig = this.configuration.getNetworkConfig();
        Bindings b = new Bindings();

        if (nwConfig.ipV4()) {
            b.addProtocol(StandardProtocolFamily.INET);
        }
        if (nwConfig.ipV6()) {
            b.addProtocol(StandardProtocolFamily.INET6);
        }
        Collection<InetAddress> configAddresses = NetworkUtils.getConfigAddresses(nwConfig);
        if (configAddresses.isEmpty()) {
            b.setListenAny(true);
        } else {
            for (InetAddress addr : configAddresses) {
                b.addAddress(addr);
            }
        }
        return b;
    }

    /**
     * The tomp2p channel server configuration initialization.
     */
    private ChannelServerConfiguration getServerConfig(final Bindings bindings, final int port) {
        ChannelServerConfiguration serverConfig = new ChannelServerConfiguration();
        serverConfig.ports(new Ports(port, port));
        serverConfig.forceTCP(false).forceUDP(true);
        serverConfig.pipelineFilter(new PeerBuilder.DefaultPipelineFilter());
        serverConfig.bindings(bindings);
        return serverConfig;
    }

    /**
     * The tomp2p channel client configuration initialization.
     */
    private ChannelClientConfiguration getClientConfig(final Bindings bindings) {
        ChannelClientConfiguration clientConfig = new ChannelClientConfiguration();

        clientConfig.bindings(bindings);
        clientConfig.pipelineFilter(new PeerBuilder.DefaultPipelineFilter());
        clientConfig.maxPermitsPermanentTCP(250);
        clientConfig.maxPermitsTCP(250);
        clientConfig.maxPermitsUDP(250);
        return clientConfig;
    }

    private Number160 getPeerId() {
        if (this.configuration.getIdentity() == null) {
            Identity id = new Identity(MetamphetUtils.createRandomHash());
            this.configuration.setIdentity(id);
        }
        //TODO manage correctly identity!
        return TomP2pUtils.toNumber160(this.configuration.getIdentity());
    }

    /**
     * Initializes tomp2p2 and starts listening on the DHT.
     */
    private void startAndListen() throws IOException {
        Number160 peerId = getPeerId();
        PeerBuilder peerBuilder = new PeerBuilder(peerId);
        //Udp port from configuration.
        int udpPort = this.configuration.getNetworkConfig().getPort();
        Bindings bindings = configureBindings();
        peerBuilder.portsExternal(udpPort);
        peerBuilder.enableBroadcast(false).enableMaintenance(false).enableQuitRPC(false);
        peerBuilder.channelServerConfiguration(getServerConfig(bindings, udpPort));
        peerBuilder.channelClientConfiguration(getClientConfig(bindings));

        this.peer = peerBuilder.start();
//        this.peer.connectionBean().dispatcher().peerBean().addPeerStatusListener(new PeerStatusListener() {
//
//            @Override
//            public boolean peerFailed(PeerAddress remotePeer, PeerException exception) {
//                logger.debug("Peer status listener, peer failed. Remote peer = " + remotePeer, exception);
//                return false;
//            }
//
//            @Override
//            public boolean peerFound(PeerAddress remotePeer, PeerAddress referrer,
//                    PeerConnection peerConnection, RTT roundTripTime) {
//                logger.debug("Peer status listener, peer FOUND. Remote peer = "
//                              + remotePeer + " referrer = " + referrer);
//                return true;
//            }
//        });

        logger.debug("DHT address = " + this.peer.peerAddress());
        this.peerDHT = new PeerBuilderDHT(peer).start();
        //this.peerDHT.
        // TODO Define custom storage layer for routing table etc on DHT peer.
    }

    @Override
    public BootstrapOperation bootstrap() {
        Tomp2pBootstrapOperation b = new Tomp2pBootstrapOperation(this, this.configuration.getKnownPeers(),
                this.configuration.isBootstrapBroadcast());
        b.start();
        return b;
    }

    @Override
    public FindPeersOperation findPeers(final MetHash hash) {
        Number160 contentHash = TomP2pUtils.toNumber160(hash);
        TomP2pFindPeersOperation operation = new TomP2pFindPeersOperation(this, contentHash);

        operation.start();
        return operation;
    }

    @Override
    public StoreOperation store(final MetHash hash) {
        Number160 tomp2pHash = TomP2pUtils.toNumber160(hash);
        TomP2pStoreOperation storeOperation = new TomP2pStoreOperation(this, tomp2pHash);

        storeOperation.start();
        return storeOperation;
    }

    @Override
    public void stop() {
        if (peer == null || peer.isShutdown()) {
            logger.info("DHT peer not initialized or already shutdown.");
            return;
        }
        FutureDone<Void> shutdownAnnounce = this.peer.announceShutdown().start();
        shutdownAnnounce.addListener(new BaseFutureAdapter<BaseFuture>() {

            @Override
            public void operationComplete(final BaseFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.debug("Successfully announced to peers we are shutting down");
                }
            }
        });
        shutdownAnnounce.awaitUninterruptibly();

        BaseFuture shutDownOperation = TomP2pDHT.this.peer.shutdown();
        shutDownOperation.addListener(new BaseFutureAdapter<BaseFuture>() {

            @Override
            public void operationComplete(final BaseFuture future) throws Exception {
                TomP2pDHT.this.logger.info("Meta DHT has shut down.");
            }
        });
        shutDownOperation.awaitUninterruptibly();
    }

}
