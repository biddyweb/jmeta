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
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;
import org.meta.dht.StoreOperation;
import static org.meta.tests.dht.AbstractDHTTests.validHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nico
 */
public class DHTStoreTest extends AbstractDHTTests {

    private static final Logger logger = LoggerFactory.getLogger(DHTStoreTest.class);

    /**
     * Test the store process in the DHT.
     */
    @Test
    public void testSimpleStore() {

        this.bootstrapDht(dht1, false);
        this.bootstrapDht(dht2, true);

        StoreOperation storeOperation = dht1.store(validHash);
        storeOperation.addListener(new OperationListener<StoreOperation>() {

            @Override
            public void failed(StoreOperation operation) {
                logger.error("DHT1 Store operation failed.");
                Assert.fail("DHT1 Store operation failed.");
            }

            @Override
            public void complete(StoreOperation operation) {
                logger.info("DHT1 Store operation success!");
            }
        });

        storeOperation.awaitUninterruptibly();

//        storeOperation = dht2.store(validHash);
//        storeOperation.addListener(new OperationListener<StoreOperation>() {
//
//            @Override
//            public void failed(StoreOperation operation) {
//                logger.error("DHT2 Store operation failed.");
//                Assert.fail("DHT2  Store operation failed.");
//            }
//
//            @Override
//            public void complete(StoreOperation operation) {
//                logger.info("DHT2 Store operation success!");
//            }
//        });
//
//        storeOperation.awaitUninterruptibly();

        FindPeersOperation findPeersOperation = dht2.findPeers(validHash);
        findPeersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(FindPeersOperation operation) {
                logger.error("Find peers operation failed" + validHash);
                Assert.fail("Find peers operation failed for key: " + validHash);
            }

            @Override
            public void complete(FindPeersOperation operation) {
                logger.info("Find peer operation success!");
                for (MetaPeer peer : operation.getPeers()) {
                    //Assert.assertEquals("Stored and retrieved values are different!", peer.getID().toString());
                    logger.debug("Got peer = " + peer.toString());
                }
            }
        });
        findPeersOperation.awaitUninterruptibly();
    }
}
