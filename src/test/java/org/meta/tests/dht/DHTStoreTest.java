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

import org.junit.Assert;
import org.junit.Test;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.OperationListener;
import org.meta.dht.StoreOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nico
 */
public class DHTStoreTest extends AbstractDHTTests {

    private static final Logger logger= LoggerFactory.getLogger(DHTStoreTest.class);

    /**
     * Test the store process in the DHT.
     */
    @Test
    public void testSimpleStore() {

        dht1.setConfiguration(configurationDht1); //Reset valid configuration
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
                logger.error("Store operation failed.");
                Assert.fail("Store operation failed.");
            }

            @Override
            public void complete(StoreOperation operation) {
                System.out.println("Store operation success!");
                logger.info("Store operation success!");
            }
        });
        storeOperation.awaitUninterruptibly();

//        FindPeersOperation findPeersOperation = dht1.findPeers(validHash);
//        findPeersOperation.addListener(new OperationListener<FindPeersOperation>() {
//
//            @Override
//            public void failed(FindPeersOperation operation) {
//                Logger.getLogger(DHTStoreTests.class.getName()).log(Level.SEVERE, "Find peers operation failed");
//            }
//
//            @Override
//            public void complete(FindPeersOperation operation) {
//                Logger.getLogger(DHTStoreTests.class.getName()).log(Level.INFO, "Find peer operation success!");
//            }
//        });
//        findPeersOperation.awaitUninterruptibly();
    }
}
