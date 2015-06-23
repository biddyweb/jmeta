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

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.meta.dht.DHTConfiguration;
import org.meta.dht.MetaPeer;

/**
 * @author nico
 */
public class DHTConfigTest {

    @Test
    /**
     * Test the parsing of DHT bootstrap peers in the configuration file.
     */
    public void testPeersParsing() {
        String testString1 = "127.0.0.1:4000";
        String testString2 = "127.0.0.1:4000,";
        String testString3 = "127.0.0.1:4000,127.0.0.1:4001";
        String testString4 = "127.0.0.1:4000, dsfds";
        String testString5 = "127.0.0.14000";

        Collection<MetaPeer> tmp;
        try {
            tmp = DHTConfiguration.peersFromString(testString1);
            Assert.assertNotNull("Test string1 should not fail.", tmp);
            Assert.assertEquals("Test string1 should contain 1 peer", tmp.size(), 1);
        } catch (UnknownHostException ex) {
            Logger.getLogger(DHTConfigTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Test string1 host should be known.");
        }
        try {
            tmp = DHTConfiguration.peersFromString(testString2);
            Assert.assertNotNull("Test string2 should not fail.", tmp);
            Assert.assertEquals("Test string2 should contain 1 peer", tmp.size(), 1);
        } catch (UnknownHostException ex) {
            Logger.getLogger(DHTConfigTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Test string1 host should be known.");
        }
        try {
            tmp = DHTConfiguration.peersFromString(testString3);
            Assert.assertNotNull("Test string3 should not fail.", tmp);
            Assert.assertEquals("Test string3 should contain 2 peers", 2, tmp.size());
        } catch (UnknownHostException ex) {
            Logger.getLogger(DHTConfigTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Test string1 host should be known.");
        }
        try {
            tmp = DHTConfiguration.peersFromString(testString4);
            Assert.assertNotNull("Test string4 result should not be null.", tmp);
            Assert.assertEquals("Test string4 should contain 1 peer", 1, tmp.size());
        } catch (UnknownHostException ex) {
            Logger.getLogger(DHTConfigTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            tmp = DHTConfiguration.peersFromString(testString5);
            Assert.assertNotNull("Test string5 result should not be null.", tmp);
            Assert.assertEquals("Test string5 result should not be null.", 0, tmp.size());
        } catch (UnknownHostException ex) {
            Logger.getLogger(DHTConfigTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail("Test string5 should not throw");
        }
    }

}
