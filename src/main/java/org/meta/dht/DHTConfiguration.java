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
package org.meta.dht;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.meta.common.Identity;
import org.meta.common.MetamphetUtils;

/**
 * Class holding general configuration entries for the DHT.
 *
 * @author nico
 */
public class DHTConfiguration {

    /**
     * The default DHT port.
     */
    public static final short DEFAULT_DHT_PORT = 15000;

    /**
     * The key in configuration file for the DHT port.
     */
    public static final String DHT_PORT_KEY = "dhtPort";

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
     * The properties class related to the configuration file.
     */
    private Properties properties;

    /**
     * Our identity (hash) on the DHT.
     */
    private Identity identity;

    /**
     * The port the DHT will listen to.
     */
    private short port;

    /**
     * The list of known peers in the DHT to help us bootstrap.
     */
    private Collection<MetaPeer> knownPeers;

    /**
     * If we broadcast to bootstrap or not.
     */
    private boolean bootstrapBroadcast;

    /**
     * If we only listen to local peers
     */
    private boolean dhtLocalOnly;

    /**
     * Initializes the configuration with default values.
     */
    public DHTConfiguration() {
        this.port = DEFAULT_DHT_PORT;
        this.identity = new Identity(MetamphetUtils.createRandomHash());
        //No known peers by default...
        this.knownPeers = new ArrayList<>();
        //No known peers so we broadcast.
        this.bootstrapBroadcast = true;
        this.dhtLocalOnly = false;
    }

    /**
     * Initializes the configuration with given parameters.
     *
     * @param id Our identity over the DHT.
     * @param port The DHT port to listen to.
     * @param knownPeers The list of known peers to bootstrap to.
     * @param broadast If we broadcast to bootstrap or not.
     */
    public DHTConfiguration(Identity id, short port, Collection<MetaPeer> knownPeers, boolean broadast, boolean localOnly) {
        this.port = port;
        this.identity = id;
        this.bootstrapBroadcast = broadast;
        this.knownPeers = knownPeers;
        this.dhtLocalOnly = localOnly;
    }

    /**
     * Initializes the configuration from the configuration file. If some
     * entries are not present, uses default values instead.
     *
     * @param prop The properties instance related to the configuration file.
     */
    public DHTConfiguration(Properties prop) {
        if (prop == null) {
            throw new NullPointerException("DHTConfiguration, can't initialize with null properties.");
        }
        this.properties = prop;
        this.initFromProperties();
    }

    private void initFromProperties() {
        if (this.properties.containsKey(DHT_PORT_KEY)) {
            this.port = Short.valueOf(this.properties.getProperty(DHT_PORT_KEY));
        } else {
            this.port = DEFAULT_DHT_PORT;
        }
        if (this.properties.containsKey(DHT_IDENTITY_KEY)) {
            this.identity = new Identity(this.properties.getProperty(DHT_IDENTITY_KEY));
        } else {
            this.identity = new Identity(MetamphetUtils.createRandomHash());
        }
        if (this.properties.containsKey(DHT_BOOTSTRAP_BROADCAST_KEY)) {
            this.bootstrapBroadcast = Boolean.valueOf(this.properties.getProperty(DHT_BOOTSTRAP_BROADCAST_KEY));
        } else {
            this.bootstrapBroadcast = true;
        }
        if (this.properties.containsKey(DHT_LOCAL_ONLY_KEY)) {
            this.dhtLocalOnly = Boolean.valueOf(this.properties.getProperty(DHT_LOCAL_ONLY_KEY));
        } else {
            this.dhtLocalOnly = false;
        }
        if (this.properties.containsKey(DHT_KNOWN_PEERS_KEY)) {
            String knownPeersString = this.properties.getProperty(DHT_KNOWN_PEERS_KEY);
            try {
                this.knownPeers = DHTConfiguration.peersFromString(knownPeersString);
            } catch (UnknownHostException ex) {
                this.knownPeers = new ArrayList<>();
            }
        } else {
            this.knownPeers = new ArrayList<>();
        }
    }

    /**
     * Utility function to create peers from a string representation.
     *
     * For now following formation is supported :
     * <ul>
     * <li>ip:port[,coma-separated list]</li>
     * <li>hostname:port[,coma-separated list]</li>
     * </ul>
     *
     * @param peersString The string to extract peers from.
     * @return The collection of {@link MetaPeer} extracted from the given
     * string representation.
     */
    public static Collection<MetaPeer> peersFromString(String peersString) throws UnknownHostException {
        Collection<MetaPeer> peers = new ArrayList<MetaPeer>();
        String[] knownPeersStringList = peersString.split(",");
        for (String peerString : knownPeersStringList) {
            String[] peerInfo = peerString.split(":");
            if (peerInfo.length != 2) {
                continue;
            }
            InetAddress addr = InetAddress.getByName(peerInfo[0]);
            short peerPort = Short.valueOf(peerInfo[1]);
            peers.add(new MetaPeer(null, addr, peerPort));
        }
        return peers;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public Collection<MetaPeer> getKnownPeers() {
        return knownPeers;
    }

    public void setKnwonPeers(Collection<MetaPeer> knwonPeers) {
        this.knownPeers = knwonPeers;
    }

    public boolean isBootstrapBroadcast() {
        return bootstrapBroadcast;
    }

    public void setBootstrapBroadcast(boolean bootstrapBroadcast) {
        this.bootstrapBroadcast = bootstrapBroadcast;
    }

    public boolean isDhtLocalOnly() {
        return dhtLocalOnly;
    }

    public void setDhtLocalOnly(boolean dhtLocalOnly) {
        this.dhtLocalOnly = dhtLocalOnly;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

}
