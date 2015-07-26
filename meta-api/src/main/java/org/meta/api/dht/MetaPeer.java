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
package org.meta.api.dht;

import java.net.InetAddress;
import java.util.Objects;
import org.meta.api.common.Identity;

/**
 *
 * Class representing a peer in the DHT.
 *
 * Must a least contain an IP address and a port.
 *
 * May also contain other information and operation related to a peer.
 *
 * @see {@link MetHash}
 *
 */
public class MetaPeer {

    /**
     * The identity of the peer on the DHT.
     */
    protected Identity id;

    /**
     * The InetAddress to contact this peer.
     */
    protected InetAddress address;

    /**
     * The port to contact this peer.
     */
    protected short port;

    /**
     * Default constructor. Initialize this peer with empty attributes.
     */
    public MetaPeer() {
    }

    /**
     *
     * Initialize this peer with the given id, with empty address and port.
     *
     * @param identity The peer id.
     */
    public MetaPeer(final Identity identity) {
        this(identity, null, (short) 0);
    }

    /**
     * Initialize this peer with id, address and port.
     *
     * @param identity The peer id.
     * @param addr The peer address.
     * @param peerPort The peer port.
     */
    public MetaPeer(final Identity identity, final InetAddress addr, final short peerPort) {
        this.id = identity;
        this.address = addr;
        this.port = peerPort;
    }

    /**
     * @return the identity of this peer.
     */
    public final Identity getID() {
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
     * @return The InetAddress.
     */
    public final InetAddress getAddress() {
        return address;
    }

    /**
     * Set the address of this peer.
     *
     * @param addr The new address.
     */
    public void setAddress(final InetAddress addr) {
        this.address = addr;
    }

    /**
     * Get the port of this peer.
     *
     * @return The port.
     */
    public final short getPort() {
        return port;
    }

    /**
     * Set the port of this peer.
     *
     * @param peerPort The new port.
     */
    public void setPort(final short peerPort) {
        this.port = peerPort;
    }

    /**
     * @return The string representation of this peer.
     */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MetaPeer [ ");
        if (this.id != null) {
            sb.append(this.id.toString()).append(":");
        }
        if (this.getAddress() != null) {
            sb.append(this.getAddress().getHostAddress()).append(":");
        }
        sb.append(this.port).append(" ]");
        return sb.toString();
    }

    /**
     *
     * @param obj the object to check for equality
     * @return true if equal, false otherwise
     */
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
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        return true;
    }
}
