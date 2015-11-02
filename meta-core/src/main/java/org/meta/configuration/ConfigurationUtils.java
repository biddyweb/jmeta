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
package org.meta.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.meta.api.common.MetaPeer;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;
import org.meta.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General utility class for configuration parsing.
 */
public final class ConfigurationUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);

    private static final String LOOPBACK = NetworkUtils.getLoopbackInterface();

    /**
     * Hidden default constructor.
     */
    private ConfigurationUtils() {
    }

    /**
     * Explode the given string using the ',' delimiter.
     *
     * @param value The input string to split.
     *
     * @return The exploded parts.
     */
    public static String[] asList(final String value) {
        return value.split(",");
    }

    /**
     * Utility function to create peers from a string representation.
     *
     * For now the following formats are supported :
     *
     * <ul>
     * <li>ip:port[,coma-separated list]</li>
     * <li>hostname:port[,coma-separated list]</li>
     * </ul>
     *
     * @param peersString The string to extract peers from.
     * @return The collection of {@link MetaPeer} extracted from the given string representation
     *
     * //@throws InvalidConfigurationException if an invalid ip/host is given.
     */
    public static Collection<MetaPeer> peersFromString(final String peersString) {
        Collection<MetaPeer> peers = new ArrayList<>();
        String[] knownPeersStringList = asList(peersString);

        for (String peerString : knownPeersStringList) {
            String[] peerInfo = peerString.split(":");
            if (peerInfo.length != 2) {
                logger.warn("Invalid peer specified in config: " + peersString);
                continue;
            }
            try {
                InetAddress addr = InetAddress.getByName(peerInfo[0]);
                short peerPort = Short.valueOf(peerInfo[1]);
                peers.add(new MetaPeer(null, addr, peerPort));
            } catch (UnknownHostException ex) {
                //Do not throw if invalid peer
            }
        }
        return peers;
    }

    /**
     * Parse a list of network interface from a string.
     *
     * @param interfacesString The input string
     *
     * @return The list of interfaces found in the string.
     * @throws InvalidConfigurationException if an invalid interface is encountered.
     */
    public static Collection<String> interfacesFromString(final String interfacesString)
            throws InvalidConfigurationException {
        Collection<String> interfaces = new ArrayList<>();
        String[] ifs = asList(interfacesString);

        for (String iface : ifs) {
            try {
                if (NetworkInterface.getByName(iface) == null) {
                    throw new InvalidConfigurationException("Invalid interface specified in network config.");
                }
                interfaces.add(iface);
            } catch (SocketException ex) {
                throw new InvalidConfigurationException("Invalid interface specified in network config.", ex);
            }
        }
//        if (!interfaces.isEmpty() && !interfaces.contains(LOOPBACK)) {
//            //Always add binding to loopback.
//            //Even windows has loopback, houray!
//            interfaces.add(LOOPBACK);
//        }
        return interfaces;
    }

    /**
     * Parse a list of network bindable addresses from a string.
     *
     * @param addressesString The input string
     *
     * @return The list of addresses found in the string.
     *
     * @throws org.meta.api.configuration.exceptions.InvalidConfigurationException if an invalid address or
     * hostname is encountered.
     */
    public static Collection<InetAddress> addressesFromString(final String addressesString)
            throws InvalidConfigurationException {
        Collection<InetAddress> addresses = new ArrayList<>();
        String[] addrs = asList(addressesString);
        for (String addrStr : addrs) {
            try {
                InetAddress addr = InetAddress.getByName(addrStr);
                addresses.add(addr);
            } catch (UnknownHostException ex) {
                throw new InvalidConfigurationException("Invalid address specified in configuration.", ex);
            }
        }
        return addresses;
    }

    /**
     * @param propertiesPath The path to create the properties from.
     * @return The created Properties object
     *
     * @throws FileNotFoundException If invalid path given
     * @throws IOException If a file error occur
     */
    public static Properties createProperties(final String propertiesPath)
            throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(propertiesPath);
        Properties newProperties = new Properties();
        newProperties.load(fis);
        return newProperties;
    }

}
