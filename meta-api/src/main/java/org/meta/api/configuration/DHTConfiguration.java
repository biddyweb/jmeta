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
package org.meta.api.configuration;

import java.util.Collection;
import org.meta.api.common.Identity;
import org.meta.api.dht.MetaPeer;

/**
 * Interface for DHT configuration entries.
 *
 * @author nico
 */
public interface DHTConfiguration {

    /**
     * @return The configured {@link Identity}.
     */
    Identity getIdentity();

    /**
     * @return A Collection of known peers.
     */
    Collection<MetaPeer> getKnownPeers();

    /**
     * @return The {@link NetworkConfiguration} for the DHT.
     */
    NetworkConfiguration getNetworkConfig();

    /**
     * TODO remove and always broadcast ?
     *
     * @return if broadcasting to find peers.
     */
    boolean isBootstrapBroadcast();

    /**
     * @return true if the DHT must deal only witch link-local addresses, false otherwise.
     */
    boolean isDhtLocalOnly();

    /**
     * @param bootstrapBroadcast boolean
     */
    void setBootstrapBroadcast(boolean bootstrapBroadcast);

    /**
     * @param dhtLocalOnly the new value for local-only dht mode.
     */
    void setDhtLocalOnly(boolean dhtLocalOnly);

    /**
     *
     * @param identity the identity used by our peer on the DHT.
     */
    void setIdentity(Identity identity);

    /**
     * @param knwonPeers a collection of known peers.
     */
    void setKnwonPeers(Collection<MetaPeer> knwonPeers);

    /**
     *
     * @param networkConfig the backend network configuration for the DHT.
     */
    void setNetworkConfig(NetworkConfiguration networkConfig);

}
