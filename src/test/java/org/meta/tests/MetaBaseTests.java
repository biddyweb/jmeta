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
package org.meta.tests;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import org.junit.BeforeClass;
import org.meta.configuration.AMPConfiguration;
import org.meta.configuration.DHTConfiguration;
import org.meta.configuration.MetaConfiguration;
import org.meta.configuration.ModelConfiguration;
import org.meta.configuration.WSConfiguration;

/**
 * Base class for tests to pre-configure the JMETA env
 */
public abstract class MetaBaseTests {

    public static void initConfigurations() {
        //Init all configs with default values manually
        MetaConfiguration.setAmpConfiguration(new AMPConfiguration());
        MetaConfiguration.setDhtConfiguration(new DHTConfiguration());
        MetaConfiguration.setWSConfiguration(new WSConfiguration());
        MetaConfiguration.setModelConfiguration(new ModelConfiguration());
    }

    @BeforeClass
    public static void setUp() {
        initConfigurations();
    }

    /**
     * Look for a usable network interface address to use for tests. It must be
     * routable (even locally). Falls back to 'localhost'
     *
     * //TODO better addr choice (no more randomness...)
     *
     * @return The local inetAddress for tests
     *
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static InetAddress getLocalAddress() throws UnknownHostException, SocketException {
//        InetAddress localAddr = null;
//        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//
//        for (NetworkInterface netIf : Collections.list(networkInterfaces)) {
//            if (netIf.isUp()) {
//                for (InetAddress ifAddr : Collections.list(netIf.getInetAddresses())) {
//                    if (ifAddr instanceof Inet4Address) {
//                        //We prefer ipv4 for tests...
//                        localAddr = ifAddr;
//                        break;
//                    }
//                }
//                if (localAddr != null) {
//                    break;
//                }
//            }
//        }
//        if (localAddr == null) {
//            localAddr = InetAddress.getByName("localhost");
//        }
        return InetAddress.getByName("127.0.0.1");
        //return localAddr;
    }
}
