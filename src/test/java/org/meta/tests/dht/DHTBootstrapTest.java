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
import org.meta.dht.BootstrapOperation;
import org.meta.dht.MetaPeer;

/**
 * @author nico
 */
public class DHTBootstrapTest extends AbstractDHTTests {

    /**
     * Test the bootstrap process to another peer. Assumes the another node is
     * running!
     */
    @Test
    public void testBootstrapSuccess() {

        dht1.setConfiguration(configurationDht1); //Reset valid configuration
        BootstrapOperation bootstrapOperation = (BootstrapOperation) dht1.bootstrap().awaitUninterruptibly();

        if (bootstrapOperation.isFailure()) {
            Logger.getLogger(DHTBootstrapTest.class.getName()).log(Level.SEVERE,
                    "Bootstrap operation failed, reason: {0}",
                    bootstrapOperation.getFailureMessage());
            Assert.fail("Bootstrap operation failed.");
        } else if (bootstrapOperation.isSuccess()) {
            //TODO check that the peer we boostraped to is DHT2!
            for (MetaPeer peer : bootstrapOperation.getBootstrapTo()) {
                System.out.println("Bootstraped to : " + peer);
            }
        }
    }
}
