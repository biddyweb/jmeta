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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.meta.common.MetaProperties;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.DHTConfiguration;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;

/**
 *
 * @author nico
 */
public class DHTBootstrapTests {

    @Test
    public void test() {
        MetaDHT dht = MetaDHT.getInstance();
        DHTConfiguration configuration = new DHTConfiguration(MetaProperties.get("conf/tests.prop"));
        try {
            dht.start(configuration);
        } catch (IOException ex) {
            Logger.getLogger(DHTBootstrapTests.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            System.out.println("Waiting 15s for other (test) peers to initialize...");
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        dht.bootstrap().addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                Logger.getLogger(DHTBootstrapTests.class.getName()).log(Level.SEVERE, "Bootstrap oeration failed.");
                Assert.fail("Bootstrap oeration failed.");
            }

            @Override
            public void complete(BootstrapOperation operation) {
                if (operation.isFailure()) {
                    Assert.fail("Bootstrap oeration completed unsuccessfully.");
                }
                for (MetaPeer peer : operation.getBootstrapTo()) {
                    System.out.println("Bootstraped to : " + peer);
                }
            }
        });

        try {
            System.out.println("Waiting 60s for peers to bootstrap to us...");
            Thread.sleep(60000);
        } catch (InterruptedException ex) {
            Logger.getLogger(DHTBootstrapTests.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }
}
