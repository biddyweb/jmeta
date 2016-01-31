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
package org.meta.api.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Class representing a peer in the DHT.
 *
 * Must a least contain an IP address and a port.
 *
 * May also contain other information and operation related to a peer.
 *
 * @see {@link MetHash}
 * @author nico
 * @version $Id: $
 */
public class MetaPeer {

    /**
     * The identity of the peer on the DHT.
     */
    protected Identity id;

    /**
     * The inet address and port of this peer.
     */
    protected InetSocketAddress peerAddress;

    /**
     * Default constructor.
     *
     * Initialize this peer with empty attributes.
     */
    public MetaPeer() {
    }

    /**
     * Initialize this peer with empty identity and given peer address.
     *
     * @param peerAddr the peer address
     */
    public MetaPeer(final InetSocketAddress peerAddr) {
        this(null, peerAddr);
    }

    /**
     * Initialize this peer with the given id, with empty peer address.
     *
     * @param identity The peer id.
     */
    public MetaPeer(final Identity identity) {
        this(identity, null, (short) 0);
    }

    /**
     * Initialize this peer with the given id and peer address.
     *
     * @param identity The peer id.
     * @param peerAddr the peer address
     */
    public MetaPeer(final Identity identity, final InetSocketAddress peerAddr) {
        this.id = identity;
        this.peerAddress = peerAddr;
    }

    /**
     * Initialize this peer with id, address and port.
     *
     * @param identity The peer id.
     * @param addr The peer address.
     * @param peerPort The peer port.
     */
    public MetaPeer(final Identity identity, final InetAddress addr, final short peerPort) {
        this(identity, new InetSocketAddress(addr, peerPort));
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return the identity of this peer.
     */
    public final Identity getId() {
        return id;
    }

    /**
     * Set the identity of this peer.
     *
     * @param identity The new identity.
     */
    public final void setId(final Identity identity) {
        this.id = identity;
    }

    /**
     * Get the address of this peer.
     *
     * @return The Peer Address
     */
    public final InetSocketAddress getSocketAddr() {
        return peerAddress;
    }

    /**
     * Set the address of this peer.
     *
     * @param addr The new address.
     */
    public void setAddress(final InetSocketAddress addr) {
        this.peerAddress = addr;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MetaPeer [ ");
        if (this.id != null) {
            sb.append(this.id.toString()).append(":");
        }
        if (this.getSocketAddr() != null) {
            sb.append(this.peerAddress.toString()).append(" ]");
        }
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetaPeer other = (MetaPeer) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.peerAddress.getAddress(), other.peerAddress.getAddress())
                && Objects.equals(this.peerAddress.getPort(), other.peerAddress.getPort());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        hash = 17 * hash + Objects.hashCode(this.peerAddress);
        return hash;
    }
}
