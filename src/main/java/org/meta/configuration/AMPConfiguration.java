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

import java.util.Properties;

/**
 *
 */
public final class AMPConfiguration extends BaseConfiguration {

    /**
     * The default port to listen to AMP messages.
     */
    public static final Short DEFAULT_AMP_PORT = 4242;

    /**
     * The key in configuration file for the amp port.
     */
    public static final String AMP_PORT_KEY = "ampPort";

    /**
     * The port the amp stack will listen to.
     */
    private Short ampPort = DEFAULT_AMP_PORT;

    /**
     * Empty initialization with default values.
     */
    public AMPConfiguration() {
    }

    /**
     * Initializes the AMP config from properties.
     *
     * @param properties The properties to take configuration from.
     */
    public AMPConfiguration(Properties properties) {
        super(properties);
        if (properties != null) {
            initFromProperties();
        }
    }

    @Override
    void initFromProperties() {
        Short port = this.getShort(AMP_PORT_KEY);
        if (port != null) {
            this.ampPort = port;
        }
    }

    /**
     *
     * @return The amp port
     */
    public Short getAmpPort() {
        return ampPort;
    }

    /**
     *
     * @param ampPort The new amp port
     */
    public void setAmpPort(Short ampPort) {
        this.ampPort = ampPort;
    }
}
