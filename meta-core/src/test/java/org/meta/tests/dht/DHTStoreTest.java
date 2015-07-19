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
package org.meta.tests.dht;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.Identity;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.common.OperationListener;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;
import org.meta.api.dht.FindPeersOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.dht.MetaPeer;
import org.meta.api.dht.StoreOperation;
import org.meta.configuration.ConfigurationUtils;
import org.meta.configuration.DHTConfigurationImpl;
import org.meta.configuration.MetaConfiguration;
import static org.meta.tests.MetaBaseTests.getLocalAddress;
import static org.meta.tests.dht.BaseDHTTests.createDhtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nico
 */
public class DHTStoreTest extends BaseDHTTests {

    private static final short DHT1_PORT = 15000;
    private static final short DHT2_PORT = 15001;

    private static String DHT1_PEER_STRING;

    private static String DHT2_PEER_STRING;
    private static InetAddress DHT1_PEER_ADDR;
    private static InetAddress DHT2_PEER_ADDR;
    private static final short FIRST_AMP_PORT = 4242;
    private static final short SECOND_AMP_PORT = 4243;

    private static final MetHash validHash = new MetHash(42);
    private static final MetHash invalidHash = new MetHash(43);

    private static final Logger logger = LoggerFactory.getLogger(DHTStoreTest.class);

    private static MetaDHT dhtNode1;
    private static MetaDHT dhtNode2;
    private static DHTConfigurationImpl configurationDht1;
    private static DHTConfigurationImpl configurationDht2;

    /**
     *
     * @throws IOException
     */
    @BeforeClass
    public static void initDHtNodes() throws IOException {

        try {
            DHT1_PEER_ADDR = getLocalAddress();
            DHT1_PEER_STRING = DHT1_PEER_ADDR.getHostAddress() + ":" + DHT1_PORT;
            DHT2_PEER_ADDR = getLocalAddress();
            DHT2_PEER_STRING = DHT2_PEER_ADDR.getHostAddress() + ":" + DHT2_PORT;

            MetaConfiguration.getAmpConfiguration().setAmpPort((short) 4243);
            configurationDht1 = createDhtConfig(new Identity(MetamphetUtils.makeSHAHash("Peer1")),
                DHT1_PORT,
                ConfigurationUtils.peersFromString(DHT2_PEER_STRING),
                false,
                true);
            dhtNode1 = BaseDHTTests.createDHTNode(configurationDht1);
            dhtNode1.start();

            configurationDht2 = createDhtConfig(new Identity(MetamphetUtils.makeSHAHash("Peer2")),
                DHT2_PORT,
                ConfigurationUtils.peersFromString(DHT1_PEER_STRING),
                false,
                true);
            dhtNode2 = BaseDHTTests.createDHTNode(configurationDht2);
            dhtNode2.start();

            bootstrapDht(dhtNode1, false);
            bootstrapDht(dhtNode2, true);
        } catch (InvalidConfigurationException ex) {
            java.util.logging.Logger.getLogger(DHTStoreTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @AfterClass
    public static void shutDownDhtNodes() {
        dhtNode1.stop();
        dhtNode2.stop();
    }

    /**
     * Test the store process in the DHT.
     *
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testSimpleStore() throws UnknownHostException, IOException {

        //Forcing AMP Port
        MetaConfiguration.getAmpConfiguration().setAmpPort(FIRST_AMP_PORT);

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

        //Re-setting AMP port to avoid duplicate entry in the DHT.
        MetaConfiguration.getAmpConfiguration().setAmpPort(SECOND_AMP_PORT);
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
                logger.info("Find peer operation success!");
                expectedMetaPeerDht1 = new MetaPeer(null, InetAddress.getLoopbackAddress(),
                    FIRST_AMP_PORT);
                expectedMetaPeerDht2 = new MetaPeer(null, InetAddress.getLoopbackAddress(),
                    SECOND_AMP_PORT);
                //int matchedPeers = 0;
//                for (MetaPeer peer : operation.getPeers()) {
//                    logger.debug("Got peer = " + peer.toString());
//                    if (expectedMetaPeerDht1.equals(peer)
//                        || expectedMetaPeerDht2.equals(peer)) {
//                        ++matchedPeers;
//                    }
//                }

                Assert.assertTrue("We should have retrieved the two peers!", operation.getPeers().size() == 2);
            }
        });
        findPeersOperation.awaitUninterruptibly();
    }
}
