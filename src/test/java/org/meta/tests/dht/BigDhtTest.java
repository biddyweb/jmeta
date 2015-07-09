/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 JMeta
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
package org.meta.tests.dht;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.Test;

import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class BigDhtTest extends BaseDHTTests {

    private static final Logger logger = LoggerFactory.getLogger(BigDhtTest.class);

    private static final int NB_PEERS = 3;
    private static final MetaDHT[] dhts = new MetaDHT[NB_PEERS];

    @AfterClass
    public static void tearDown() {
        for (int i = 0; i < NB_PEERS; ++i) {
            dhts[i].stop();
        }
    }

    /**
     *
     * @throws IOException
     */
    //@Test
    public void testManyNodesOneHash() throws IOException {
        try {
            short port = 15000;
            Collection<MetaPeer> knownPeer = Collections.singletonList(new MetaPeer(null, InetAddress.getByName("127.0.0.1"), port));

            dhts[0] = BigDhtTest.createDHTNode(BaseDHTTests.createDhtConfig(null, port, null, false, true));
            dhts[0].start();
            for (int i = 1; i < NB_PEERS; ++i) {
                ++port;
                dhts[i] = BigDhtTest.createDHTNode(BaseDHTTests.createDhtConfig(null, (short) (port), knownPeer, false, true));
                dhts[i].start();
            }

            //Bootstrap the first node 'alone' then bootstrap all the others to him
            this.bootstrapDht(dhts[0], false);
            Thread.sleep(150);
            for (int i = 1; i < NB_PEERS; ++i) {
                this.bootstrapDht(dhts[i], true);
                Thread.sleep(150);
            }
            //Create a random hash and store it in every nodes
            MetHash hash = MetamphetUtils.createRandomHash();
            for (int i = 0; i < NB_PEERS; ++i) {
                Thread.sleep(50);
                BaseDHTTests.StoreIntoDht(dhts[i], hash);
            }
            Thread.sleep(2000);

            MetaDHT lastNode = dhts[NB_PEERS - 1];
            FindPeersOperation operation = lastNode.findPeers(hash);
            operation.awaitUninterruptibly();
            //Check that we have all the NB_PEERS peers from the DHT.
            logger.debug("GOT PEERS FOR HASH: " + operation.getPeers().size());
            //Assert.assertTrue("Find peers did not return NB_PEERS peers", operation.getPeers().size() == NB_PEERS);
        } catch (InterruptedException ex) {
            Assert.fail("Interrupted while sleeping...");
        }
    }
}
