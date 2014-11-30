/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Nicolas Michon
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.dht.tomp2p;

import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import org.meta.dht.StoreOperation;

/**
 * Tomp2p implementation of the store operation.
 *
 * @author nico
 */
public class TomP2pStoreOperation extends StoreOperation {

    private TomP2pDHT dht;
    private Number160 hash;

    /**
     * Create the bootstrap operation with given arguments.
     *
     * @param dht The dht.
     */
    public TomP2pStoreOperation(TomP2pDHT dht, Number160 hash) {
        this.dht = dht;
        this.hash = hash;
    }

    @Override
    public void start() {
        this.dht.getPeer().put(this.hash).start().addListener(new BaseFutureListener<BaseFuture>() {

            @Override
            public void operationComplete(BaseFuture future) throws Exception {
                if (future.isSuccess()) {
                    TomP2pStoreOperation.this.setState(OperationState.COMPLETE);
                } else {
                    TomP2pStoreOperation.this.setState(OperationState.FAILED);
                }
                TomP2pStoreOperation.this.finish();
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                TomP2pStoreOperation.this.setState(OperationState.FAILED);
                TomP2pStoreOperation.this.finish();
            }
        });
    }

    @Override
    public void finish() {
        this.notifyListeners();
    }
}
