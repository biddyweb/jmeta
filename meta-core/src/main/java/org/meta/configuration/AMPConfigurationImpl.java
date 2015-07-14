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

import org.meta.api.configuration.AMPConfiguration;
import java.util.Properties;
import org.meta.api.configuration.NetworkConfiguration;

/**
 * Class holding general configuration entries for the amp stack.
 */
public final class AMPConfigurationImpl extends PropertiesConfiguration implements AMPConfiguration {

    /**
     * The default size for Sender thread pool.
     */
    public static final int DEFAULT_SENDER_THREAD_POOL_SIZE = 100;

    /**
     * The key for DEFAUL_SEND_THREAD_POOL_SIZE in amp.conf.
     */
    public static final String SENDER_TH_POOL_KEY = "senderThreadPoolSize";

    /**
     * The default size for Server thread pool.
     */
    public static final int DEFAULT_SEVER_THREAD_POOL_SIZE = 100;

    /**
     * The key for DEFAUL_SEVER_THREAD_POOL_SIZE in amp.conf.
     */
    public static final String SERVER_TH_POOL_KEY = "serverThreadPoolSize";

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
    private Integer senderThPoolSize = DEFAULT_SENDER_THREAD_POOL_SIZE;
    private Integer serverThPoolSize = DEFAULT_SEVER_THREAD_POOL_SIZE;

    /**
     * The network parameters.
     */
    private NetworkConfiguration networkConfig;

    /**
     * Empty initialization with default values.
     */
    public AMPConfigurationImpl() {
    }

    /**
     * Initializes the AMP config from properties.
     *
     * @param properties The properties to take configuration from.
     */
    public AMPConfigurationImpl(Properties properties) {
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
        Integer senderThPoolSize = this.getInt(SENDER_TH_POOL_KEY);
        if (senderThPoolSize != null) {
            this.senderThPoolSize = senderThPoolSize;
        }
        Integer serverThPoolSize = this.getInt(SERVER_TH_POOL_KEY);
        if (senderThPoolSize != null) {
            this.serverThPoolSize = serverThPoolSize;
        }
    }

    /**
     *
     * @return The amp port
     */
    @Override
    public Short getAmpPort() {
        return ampPort;
    }

    /**
     *
     * @param ampPort The new amp port
     */
    @Override
    public void setAmpPort(Short ampPort) {
        this.ampPort = ampPort;
    }

    /**
     * @return the senderThPoolSize
     */
    @Override
    public Integer getSenderThPoolSize() {
        return senderThPoolSize;
    }

    /**
     * @param senderThPoolSize the senderThPoolSize to set
     */
    @Override
    public void setSenderThPoolSize(Integer senderThPoolSize) {
        this.senderThPoolSize = senderThPoolSize;
    }

    /**
     * @return the serverThPoolSize
     */
    @Override
    public Integer getServerThPoolSize() {
        return serverThPoolSize;
    }

    /**
     * @param serverThPoolSize the serverThPoolSize to set
     */
    @Override
    public void setServerThPoolSize(Integer serverThPoolSize) {
        this.serverThPoolSize = serverThPoolSize;
    }

    /**
     * 
     * @return The network configuration.
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
        this.networkConfig = networkConfig;
    }

}
