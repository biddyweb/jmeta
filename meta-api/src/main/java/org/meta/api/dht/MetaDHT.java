/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 JMeta
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
package org.meta.api.dht;

import java.io.IOException;
import org.meta.api.common.MetHash;
import org.meta.api.configuration.DHTConfiguration;

/**
 * @author nico
 */
public abstract class MetaDHT {

    /**
     * The DHTConfiguration object.
     */
    protected DHTConfiguration configuration;

    /**
     * Initialize the DHT with the given configuration.
     *
     * @param config The dht configuration.
     */
    public MetaDHT(final DHTConfiguration config) {
        this.configuration = config;
    }

    /**
     * Initializes and starts the DHT.
     *
     * Registers our identity and starts listening for peers in the DHT.
     *
     * @throws java.io.IOException If an underlying network operation failed.
     */
    public abstract void start() throws IOException;

    /**
     * Bootstrap the DHT to find nodes.
     *
     * @return The asynchronous {@link BootstrapOperation} representing the outcome of the bootstrap.
     */
    public abstract BootstrapOperation bootstrap();

    /**
     * @param hash The hash to find peers for.
     *
     * @return The asynchronous {@link FindPeersOperation} representing the outcome of the operation.
     */
    public abstract FindPeersOperation findPeers(MetHash hash);

    /**
     * @param hash The hash to store on the DHT.
     *
     * @return The asynchronous {@link StoreOperation} representing the outcome of the operation.
     */
    public abstract StoreOperation store(MetHash hash);

    /**
     * Stop the DHT, closing all sockets and connections to peers.
     */
    public abstract void stop();

    /**
     * Set the configuration of this DHT instance.
     *
     * ! Once the DHT is started, re-defining the configuration will result in undefined behavior !
     *
     * @param config The configuration Object.
     */
    public final void setConfiguration(final DHTConfiguration config) {
        this.configuration = config;
    }

    /**
     * @return The Configuration used by this DHT instance.
     */
    public final DHTConfiguration getConfiguration() {
        return this.configuration;
    }

}
