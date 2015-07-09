/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 JMeta
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.configuration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.meta.configuration.exceptions.InvalidConfigurationException;
import org.meta.dht.MetaPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General utility class for configuration parsing.
 */
public class ConfigurationUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);

    private static final String LOOPBACK = getLoopbackInterface();

    /**
     *
     * @return
     */
    private static String getLoopbackInterface() {
        try {
            for (NetworkInterface netIf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (netIf.isLoopback()) {
                    return netIf.getName();
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    /**
     * Utility function to create peers from a string representation.
     *
     * For now the following format is supported :
     * <ul>
     * <li>ip:port[,coma-separated list]</li>
     * <li>hostname:port[,coma-separated list]</li>
     * </ul>
     *
     * @param peersString The string to extract peers from.
     * @return The collection of {@link MetaPeer} extracted from the given
     * string representation or null if none found.
     *
     * @throws InvalidConfigurationException if an invalid ip/host is given.
     */
    public static Collection<MetaPeer> peersFromString(String peersString) throws InvalidConfigurationException {
        Collection<MetaPeer> peers = new ArrayList<>();
        String[] knownPeersStringList = peersString.split(",");

        for (String peerString : knownPeersStringList) {
            String[] peerInfo = peerString.split(":");
            if (peerInfo.length != 2) {
                logger.warn("Invalid peer specified in config..." + peersString);
                continue;
            }
            InetAddress addr;
            try {
                addr = InetAddress.getByName(peerInfo[0]);
                short peerPort = Short.valueOf(peerInfo[1]);
                peers.add(new MetaPeer(null, addr, peerPort));
            } catch (UnknownHostException ex) {
                throw new InvalidConfigurationException("Invalid ip or hostname specified in configuration", ex);
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
     * @throws org.meta.configuration.exceptions.InvalidConfigurationException
     */
    public static Collection<String> interfacesFromString(String interfacesString) throws InvalidConfigurationException {
        Collection<String> interfaces = new ArrayList<>();
        String[] ifs = interfacesString.split(",");

        for (String iface : ifs) {
            try {
                if (NetworkInterface.getByName(iface) == null) {
                    throw new InvalidConfigurationException("Unknown interface specified in network configuration.");
                }
                interfaces.add(iface);
            } catch (SocketException ex) {
                throw new InvalidConfigurationException("Unknown interface specified in network configuration.", ex);
            }
        }
        if (!interfaces.isEmpty() && !interfaces.contains(LOOPBACK)) {
            //Always add binding to loopback.
            //Even windows has loopback, houray!
            interfaces.add(LOOPBACK);
        }
        return interfaces;
    }

    /**
     * Parse a list of network bindable addresses from a string.
     *
     * @param addressesString The input string
     *
     * @return The list of addresses found in the string.
     *
     * @throws org.meta.configuration.exceptions.InvalidConfigurationException
     * if an invalid address or hostname is encountered.
     */
    public static Collection<InetAddress> addressesFromString(String addressesString) throws InvalidConfigurationException {
        Collection<InetAddress> addresses = new ArrayList<>();
        String[] addrs = addressesString.split(",");
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

}
