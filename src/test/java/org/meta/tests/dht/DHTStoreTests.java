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
package org.meta.tests.dht;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.DHTOperation;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;
import org.meta.dht.StoreOperation;
import static org.meta.tests.dht.AbstractDHTTests.dht1;

/**
 * @author nico
 */
public class DHTStoreTests extends AbstractDHTTests {

    /**
     * Test the store process in the DHT.
     */
    @Test
    public void simpleStoreTest() {

        dht1.setConfiguration(configurationDht1); //Re - set valid configuration
        BootstrapOperation bootstrapOperation = dht1.bootstrap();

        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                Assert.fail("Bootstrap operation failed during store test.");
            }

            @Override
            public void complete(BootstrapOperation operation) {
            }
        });
        bootstrapOperation.awaitUninterruptibly();

        StoreOperation storeOperation = dht1.store(validHash);
        storeOperation.addListener(new OperationListener<StoreOperation>() {

            @Override
            public void failed(StoreOperation operation) {
                Logger.getLogger(DHTStoreTests.class.getName()).log(Level.SEVERE, "Store operation failed.");
                Assert.fail("Store operation failed.");
            }

            @Override
            public void complete(StoreOperation operation) {
                System.out.println("Store operation success!");
                Logger.getLogger(DHTStoreTests.class.getName()).log(Level.INFO, "Store operation success!");
            }
        });
        storeOperation.awaitUninterruptibly();

        FindPeersOperation findPeersOperation = dht1.findPeers(validHash);
        findPeersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(FindPeersOperation operation) {
                Logger.getLogger(DHTStoreTests.class.getName()).log(Level.SEVERE, "Find peers operation failed");
            }

            @Override
            public void complete(FindPeersOperation operation) {
                Logger.getLogger(DHTStoreTests.class.getName()).log(Level.INFO, "Find peer operation success!");
            }
        });
        findPeersOperation.awaitUninterruptibly();
    }
}
