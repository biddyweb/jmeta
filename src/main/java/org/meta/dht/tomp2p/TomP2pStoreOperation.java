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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tomp2p.dht.PutBuilder;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.meta.common.MetHash;
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
     * @param hash The hash to store in the DHT.
     */
    public TomP2pStoreOperation(TomP2pDHT dht, Number160 hash) {
        this.dht = dht;
        this.hash = hash;
    }

    @Override
    public void start() {
        try {
            PutBuilder putBuilder;
            putBuilder = new PutBuilder(this.dht.getPeerDHT(), hash);
            putBuilder.data(new Data("test data")).start().addListener(new BaseFutureListener<BaseFuture>() {

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
        } catch (IOException ex) {
            Logger.getLogger(TomP2pStoreOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void finish() {
        this.notifyListeners();
    }
}
