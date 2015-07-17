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
package org.meta.api.dht;

import java.net.InetAddress;
import java.util.Objects;
import org.meta.api.common.Identity;
import org.meta.api.common.MetHash;

/**
 *
 * Class representing a peer in the DHT.
 *
 * Must a least contain an ip address and a port.
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
     * @param id. The peer id.
     */
    public MetaPeer(Identity id) {
        this(id, null, (short) 0);
    }

    /**
     * Initialize this peer with id, address and port.
     *
     * @param id The peer id.
     * @param addr. The peer address.
     * @param port. The peer port.
     */
    public MetaPeer(Identity id, InetAddress addr, short port) {
        this.id = id;
        this.address = addr;
        this.port = port;
    }

    /**
     * @return the identity of this peer.
     */
    public Identity getID() {
        return id;
    }

    /**
     * Set the identity of this peer.
     *
     * @param id The new identity.
     */
    public void setId(Identity id) {
        this.id = id;
    }

    /**
     * Get the address of this peer.
     *
     * @return The InetAddress.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Set the address of this peer.
     *
     * @param address The new address.
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * Get the port of this peer.
     *
     * @return The port.
     */
    public short getPort() {
        return port;
    }

    /**
     * Set the port of this peer.
     *
     * @param port The new port.
     */
    public void setPort(short port) {
        this.port = port;
    }

    /**
     * @return The string representation of this peer.
     */
    @Override
    public String toString() {
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
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
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
