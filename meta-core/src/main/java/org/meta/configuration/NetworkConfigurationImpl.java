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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;

/**
 * Class representing networking configuration parameters such as a port, listen
 * interfaces or addresses.
 */
public class NetworkConfigurationImpl extends PropertiesConfiguration implements NetworkConfiguration {

    /**
     * The key in the configuration file for the port parameter.
     */
    public static String NETWORK_PORT_KEY = "port";

    /**
     * The key in the configuration file for the interfaces parameter.
     */
    public static String NETWORK_INTERFACES_KEY = "interfaces";

    /**
     * The key in the configuration file for the addresses parameter.
     */
    public static String NETWORK_ADDRESSES_KEY = "listen-addresses";

    /**
     * The key in the configuration file for the ipv4 parameter.
     */
    public static String IPv4_KEY = "ipV4";

    /**
     * The key in the configuration file for the ipv6 parameter.
     */
    public static String IPv6_KEY = "ipV6";

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
     * @param port
     * @param interfaces
     * @param addresses
     */
    public NetworkConfigurationImpl(Short port, Collection<String> interfaces, Collection<InetAddress> addresses) {
        this.port = port;
        this.interfaces = interfaces;
        this.addresses = addresses;
        this.ipV6 = false;
    }

    /**
     * Initializes this network configuration with the given Properties.
     *
     * @param props The Properties from which to take values from.
     */
    public NetworkConfigurationImpl(Properties props) {
        super(props);
        this.addresses = new ArrayList<>();
        this.interfaces = new ArrayList<>();
    }

    @Override
    void initFromProperties() throws InvalidConfigurationException {
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
        Boolean preferIpV6 = this.getBoolean(IPv6_KEY);
        if (preferIpV6 != null) {
            System.out.println("IPV6 enabled ? :" + preferIpV6);
            this.ipV6 = preferIpV6;
        }
        Boolean preferIpV4 = this.getBoolean(IPv4_KEY);
        if (preferIpV4 != null) {
            System.out.println("IPV4 enabled ? :" + preferIpV4);
            this.ipV4 = preferIpV4;
        }
        if (!ipV4 && !ipV6) {
            throw new InvalidConfigurationException("ipv4 and ipv6 cannot be both disabled!");
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Collection<String> getInterfaces() {
        return interfaces;
    }

    /**
     * @param interfaces
     */
    @Override
    public void setInterfaces(Collection<String> interfaces) {
        this.interfaces = interfaces;
    }

    /**
     * @return
     */
    @Override
    public Collection<InetAddress> getAddresses() {
        return addresses;
    }

    /**
     *
     * @param addresses
     */
    @Override
    public void setAddresses(Collection<InetAddress> addresses) {
        this.addresses = addresses;
    }

    /**
     *
     * @return
     */
    @Override
    public Short getPort() {
        return port;
    }

    /**
     *
     * @param port
     */
    @Override
    public void setPort(Short port) {
        this.port = port;
    }

    /**
     * @return true enabling ipV6, false otherwise.
     */
    @Override
    public boolean ipV6() {
        return ipV6;
    }

    /**
     * @param ipV6 the new ipV6 value.
     */
    @Override
    public void setIpV6(boolean ipV6) {
        this.ipV6 = ipV6;
    }

    /**
     * @return true enabling ipV6, false otherwise.
     */
    @Override
    public boolean ipV4() {
        return ipV4;
    }

    /**
     * @param ipV4 the new ipV4 value.
     */
    @Override
    public void setIpV4(boolean ipV4) {
        this.ipV4 = ipV4;
    }
}
