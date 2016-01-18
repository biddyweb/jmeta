/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.tests.dht;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.meta.api.common.Identity;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.BootstrapOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.dht.StoreOperation;
import org.meta.configuration.DHTConfigurationImpl;
import org.meta.dht.tomp2p.TomP2pDHT;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;
import org.meta.utils.NetworkUtils;

/**
 *
 * @author nico
 */
public abstract class BaseDHTTests extends MetaBaseTests {

    /**
     *
     * @param id
     * @param port
     * @param peers
     * @param broadcast
     * @param localOnly
     * @return
     */
    public static DHTConfigurationImpl createDhtConfig(Identity id, short port, Collection<MetaPeer> peers,
            boolean broadcast, boolean localOnly) {
        DHTConfigurationImpl dhtConfig = new DHTConfigurationImpl();

        dhtConfig.setIdentity(id);
        dhtConfig.getNetworkConfig().setIpV6(false);
        dhtConfig.getNetworkConfig().setIpV4(true);
        dhtConfig.getNetworkConfig().setPort(port);
        //dhtConfig.getNetworkConfig().setInterfaces(Collections.singletonList(NetworkUtils.getLoopbackInterfaceName()));
        dhtConfig.getNetworkConfig().setAddresses(Collections.singletonList(NetworkUtils.getLoopbackAddress()));
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
    public static MetaDHT createDHTNode(DHTConfigurationImpl config, String dbName) throws IOException, StorageException {

        //KVStorage storage = new MetaMemoryStorage(MetaConfiguration.getModelConfiguration());
        //MetaCache cache = new MetaCacheStorage(storage, 500);
        return (MetaDHT) new TomP2pDHT(config, getDatabase(dbName));
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
                    org.junit.Assert.fail("Bootstrap operation returned empty list!");
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
        StoreOperation storeOperation = dht.doStore(hash);
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
