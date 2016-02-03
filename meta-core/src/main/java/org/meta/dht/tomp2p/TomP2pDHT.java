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
import net.tomp2p.connection.PeerConnection;
import net.tomp2p.connection.PeerException;
import net.tomp2p.connection.Ports;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageLayer.ProtectionEnable;
import net.tomp2p.dht.StorageLayer.ProtectionMode;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDone;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;
import net.tomp2p.peers.PeerStatusListener;
import net.tomp2p.peers.RTT;
import org.meta.api.common.Identity;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.dht.BootstrapOperation;
import org.meta.api.dht.FindPeersOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.dht.StoreOperation;
import org.meta.api.storage.MetaDatabase;
import org.meta.configuration.DHTConfigurationImpl;
import org.meta.dht.DHTPushManager;
import org.meta.dht.tomp2p.storage.TomP2pStorage;
import org.meta.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Tomp2p implementation of MetaDHT.
 *
 * Uses the tomp2p library as a backend for DHT operations. It is a first implementation and will improve with
 * time.
 *
 * @author nico
 * @version $Id: $
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

    private DHTPushManager pushManager = null;

    private final TomP2pStorage tomP2pStorage;

    private final MetaDatabase db;

    /**
     * Create the tomp2p DHT with given configuration.
     *
     * @param config The DHT configuration to use
     * @param database the global storage used to get backing storage for the dht
     */
    public TomP2pDHT(final DHTConfigurationImpl config, final MetaDatabase database) {
        super(config);
        this.db = database;
        this.tomP2pStorage = new TomP2pStorage(db);
    }

    /**
     * <p>
     * Getter for the field <code>peerDHT</code>.</p>
     *
     * @return The {@link net.tomp2p.dht.PeerDHT} representing our node.
     */
    public PeerDHT getPeerDHT() {
        return this.peerDHT;
    }

    /**
     * <p>
     * Getter for the field <code>peer</code>.</p>
     *
     * @return The {@link net.tomp2p.p2p.Peer} representing our node.
     */
    public Peer getPeer() {
        return peer;
    }

    /**
     * {@inheritDoc}
     */
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
        for (String iface : nwConfig.getInterfaces()) {
            b.addInterface(iface);
        }
        Collection<InetAddress> configAddresses = NetworkUtils.getConfigAddresses(nwConfig);
        if (configAddresses.isEmpty() && nwConfig.getInterfaces().isEmpty()) {
            logger.info("DHT: listen ANY");
            b.setListenAny(true);
        } else {
            logger.info("DHT: listen specific addresses/interfaces");
            b.setListenAny(false);
            for (InetAddress addr : configAddresses) {
                logger.info("CONFIGURE BINDING DHT: ADDING BINDING TO ADDR: " + addr);
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
        serverConfig.maxTCPIncomingConnections(0);
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
        clientConfig.maxPermitsPermanentTCP(0);
        clientConfig.maxPermitsTCP(0);
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
        logger.info("Identity on the DHT: " + peerId);
        PeerBuilder peerBuilder = new PeerBuilder(peerId);
        //Udp port from configuration.
        int udpPort = this.configuration.getNetworkConfig().getPort();
        Bindings bindings = configureBindings();
        peerBuilder.portsExternal(udpPort);
        peerBuilder.ports(udpPort);
        //peerBuilder.udpPortForwarding(udpPort);
        peerBuilder.enableBroadcast(false).enableMaintenance(false).enableQuitRPC(false);
        peerBuilder.channelServerConfiguration(getServerConfig(bindings, udpPort));
        peerBuilder.channelClientConfiguration(getClientConfig(bindings));
        this.peer = peerBuilder.start();

        this.peer.peerBean().peerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            @Override
            public void peerInserted(final PeerAddress peerAddress, final boolean verified) {
                logger.debug("PeerMapChangeListener peerInserted: " + peerAddress + " verifid ? " + verified);
            }

            @Override
            public void peerRemoved(final PeerAddress peerAddress, final PeerStatistic storedPeerAddress) {
                logger.debug("PeerMapChangeListener peerRemoved: " + peerAddress);
            }

            @Override
            public void peerUpdated(final PeerAddress peerAddress, final PeerStatistic storedPeerAddress) {
                logger.debug("PeerMapChangeListener peerUpdated: " + peerAddress);
            }
        });
        this.peer.peerBean().addPeerStatusListener(new PeerStatusListener() {
            @Override
            public boolean peerFailed(PeerAddress remotePeer, PeerException exception) {
                logger.debug("PeerStatusListener peer failed: " + remotePeer + ". Exception: " + exception);
                return false;
            }

            @Override
            public boolean peerFound(PeerAddress remotePeer, PeerAddress referrer,
                    PeerConnection peerConnection, RTT roundTripTime) {
                logger.debug("PeerStatusListener peerFound: " + remotePeer + ". referrer: " + referrer);
                return false;
            }
        });
        logger.info("Initial DHT address = " + this.peer.peerAddress());

        PeerBuilderDHT peerBuilderDHT = new PeerBuilderDHT(peer);
        //Use our custom storage.
        peerBuilderDHT.storage(tomP2pStorage);
        this.peerDHT = peerBuilderDHT.start();
        //No protection on dht entries.
        this.peerDHT.storageLayer().protection(ProtectionEnable.NONE, ProtectionMode.NO_MASTER,
                ProtectionEnable.NONE, ProtectionMode.NO_MASTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BootstrapOperation bootstrap() {
        Tomp2pBootstrapOperation b = new Tomp2pBootstrapOperation(this, this.configuration.getKnownPeers(),
                this.configuration.isBootstrapBroadcast());
        b.start();
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FindPeersOperation findPeers(final MetHash hash) {
        Number160 contentHash = TomP2pUtils.toNumber160(hash);
        TomP2pFindPeersOperation operation = new TomP2pFindPeersOperation(this, contentHash);

        //TODO instead of always launching a find peers operation, first check if we need it.
        //We could return local results directly if they are recent enough.
        operation.start();
        return operation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(final MetHash hash, final long expirationDate) {
        pushManager.pushElement(hash, expirationDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(final MetHash hash) {
        push(hash, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreOperation doStore(final MetHash hash) {
        Number160 tomp2pHash = TomP2pUtils.toNumber160(hash);
        TomP2pStoreOperation storeOperation = new TomP2pStoreOperation(this, hash, tomp2pHash);

        storeOperation.start();
        return storeOperation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
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

    /**
     * <p>
     * Setter for the field <code>pushManager</code>.</p>
     *
     * @param dhtPushManager the pushManager to set
     */
    public void setPushManager(final DHTPushManager dhtPushManager) {
        this.pushManager = dhtPushManager;
    }
}
