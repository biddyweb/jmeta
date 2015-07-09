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
import org.meta.configuration.exceptions.InvalidConfigurationException;

/**
 * Class representing networking configuration parameters such as a port, listen
 * interfaces or addresses.
 */
public class NetworkConfiguration extends BaseConfiguration {

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
     * Initializes this network configuration with given values.
     *
     * @param port
     * @param interfaces
     * @param addresses
     */
    public NetworkConfiguration(Short port, Collection<String> interfaces, Collection<InetAddress> addresses) {
        this.port = port;
        this.interfaces = interfaces;
        this.addresses = addresses;
    }

    /**
     * Initializes this network configuration with the given Properties.
     *
     * @param props The Properties from which to take values from.
     */
    public NetworkConfiguration(Properties props) {
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
    }

    /**
     *
     * @return
     */
    public Collection<String> getInterfaces() {
        return interfaces;
    }

    /**
     * @param interfaces
     */
    public void setInterfaces(Collection<String> interfaces) {
        this.interfaces = interfaces;
    }

    /**
     * @return
     */
    public Collection<InetAddress> getAddresses() {
        return addresses;
    }

    /**
     *
     * @param addresses
     */
    public void setAddresses(Collection<InetAddress> addresses) {
        this.addresses = addresses;
    }

    /**
     *
     * @return
     */
    public Short getPort() {
        return port;
    }

    /**
     *
     * @param port
     */
    public void setPort(Short port) {
        this.port = port;
    }
}
