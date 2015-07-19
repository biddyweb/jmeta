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
package org.meta.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import org.meta.api.common.Identity;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.configuration.DHTConfiguration;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;
import org.meta.api.dht.MetaPeer;

/**
 *
 * Class holding general configuration entries for the DHT.
 *
 */
public final class DHTConfigurationImpl extends PropertiesConfiguration implements DHTConfiguration {

    /**
     * The default DHT port.
     */
    public static final Short DEFAULT_DHT_PORT = 15000;

    /**
     * The key in configuration file for the list of known peers.
     */
    public static final String DHT_KNOWN_PEERS_KEY = "dhtKnownPeers";

    /**
     * The key in configuration file for the DHT identity.
     */
    public static final String DHT_IDENTITY_KEY = "dhtIdentity";

    /**
     * The key in configuration file for the bootstrap broadcast.
     */
    public static final String DHT_BOOTSTRAP_BROADCAST_KEY = "dhtBootstrapBroadcast";

    /**
     * The key in configuration file for the bootstrap broadcast.
     */
    public static final String DHT_LOCAL_ONLY_KEY = "dhtLocalOnly";

    /**
     * Our identity (hash) on the DHT.
     */
    private Identity identity = new Identity(MetamphetUtils.createRandomHash());

    /**
     * The list of known peers in the DHT to help us bootstrap.
     */
    private Collection<MetaPeer> knownPeers = new ArrayList<>();

    /**
     * If we broadcast to bootstrap or not.
     */
    private boolean bootstrapBroadcast = false;

    /**
     * If we only listen to local peers.
     */
    private boolean dhtLocalOnly = true;

    /**
     * The network configuration for the DHT.
     */
    private NetworkConfigurationImpl networkConfig;

    /**
     * Empty initialization with default values
     */
    public DHTConfigurationImpl() {
        this.networkConfig = new NetworkConfigurationImpl(DEFAULT_DHT_PORT, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    /**
     * Initializes the dht config from properties.
     *
     * @param properties
     * @throws
     * org.meta.api.configuration.exceptions.InvalidConfigurationException
     */
    public DHTConfigurationImpl(Properties properties) throws InvalidConfigurationException {
        super(properties);
        this.networkConfig = new NetworkConfigurationImpl(properties);
        if (properties != null) {
            initFromProperties();
        }
    }

    @Override
    public void initFromProperties() throws InvalidConfigurationException {
        this.networkConfig.initFromProperties();

        String id = this.getValue(DHT_IDENTITY_KEY);
        if (id != null) {
            this.identity = new Identity(id);
        }

        Boolean broacast = this.getBoolean(DHT_BOOTSTRAP_BROADCAST_KEY);
        if (broacast != null) {
            this.bootstrapBroadcast = broacast;
        }

        Boolean localOnly = this.getBoolean(DHT_LOCAL_ONLY_KEY);
        if (localOnly != null) {
            this.dhtLocalOnly = localOnly;
        }

        String peersString = this.getValue(DHT_KNOWN_PEERS_KEY);
        if (peersString != null) {
            this.knownPeers = ConfigurationUtils.peersFromString(peersString);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Collection<MetaPeer> getKnownPeers() {
        return knownPeers;
    }

    /**
     *
     * @param knwonPeers
     */
    @Override
    public void setKnwonPeers(Collection<MetaPeer> knwonPeers) {
        this.knownPeers = knwonPeers;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isBootstrapBroadcast() {
        return bootstrapBroadcast;
    }

    /**
     *
     * @param bootstrapBroadcast
     */
    @Override
    public void setBootstrapBroadcast(boolean bootstrapBroadcast) {
        this.bootstrapBroadcast = bootstrapBroadcast;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDhtLocalOnly() {
        return dhtLocalOnly;
    }

    /**
     *
     * @param dhtLocalOnly
     */
    @Override
    public void setDhtLocalOnly(boolean dhtLocalOnly) {
        this.dhtLocalOnly = dhtLocalOnly;
    }

    /**
     *
     * @return
     */
    @Override
    public Identity getIdentity() {
        return identity;
    }

    /**
     *
     * @param identity
     */
    @Override
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    /**
     *
     * @return
     */
    @Override
    public NetworkConfiguration getNetworkConfig() {
        return networkConfig;
    }

    /**
     *
     * @param networkConfig
     */
    @Override
    public void setNetworkConfig(NetworkConfiguration networkConfig) {
        this.networkConfig = (NetworkConfigurationImpl) networkConfig;
    }

}
