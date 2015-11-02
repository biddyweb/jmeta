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

import java.util.Collections;
import java.util.Properties;
import org.meta.api.configuration.NetworkConfiguration;
import org.meta.api.configuration.P2PPConfiguration;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;

/**
 * Class holding general configuration entries for the p2pp stack.
 */
public final class P2PPConfigurationImpl extends PropertiesConfiguration
        implements P2PPConfiguration {

    /**
     * The default size for Sender thread pool.
     */
    public static final int DEFAULT_SENDER_THREAD_POOL_SIZE = 100;

    /**
     * The key for DEFAUL_SEND_THREAD_POOL_SIZE in p2pp.conf.
     */
    public static final String SENDER_TH_POOL_KEY = "senderThreadPoolSize";

    /**
     * The default size for Server thread pool.
     */
    public static final int DEFAULT_SEVER_THREAD_POOL_SIZE = 100;

    /**
     * The key for DEFAUL_SEVER_THREAD_POOL_SIZE in p2pp.conf.
     */
    public static final String SERVER_TH_POOL_KEY = "serverThreadPoolSize";

    /**
     * The default port to listen to P2PP messages.
     */
    public static final Short DEFAULT_P2PP_PORT = 4002;

    /**
     * The port the amp stack will listen to.
     */
    private Integer senderThPoolSize;
    private Integer serverThPoolSize;

    /**
     * The network parameters.
     */
    private NetworkConfigurationImpl networkConfig;

    /**
     * Empty initialization with default values.
     */
    public P2PPConfigurationImpl() {
        this.serverThPoolSize = DEFAULT_SEVER_THREAD_POOL_SIZE;
        this.senderThPoolSize = DEFAULT_SENDER_THREAD_POOL_SIZE;
        this.networkConfig = new NetworkConfigurationImpl(
                DEFAULT_P2PP_PORT,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST);
    }

    /**
     * Initializes the AMP config from properties.
     *
     * @param properties The properties to take configuration from.
     * @throws InvalidConfigurationException if an invalid configuration entry is encountered
     */
    public P2PPConfigurationImpl(final Properties properties) throws InvalidConfigurationException {
        super(properties);
        this.networkConfig = new NetworkConfigurationImpl(properties);
        this.serverThPoolSize = DEFAULT_SEVER_THREAD_POOL_SIZE;
        this.senderThPoolSize = DEFAULT_SENDER_THREAD_POOL_SIZE;
        if (properties != null) {
            initFromProperties();
        }
    }

    @Override
    void initFromProperties() throws InvalidConfigurationException {
        this.networkConfig.initFromProperties();

        Integer senderThreadPoolSize = this.getInt(SENDER_TH_POOL_KEY);
        if (senderThreadPoolSize != null) {
            this.senderThPoolSize = senderThreadPoolSize;
        }
        Integer serverThreadPoolSize = this.getInt(SERVER_TH_POOL_KEY);
        if (serverThreadPoolSize != null) {
            this.serverThPoolSize = serverThreadPoolSize;
        }
    }

    /**
     * @return the senderThPoolSize
     */
    @Override
    public Integer getSenderThPoolSize() {
        return senderThPoolSize;
    }

    /**
     * @param threadPoolSize the senderThPoolSize to set
     */
    @Override
    public void setSenderThPoolSize(final Integer threadPoolSize) {
        this.senderThPoolSize = threadPoolSize;
    }

    /**
     * @return the serverThPoolSize
     */
    @Override
    public Integer getServerThPoolSize() {
        return serverThPoolSize;
    }

    /**
     * @param threadPoolSize the serverThPoolSize to set
     */
    @Override
    public void setServerThPoolSize(final Integer threadPoolSize) {
        this.serverThPoolSize = threadPoolSize;
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
     * @param nwConfig The new network configuration.
     */
    @Override
    public void setNetworkConfig(final NetworkConfiguration nwConfig) {
        this.networkConfig = (NetworkConfigurationImpl) nwConfig;
    }

}