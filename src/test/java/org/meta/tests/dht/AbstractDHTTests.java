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
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.meta.common.Identity;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.configuration.DHTConfiguration;
import org.meta.dht.MetaDHT;
import org.meta.dht.tomp2p.TomP2pDHT;

/**
 *
 * @author nico
 */
public abstract class AbstractDHTTests extends TestCase {

    public static final short DHT1_PORT = 15000;
    public static final short DHT2_PORT = 15001;
    public static final String DHT1_CONFIG = "0.0.0.0:" + DHT1_PORT;
    public static final String DHT2_CONFIG = "0.0.0.0:" + DHT2_PORT;
    public static final Boolean DHT1_BROADCAST = false;
    public static final Boolean DHT2_BROADCAST = false;

    protected static MetaDHT dht1;
    protected static MetaDHT dht2;
    protected static DHTConfiguration configurationDht1;
    protected static DHTConfiguration configurationDht2;

    protected static MetHash validHash = new MetHash(42);
    protected static MetHash invalidHash = new MetHash(43);

    @Override
    public void setUp() {
        Logger.getLogger(AbstractDHTTests.class.getCanonicalName()).log(Level.INFO, "Creating tests dht nodes...");
        setupDht1();
        setupDht2();
        Logger.getLogger(AbstractDHTTests.class.getCanonicalName()).log(Level.INFO, "Tests dht nodes Created.");
    }

    public static void setupDht1() {
        try {
            dht1 = new TomP2pDHT();
            //Hard-coded configuration
            configurationDht1 = new DHTConfiguration(new Identity(MetamphetUtils.makeSHAHash("Peer1")), DHT1_PORT, DHTConfiguration.peersFromString(DHT2_CONFIG), DHT1_BROADCAST, true);
            dht1.start(configurationDht1);
        } catch (IOException ex) {
            Logger.getLogger(AbstractDHTTests.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        }
    }

    public static void setupDht2() {
        try {
            dht2 = new TomP2pDHT();
            //Hard-coded configuration
            configurationDht2 = new DHTConfiguration(new Identity(MetamphetUtils.makeSHAHash("Peer2")), DHT2_PORT, DHTConfiguration.peersFromString(DHT1_CONFIG), DHT2_BROADCAST, true);
            dht2.start(configurationDht2);
        } catch (IOException ex) {
            Logger.getLogger(AbstractDHTTests.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        }
    }

    @Override
    public void tearDown() {
        dht1.stop();
        dht2.stop();
    }

}
