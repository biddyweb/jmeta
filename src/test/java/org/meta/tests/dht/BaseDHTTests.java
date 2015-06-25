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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.meta.common.Identity;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.configuration.DHTConfiguration;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;
import org.meta.dht.StoreOperation;
import org.meta.dht.tomp2p.TomP2pDHT;
import org.meta.tests.MetaBaseTests;

/**
 *
 */
public abstract class BaseDHTTests extends MetaBaseTests {

    public static final short DHT1_PORT = 15000;
    public static final short DHT2_PORT = 15001;
    public static final String DHT1_CONFIG = InetAddress.getLoopbackAddress().getHostAddress() + ":" + DHT1_PORT;
    public static final String DHT2_CONFIG = InetAddress.getLoopbackAddress().getHostAddress() + ":" + DHT2_PORT;
    public static final Boolean DHT1_BROADCAST = false;
    public static final Boolean DHT2_BROADCAST = false;

    protected static MetaDHT dhtNode1;
    protected static MetaDHT dhtNode2;
    protected static DHTConfiguration configurationDht1;
    protected static DHTConfiguration configurationDht2;

    protected static MetHash validHash = new MetHash(42);
    protected static MetHash invalidHash = new MetHash(43);

    @Override
    public void setUp() {
        super.setUp();
        Logger.getLogger(BaseDHTTests.class.getCanonicalName()).log(Level.INFO, "Creating tests dht nodes...");
        setupDht1();
        setupDht2();
        Logger.getLogger(BaseDHTTests.class.getCanonicalName()).log(Level.INFO, "Tests dht nodes Created.");
    }

    public static DHTConfiguration createDhtConfig(Identity id, short port, Collection<MetaPeer> peers, boolean broadcast, boolean localOnly) {
        DHTConfiguration dhtConfig = new DHTConfiguration();

        dhtConfig.setIdentity(id);
        dhtConfig.setPort(port);
        dhtConfig.setKnwonPeers(peers);
        dhtConfig.setBootstrapBroadcast(broadcast);
        dhtConfig.setDhtLocalOnly(localOnly);
        return dhtConfig;
    }

    public static void setupDht1() {
        try {
            dhtNode1 = MetaDHT.getInstance();
            //Hard-coded configuration
            configurationDht1 = createDhtConfig(new Identity(MetamphetUtils.makeSHAHash("Peer1")),
                    DHT1_PORT,
                    DHTConfiguration.peersFromString(DHT2_CONFIG),
                    DHT1_BROADCAST,
                    true);
            dhtNode1.setConfiguration(configurationDht1);
            dhtNode1.start();
        } catch (IOException ex) {
            Logger.getLogger(BaseDHTTests.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        }
    }

    public static void setupDht2() {
        try {
            dhtNode2 = MetaDHT.getInstance();
            //Hard-coded configuration
            configurationDht2 = createDhtConfig(new Identity(MetamphetUtils.makeSHAHash("Peer2")),
                    DHT2_PORT,
                    DHTConfiguration.peersFromString(DHT1_CONFIG),
                    DHT2_BROADCAST,
                    true);
            dhtNode2.setConfiguration(configurationDht2);
            dhtNode2.start();
        } catch (IOException ex) {
            Logger.getLogger(BaseDHTTests.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        }
    }

    @Override
    public void tearDown() {
        dhtNode1.stop();
        dhtNode2.stop();
    }

    /**
     * Utility function to bootstrap the given dht.
     *
     * @param dht
     * @param assertIfEmpty
     */
    public void bootstrapDht(MetaDHT dht, final Boolean assertIfEmpty) {
        BootstrapOperation bootstrapOperation = dht.bootstrap();

        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                org.junit.Assert.fail("Bootstrap operation failed.");
            }

            @Override
            public void complete(BootstrapOperation operation) {
                System.out.println("bootstrap complete!");
                if (assertIfEmpty && operation.getBootstrapTo().isEmpty()) {
                    org.junit.Assert.fail("Bootstrap operation failed.");
                }
            }
        });
        bootstrapOperation.awaitUninterruptibly();
    }

    /**
     * Utility function to create a dht node.
     *
     * @param config
     * @return the created dht node.
     */
    public static MetaDHT createDHTNode(DHTConfiguration config) {
        TomP2pDHT node = new TomP2pDHT(); //Ugly creation here for tests....

        node.setConfiguration(config);
        return (MetaDHT) node;
    }

    /**
     * Utility function, store the given hash in the given DHT.
     * 
     * @param dht
     * @param hash
     */
    public static void StoreIntoDht(MetaDHT dht, final MetHash hash) {
        StoreOperation storeOperation = dht.store(hash);
        storeOperation.addListener(new OperationListener<StoreOperation>() {

            @Override
            public void failed(StoreOperation operation) {
                org.junit.Assert.fail("DHT Store operation failed for hash:" + hash.toString());
            }

            @Override
            public void complete(StoreOperation operation) {
            }
        });
        storeOperation.awaitUninterruptibly();
    }
}
