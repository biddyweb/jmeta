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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;

/**
 * Class representing networking configuration parameters such as a port, listen interfaces or addresses.
 */
public class NetworkConfigurationImpl extends PropertiesConfiguration implements NetworkConfiguration {

    /**
     * The key in the configuration file for the port parameter.
     */
    public static final String NETWORK_PORT_KEY = "port";

    /**
     * The key in the configuration file for the interfaces parameter.
     */
    public static final String NETWORK_INTERFACES_KEY = "interfaces";

    /**
     * The key in the configuration file for the addresses parameter.
     */
    public static final String NETWORK_ADDRESSES_KEY = "listen-addresses";

    /**
     * The key in the configuration file for the ipv4 parameter.
     */
    public static final String IPV4_KEY = "ipV4";

    /**
     * The key in the configuration file for the ipv6 parameter.
     */
    public static final String IPV6_KEY = "ipV6";

    /**
     * The port to listen to.
     */
    private Short port;

    /**
     * The interfaces to listen to.
     */
    private Collection<String> interfaces;

    /**
     * The address to listen to.
     */
    private Collection<InetAddress> addresses;

    /**
     * If we enable ipV4 or not.
     */
    private boolean ipV4 = true;

    /**
     * If we enable ipV6 or not.
     */
    private boolean ipV6 = false;

    /**
     * Initializes this network configuration with given values.
     *
     * @param nwPort the port to bind to.
     * @param bindInterfaces a list of network interfaces to bind to.
     * @param bindAddresses a list of addresses to bind to.
     */
    public NetworkConfigurationImpl(final Short nwPort, final Collection<String> bindInterfaces,
            final Collection<InetAddress> bindAddresses) {
        this.port = nwPort;
        this.interfaces = bindInterfaces;
        this.addresses = bindAddresses;
        this.ipV6 = false;
    }

    /**
     * Initializes this network configuration with the given Properties.
     *
     * @param props The Properties from which to take values from.
     */
    public NetworkConfigurationImpl(final Properties props) {
        super(props);
        this.addresses = new ArrayList<>();
        this.interfaces = new ArrayList<>();
    }

    @Override
    final void initFromProperties() throws InvalidConfigurationException {
        Short portValue = this.getShort(NETWORK_PORT_KEY);
        if (portValue == null) {
            throw new InvalidConfigurationException("No port specified in configuration file.");
        } else {
            this.port = portValue;
        }
        String ifaces = this.getValue(NETWORK_INTERFACES_KEY);
        if (ifaces != null) {
            this.interfaces = ConfigurationUtils.interfacesFromString(ifaces);
        }
        String addrs = this.getValue(NETWORK_ADDRESSES_KEY);
        if (addrs != null) {
            this.addresses = ConfigurationUtils.addressesFromString(addrs);
        }
        Boolean preferIpV6 = this.getBoolean(IPV6_KEY);
        if (preferIpV6 != null) {
            System.out.println("IPV6 enabled ? :" + preferIpV6);
            this.ipV6 = preferIpV6;
        }
        Boolean preferIpV4 = this.getBoolean(IPV4_KEY);
        if (preferIpV4 != null) {
            System.out.println("IPV4 enabled ? :" + preferIpV4);
            this.ipV4 = preferIpV4;
        }
        if (!ipV4 && !ipV6) {
            throw new InvalidConfigurationException("ipv4 and ipv6 cannot be both disabled!");
        }
    }

    /**
     * @return the list of interfaces to bind to.
     */
    @Override
    public final Collection<String> getInterfaces() {
        return interfaces;
    }

    /**
     * @param ifaces the interfaces to bind to.
     */
    @Override
    public final void setInterfaces(final Collection<String> ifaces) {
        this.interfaces = ifaces;
    }

    /**
     * @return the list of addresses to bind to.
     */
    @Override
    public final Collection<InetAddress> getAddresses() {
        return addresses;
    }

    /**
     *
     * @param addrs the new list of addresses to bind to.
     */
    @Override
    public final void setAddresses(final Collection<InetAddress> addrs) {
        this.addresses = addrs;
    }

    /**
     * @return the port to bind to.
     */
    @Override
    public final Short getPort() {
        return port;
    }

    /**
     * @param nwPort the new port to bind to.
     */
    @Override
    public final void setPort(final Short nwPort) {
        this.port = nwPort;
    }

    /**
     * @return true enabling ipV6, false otherwise.
     */
    @Override
    public final boolean ipV6() {
        return ipV6;
    }

    /**
     * @param ipv6 the new ipV6 value.
     */
    @Override
    public final void setIpV6(final boolean ipv6) {
        this.ipV6 = ipv6;
    }

    /**
     * @return true enabling ipV6, false otherwise.
     */
    @Override
    public final boolean ipV4() {
        return ipV4;
    }

    /**
     * @param ipv4 the new ipV4 value.
     */
    @Override
    public final void setIpV4(final boolean ipv4) {
        this.ipV4 = ipv4;
    }
}
