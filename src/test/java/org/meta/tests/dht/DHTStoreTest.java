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

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Assert;
import org.junit.Test;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;
import org.meta.dht.StoreOperation;
import static org.meta.tests.dht.BaseDHTTests.DHT1_PORT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nico
 */
public class DHTStoreTest extends BaseDHTTests {

    private static final Logger logger = LoggerFactory.getLogger(DHTStoreTest.class);

    /**
     * Test the store process in the DHT.
     *
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testSimpleStore() throws UnknownHostException {

        this.bootstrapDht(dhtNode1, false);
        this.bootstrapDht(dhtNode2, true);

        StoreOperation storeOperation = dhtNode1.store(validHash);
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

        storeOperation = dhtNode2.store(validHash);
        storeOperation.addListener(new OperationListener<StoreOperation>() {

            @Override
            public void failed(StoreOperation operation) {
                logger.error("DHT2 Store operation failed.");
                Assert.fail("DHT2  Store operation failed.");
            }

            @Override
            public void complete(StoreOperation operation) {
                logger.info("DHT2 Store operation success!");
            }
        });
        storeOperation.awaitUninterruptibly();

        FindPeersOperation findPeersOperation = dhtNode2.findPeers(validHash);
        findPeersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(FindPeersOperation operation) {
                logger.error("Find peers operation failed" + validHash);
                Assert.fail("Find peers operation failed for key: " + validHash);
            }

            @Override
            public void complete(FindPeersOperation operation) {
                MetaPeer expectedMetaPeerDht1;
                MetaPeer expectedMetaPeerDht2;
                try {
                    logger.info("Find peer operation success!");
                    expectedMetaPeerDht1 = new MetaPeer(null, InetAddress.getLocalHost(), DHT1_PORT);
                    expectedMetaPeerDht2 = new MetaPeer(null, InetAddress.getLocalHost(), DHT2_PORT);
                    int matchedPeers = 0;
                    for (MetaPeer peer : operation.getPeers()) {
                        logger.debug("Got peer = " + peer.toString());
                        if (expectedMetaPeerDht1.toString().equals(peer.toString())
                                || peer.toString().equals(expectedMetaPeerDht2.toString())) {
                            ++matchedPeers;
                        }
                    }
                    Assert.assertTrue("We should have retrieved the two peers!", matchedPeers == 2);
                } catch (UnknownHostException ex) {
                    logger.error("", ex);
                    Assert.fail();
                }
            }
        });
        findPeersOperation.awaitUninterruptibly();
    }
}
