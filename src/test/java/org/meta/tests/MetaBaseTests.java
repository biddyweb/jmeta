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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.meta.configuration.AMPConfiguration;
import org.meta.configuration.DHTConfiguration;
import org.meta.configuration.MetaConfiguration;
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
    }

    @BeforeClass
    public static void setUp() {
        initConfigurations();
    }

    public static InetAddress getLocalAddress() throws UnknownHostException, SocketException {
        InetAddress addr = InetAddress.getLocalHost();
        return addr;
    }
}
