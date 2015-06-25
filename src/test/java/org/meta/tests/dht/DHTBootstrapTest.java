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

import java.io.IOException;
import java.net.InetAddress;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.common.Identity;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.configuration.DHTConfiguration;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import static org.meta.tests.dht.BaseDHTTests.createDhtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nico
 */
public class DHTBootstrapTest extends BaseDHTTests {

    public static final short DHT1_PORT = 15000;
    public static final short DHT2_PORT = 15001;
    public static InetAddress DHT1_PEER_ADDR;
    public static InetAddress DHT2_PEER_ADDR;
    public static String DHT1_PEER_STRING;
    public static String DHT2_PEER_STRING;

    protected static MetHash validHash = new MetHash(42);
    protected static MetHash invalidHash = new MetHash(43);

    protected static MetaDHT dhtNode1;
    protected static MetaDHT dhtNode2;
    protected static DHTConfiguration configurationDht1;
    protected static DHTConfiguration configurationDht2;

    private static final Logger logger = LoggerFactory.getLogger(DHTBootstrapTest.class);

    @BeforeClass
    public static void initDHtNodes() throws IOException {

        DHT1_PEER_ADDR = getLocalAddress();
        DHT1_PEER_STRING = DHT1_PEER_ADDR.getHostAddress() + ":" + DHT1_PORT;
        DHT2_PEER_ADDR = getLocalAddress();
        DHT2_PEER_STRING = DHT2_PEER_ADDR.getHostAddress() + ":" + DHT2_PORT;

        configurationDht1 = createDhtConfig(new Identity(MetamphetUtils.makeSHAHash("Peer1")),
                DHT1_PORT,
                DHTConfiguration.peersFromString(DHT2_PEER_STRING),
                false,
                true);
        dhtNode1 = BaseDHTTests.createDHTNode(configurationDht1);
        dhtNode1.start();

        configurationDht2 = createDhtConfig(new Identity(MetamphetUtils.makeSHAHash("Peer2")),
                DHT2_PORT,
                DHTConfiguration.peersFromString(DHT1_PEER_STRING),
                false,
                true);
        dhtNode2 = BaseDHTTests.createDHTNode(configurationDht2);
        dhtNode2.start();
    }

    @AfterClass
    public static void shutDownDhtNodes() {
        dhtNode1.stop();
        dhtNode2.stop();
    }

    /**
     * Test the bootstrap process to another peer. Assumes the two nodes are
     * running.
     */
    @Test
    public void testBootstrapSuccess() {

        DHTBootstrapTest.bootstrapDht(dhtNode1, false);
        //DHTBootstrapTest.bootstrapDht(dhtNode2, true);

        BootstrapOperation bootstrapOperation = (BootstrapOperation) dhtNode2.bootstrap().awaitUninterruptibly();

        if (bootstrapOperation.isFailure()) {
            logger.error("Bootstrap operation failed, reason: " + bootstrapOperation.getFailureMessage());
            Assert.fail("Bootstrap operation failed.");
        } else if (bootstrapOperation.isSuccess()) {
            MetaPeer expectedPeer = new MetaPeer(null, DHT1_PEER_ADDR, DHT1_PORT);

            for (MetaPeer peer : bootstrapOperation.getBootstrapTo()) {
                if (peer.equals(expectedPeer)) {
                    logger.debug("Bootstraped to expected peer!");
                    return;
                }
            }
            Assert.fail("Failed to bootstrap to expected peer");
        }
    }
}
