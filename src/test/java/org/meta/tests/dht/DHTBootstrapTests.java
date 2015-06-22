/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Nicolas Michon
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

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.DHTConfiguration;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;

/**
 *
 * @author nico
 */
public class DHTBootstrapTests extends AbstractDHTTests {

    /**
     * Test the bootstrap process to another peer. Assumes the another node is running!
     */
    @Test
    public void testBootstrapSuccess() {

        dht1.setConfiguration(configurationDht1); //Re - set valid configuration
        BootstrapOperation bootstrapOperation = dht1.bootstrap();

        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                Logger.getLogger(DHTBootstrapTests.class.getName()).log(Level.SEVERE, "Bootstrap operation failed.");
                Assert.fail("Bootstrap operation failed.");
            }

            @Override
            public void complete(BootstrapOperation operation) {
                if (operation.isFailure()) {
                    Assert.fail("Bootstrap operation completed unsuccessfully. Reason : " + operation.getFailureMessage());
                }
                for (MetaPeer peer : operation.getBootstrapTo()) {
                    System.out.println("Bootstraped to : " + peer);
                }
            }
        });
        bootstrapOperation.awaitUninterruptibly();
    }

    /**
     * Test the bootstrap failure to another peer.
     *
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testBootstrapFailure() throws UnknownHostException {

        DHTConfiguration wrongConfig = new DHTConfiguration(null, DHT1_PORT, DHTConfiguration.peersFromString("127.0.0.254:1"), false);
        dht1.setConfiguration(wrongConfig); //Set invalid known peer 

        BootstrapOperation bootstrapOperation = dht1.bootstrap();

        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                Logger.getLogger(DHTBootstrapTests.class.getName()).log(Level.INFO, "testBootstrapFailure: Bootstrap operation failed and it's normal");
            }

            @Override
            public void complete(BootstrapOperation operation) {
                if (operation.isSuccess()) {
                    Assert.fail("Bootstrap oeration completed successfully and shouldn't have.");
                }
            }
        });
        bootstrapOperation.awaitUninterruptibly();
    }
}
