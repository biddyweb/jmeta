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
package org.meta.api.dht;

import java.io.IOException;
import org.meta.api.common.MetHash;
import org.meta.api.configuration.DHTConfiguration;

/**
 * <p>Abstract MetaDHT class.</p>
 *
 * @author nico
 * @version $Id: $
 */
public abstract class MetaDHT {

    /**
     * The DHTConfiguration object.
     */
    protected DHTConfiguration configuration;

    /**
     * The backing storage of the dht.
     */
    //protected MetaCache storage;
    /**
     * Initialize the DHT with the given configuration and given storage.
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
     * <p>findPeers</p>
     *
     * @param hash The hash to find peers for.
     * @return The asynchronous {@link FindPeersOperation} representing the outcome of the operation.
     */
    public abstract FindPeersOperation findPeers(MetHash hash);

    /**
     * Do the actual push on the DHT.
     *
     * The hash will be valid only one hour.
     *
     * @param hash The hash to store on the DHT
     * @return The asynchronous {@link StoreOperation} representing the outcome of the operation.
     */
    public abstract StoreOperation doStore(MetHash hash);

    /**
     * Add the given hash to the list of 'push elements'. This hash will be registered and pushed regularly.
     *
     * @param hash The hash to store on the DHT.
     * @param expirationDate The date when the hash will no longer be pushed on the dht.
     */
    public abstract void push(MetHash hash, long expirationDate);

    /**
     * Push without expiration.
     *
     * @param hash The hash to store on the DHT. A pushed element will be updated every hour on the dht
     */
    public abstract void push(MetHash hash);

    /**
     * Stop the DHT, closing all sockets and connections to peers.
     */
    public abstract void close();

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
     * <p>Getter for the field <code>configuration</code>.</p>
     *
     * @return The Configuration used by this DHT instance.
     */
    public final DHTConfiguration getConfiguration() {
        return this.configuration;
    }
}
