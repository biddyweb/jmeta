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
     * @return
     */
    boolean isBootstrapBroadcast();

    /**
     * TODO REMOVE
     * @return
     */
    boolean isDhtLocalOnly();

    /**
     *
     * @param bootstrapBroadcast
     */
    void setBootstrapBroadcast(boolean bootstrapBroadcast);

    /**
     *
     * @param dhtLocalOnly
     */
    void setDhtLocalOnly(boolean dhtLocalOnly);

    /**
     *
     * @param identity
     */
    void setIdentity(Identity identity);

    /**
     *
     * @param knwonPeers
     */
    void setKnwonPeers(Collection<MetaPeer> knwonPeers);

    /**
     *
     * @param networkConfig
     */
    void setNetworkConfig(NetworkConfiguration networkConfig);

}
