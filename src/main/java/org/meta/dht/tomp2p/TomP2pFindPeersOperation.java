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

import net.tomp2p.peers.Number160;
import org.meta.dht.FindPeersOperation;

/**
 *
 * Tomp2p implementation of the find peers for hash operation.
 *
 * @author nico
 */
public class TomP2pFindPeersOperation extends FindPeersOperation {

    private TomP2pDHT dht;
    private Number160 hash;

    public TomP2pFindPeersOperation(TomP2pDHT dht, Number160 hash) {
        this.dht = dht;
        this.hash = hash;
    }

    @Override
    public void start() {
        //this.dht.getPeer().
    }

    @Override
    public void finish() {
        //  this.d
    }

}
