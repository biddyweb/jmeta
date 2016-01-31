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

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.meta.api.common.Identity;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;

/**
 * General utility class for tomp2p-related entities.
 *
 * Mostly conversion methods.
 *
 * @author nico
 * @version $Id: $
 */
public final class TomP2pUtils {

    /**
     *
     */
    private TomP2pUtils() {
    }

    /**
     * Utility function to convert a MetHash to a Number160 used by TomP2p lib.
     *
     * @param hash The hash to convert.
     * @return The created Number160.
     */
    public static Number160 toNumber160(final MetHash hash) {
        return new Number160(hash.toByteArray());
    }

    /**
     * Convert a tomp2p hash {@link  Number160} to a {@link MetHash}.
     *
     * @param hash The tomp2p hash to convert.
     * @return The created MetHash.
     */
    public static MetHash toMetHash(final Number160 hash) {
        return new MetHash(hash.toByteArray());
    }

    /**
     * Convert a tomp2p peer to a meta peer.
     *
     * @param peer The tomp2p peer to convert
     * @return The created Meta Peer.
     */
    public static MetaPeer toMetaPeer(final Peer peer) {
        Identity id = new Identity(toMetHash(peer.peerID()));
        return new MetaPeer(id, peer.peerAddress().inetAddress(), (short) peer.peerAddress().udpPort());
    }

    /**
     * Convert a tomp2p peer address to a meta peer.
     *
     * @param addr The tomp2p address to convert
     * @return The created Meta Peer.
     */
    public static MetaPeer toMetaPeer(final PeerAddress addr) {
        Identity id = new Identity(toMetHash(addr.peerId()));
        return new MetaPeer(id, addr.inetAddress(), (short) addr.udpPort());
    }

}
