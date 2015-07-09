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

import java.util.Collection;
import org.meta.common.Identity;
import org.meta.common.MetHash;
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

    public static DHTConfiguration createDhtConfig(Identity id, short port, Collection<MetaPeer> peers, boolean broadcast, boolean localOnly) {
        DHTConfiguration dhtConfig = new DHTConfiguration();

        dhtConfig.setIdentity(id);
        dhtConfig.getNetworkConfig().setPort(port);
        dhtConfig.setKnwonPeers(peers);
        dhtConfig.setBootstrapBroadcast(broadcast);
        dhtConfig.setDhtLocalOnly(localOnly);
        return dhtConfig;
    }

    /**
     * Utility function to create a dht node.
     *
     * @param config
     * @return the created dht node.
     */
    public static MetaDHT createDHTNode(DHTConfiguration config) {
        return (MetaDHT)new TomP2pDHT(config);
    }

    /**
     * Utility function to bootstrap the given dht.
     *
     * @param dht
     * @param assertIfEmpty
     */
    public static void bootstrapDht(MetaDHT dht, final Boolean assertIfEmpty) {
        BootstrapOperation bootstrapOperation = dht.bootstrap();

        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                org.junit.Assert.fail("Bootstrap operation failed.");
            }

            @Override
            public void complete(BootstrapOperation operation) {
                if (assertIfEmpty && operation.getBootstrapTo().isEmpty()) {
                    org.junit.Assert.fail("Bootstrap operation failed.");
                }
            }
        });
        bootstrapOperation.awaitUninterruptibly();
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
