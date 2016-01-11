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
package org.meta.tests;

import java.io.IOException;
import java.util.Collections;
import org.junit.BeforeClass;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.storage.MetaDatabase;
import org.meta.configuration.DHTConfigurationImpl;
import org.meta.configuration.MetaConfiguration;
import org.meta.configuration.ModelConfigurationImpl;
import org.meta.configuration.NetworkConfigurationImpl;
import org.meta.configuration.P2PPConfigurationImpl;
import org.meta.configuration.WSConfigurationImpl;
import org.meta.storage.BerkeleyDatabase;
import org.meta.storage.exceptions.StorageException;
import org.meta.utils.NetworkUtils;

/**
 * Base class for tests to pre-configure the JMETA env.
 */
public abstract class MetaBaseTests {

    /**
     *
     */
    public static void initConfigurations() throws IOException {
        NetworkConfiguration dhtNetworkConfig = new NetworkConfigurationImpl(
                DHTConfigurationImpl.DEFAULT_DHT_PORT,
                Collections.singletonList(NetworkUtils.getLoopbackInterfaceName()),
                null);
        DHTConfigurationImpl dhtConfig = new DHTConfigurationImpl();
        dhtConfig.setNetworkConfig(dhtNetworkConfig);
        MetaConfiguration.setDhtConfiguration(dhtConfig);

        NetworkConfiguration p2ppNetworkConfig = new NetworkConfigurationImpl(
                P2PPConfigurationImpl.DEFAULT_P2PP_PORT,
                Collections.singletonList(NetworkUtils.getLoopbackInterfaceName()),
                null);
        P2PPConfigurationImpl p2ppConfig = new P2PPConfigurationImpl();
        p2ppConfig.setNetworkConfig(p2ppNetworkConfig);
        MetaConfiguration.setP2ppConfiguration(p2ppConfig);
        MetaConfiguration.setWSConfiguration(new WSConfigurationImpl());

        ModelConfigurationImpl modelConfig = new ModelConfigurationImpl();
        modelConfig.setDatabasePath(TestUtils.createTempDir("DefaultBaseTestStorage").getAbsolutePath());
        MetaConfiguration.setModelConfiguration(modelConfig);
    }

    /**
     * Returns a Database implementation with the given name.
     *
     * The database is created if it did not exists.
     *
     * @param name the name of the file/directory where the actual db will be located in the Temp dir.
     * @return @throws IOException
     * @throws StorageException
     */
    protected static MetaDatabase getDatabase(final String name) throws IOException, StorageException {
        ModelConfiguration config = new ModelConfigurationImpl();
        config.setDatabasePath(TestUtils.createTempDir(name + ".metadb").getAbsolutePath());
        try {
            return new BerkeleyDatabase(config);
        } catch (StorageException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     *
     */
    @BeforeClass
    public static void setUp() throws IOException {
        initConfigurations();
    }

}
