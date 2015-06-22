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

import java.io.IOException;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
//import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import org.meta.common.Identity;
import org.meta.common.MetHash;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.DHTConfiguration;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.meta.dht.StoreOperation;

/**
 *
 * Tomp2p implementation of MetaDHT.
 *
 * Uses the tomp2p library as a backend for DHT operations.
 *
 * It is a first implementation and will improve with time.
 *
 * @author nico
 */
public class TomP2pDHT extends MetaDHT {

    /**
     * The tomp2p peer representing our node.
     */
    private Peer peer;
    private PeerDHT peerDHT;

    /**
     * Empty constructor (should not be called directly)
     */
    public TomP2pDHT() {
    }

    /**
     *
     * @return The {@link net.tomp2p.p2p.PeerDHT} representing our node.
     */
    public PeerDHT getPeerDHT() {
        return this.peerDHT;
    }

    @Override
    public void start(DHTConfiguration configuration) throws IOException {
        this.configuration = configuration;
        this.startAndListen();
    }

    /**
     * Initializes tomp2p2 and starts listening on the DHT.
     */
    private void startAndListen() throws IOException {
        Number160 peerId = toNumber160(this.configuration.getIdentity());
        Bindings b = new Bindings(); //Bind to everything
        //TODO check and configure network properly
        PeerBuilder peerBuilder = new PeerBuilder(peerId);
        peerBuilder.ports(this.configuration.getPort());
        peerBuilder.bindings(b);
        this.peer = peerBuilder.start();

        PeerBuilderDHT peerBuilderDHT = new PeerBuilderDHT(peer);
        this.peerDHT = peerBuilderDHT.start();

        //PeerMaker peerMaker = new PeerMaker(peerId);
        //peerMaker.setPorts(this.configuration.getPort());
        //peerMaker.setBindings(b);
        //this.peer = peerMaker.makeAndListen();
        //this.peer.getConfiguration().setBehindFirewall(true);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StoreOperation store(MetHash hash) {
        Number160 tomp2pHash = toNumber160(hash);
        TomP2pStoreOperation storeOperation = new TomP2pStoreOperation(this, tomp2pHash);

        storeOperation.start();
        return storeOperation;
    }

    //BELOW STATIC UTILITY FUNCTIONS (Mostly conversion functions for meta <-> tomp2p entities)
    //TODO Move to utility class ?

    /**
     * Utility function to convert a MetHash to a Number160 used by TomP2p lib.
     *
     * @param hash The hash to convert.
     *
     * @return The created Number160.
     */
    public static Number160 toNumber160(MetHash hash) {
        return new Number160(hash.toByteArray());
    }

    /**
     * Convert a tomp2p hash ({@link  Number160}) to a {@link MetHash}
     *
     * @param hash The tomp2p hash to convert.
     * @return The created MetHash.
     */
    public static MetHash toMetHash(Number160 hash) {
        return new MetHash(hash.toByteArray());
    }

    /**
     * Convert a tomp2p peer to a meta peer.
     *
     * @param peer The tomp2p peer to convert
     * @return The created Meta Peer.
     */
    public static MetaPeer toPeer(net.tomp2p.p2p.Peer peer) {
        Identity id = new Identity(toMetHash(peer.peerID()));
        //TODO check if using only UDP port is correct!
        return new MetaPeer(id, peer.peerAddress().inetAddress(), (short) peer.peerAddress().udpPort());
    }
}
