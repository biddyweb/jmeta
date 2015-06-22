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
package org.meta.dht;

import java.net.InetAddress;
import org.meta.common.Identity;
import org.meta.common.MetHash;

/**
 *
 * Class representing a peer in the DHT. Must a least contain an ip address, a
 * port and a hash (see MetHash) to be contacted through the dht.
 *
 * May also contain other information and operation related to a peer.
 *
 * @see MetHash
 *
 * @author nico
 */
public class MetaPeer {

    protected Identity id;

    protected InetAddress address;

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
     * @param id. The peer's id.
     */
    public MetaPeer(Identity id) {
        this(id, null, (short) 0);
    }

    /**
     * Initialize this peer with id, address and port.
     *
     * @param hash. The peer's id.
     * @param addr. The peer's address.
     * @param port. The peer's  port.
     */
    public MetaPeer(Identity id, InetAddress addr, short port) {
        this.id = id;
        this.address = addr;
        this.port = port;
    }

    public MetHash getID() {
        return id;
    }

    public void setId(Identity id) {
        this.id = id;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MetaPeer [ ");
        if (this.id != null) {
            sb.append(this.id.toString()).append(":");
        }
        if (this.getAddress() != null) {
            sb.append(this.getAddress().getCanonicalHostName()).append(":");
        }
        sb.append(this.port).append("]");
        return sb.toString();
    }

}
