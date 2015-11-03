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
 * Class holding general configuration entries for the p2p stack.
 */
public final class P2PPConfigurationImpl extends PropertiesConfiguration
        implements P2PPConfiguration {

    /**
     * The default size for Sender thread pool.
     */
    public static final int DEFAULT_CLIENT_THREADS = 5;

    /**
     * The key for clientThreads in p2pp.conf.
     */
    public static final String CLIENT_THREADS_KEY = "clientThreads";

    /**
     * The default size for Server thread pool.
     */
    public static final int DEFAULT_SEVER_THREADS = 5;

    /**
     * The key for serverThreads in p2pp.conf.
     */
    public static final String SERVER_THREADS_KEY = "serverThreads";

    /**
     * The default number of maximum connections.
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 1000;

    /**
     * The key for maxConnections in p2pp.conf.
     */
    public static final String MAX_CONNECTIONS_KEY = "maxConnections";

    /**
     * The default download limit (in bytes/second).
     */
    public static final int DEFAULT_MAX_DOWN_SPEED = -1;

    /**
     * The key for DEFAULT_SEVER_THREADS in p2pp.conf.
     */
    public static final String MAX_DOWN_SPEED_KEY = "maxDownloadSpeed";

    /**
     * The default upload limit (in bytes/second).
     */
    public static final int DEFAULT_MAX_UP_SPEED = -1;

    /**
     * The key for maxUploadSpeed in p2pp.conf.
     */
    public static final String MAX_UP_SPEED_KEY = "maxUploadSpeed";

    /**
     * The default port to listen to P2PP messages.
     */
    public static final Short DEFAULT_P2PP_PORT = 4002;

    private Integer clientThreads;
    private Integer serverThreads;
    private Integer maxConnections;
    private Integer maxDownloadSpeed;
    private Integer maxUploadSpeed;

    /**
     * The network parameters.
     */
    private NetworkConfigurationImpl networkConfig;

    /**
     * Empty initialization with default values.
     */
    public P2PPConfigurationImpl() {
        this.serverThreads = DEFAULT_SEVER_THREADS;
        this.clientThreads = DEFAULT_CLIENT_THREADS;
        this.maxConnections = DEFAULT_MAX_CONNECTIONS;
        this.maxDownloadSpeed = DEFAULT_MAX_DOWN_SPEED;
        this.maxUploadSpeed = DEFAULT_MAX_UP_SPEED;
        this.networkConfig = new NetworkConfigurationImpl(
                DEFAULT_P2PP_PORT,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST);
    }

    /**
     * Initializes the P2P stack config from properties.
     *
     * @param properties The properties to take configuration from.
     * @throws InvalidConfigurationException if an invalid configuration entry is encountered
     */
    public P2PPConfigurationImpl(final Properties properties) throws InvalidConfigurationException {
        super(properties);
        this.networkConfig = new NetworkConfigurationImpl(properties);
        this.serverThreads = DEFAULT_SEVER_THREADS;
        this.clientThreads = DEFAULT_CLIENT_THREADS;
        this.maxConnections = DEFAULT_MAX_CONNECTIONS;
        this.maxDownloadSpeed = DEFAULT_MAX_DOWN_SPEED;
        this.maxUploadSpeed = DEFAULT_MAX_UP_SPEED;
        if (properties != null) {
            initFromProperties();
        }
    }

    @Override
    void initFromProperties() throws InvalidConfigurationException {
        this.networkConfig.initFromProperties();

        Integer clientPoolSize = this.getInt(CLIENT_THREADS_KEY);
        if (clientPoolSize != null) {
            this.clientThreads = clientPoolSize;
        }
        Integer serverPoolSize = this.getInt(SERVER_THREADS_KEY);
        if (serverPoolSize != null) {
            this.serverThreads = serverPoolSize;
        }
        Integer connections = this.getInt(MAX_CONNECTIONS_KEY);
        if (connections != null) {
            this.maxConnections = connections;
        }
        Integer downSpeed = this.getInt(MAX_DOWN_SPEED_KEY);
        if (downSpeed != null) {
            this.maxDownloadSpeed = downSpeed;
        }
        Integer upSpeed = this.getInt(MAX_UP_SPEED_KEY);
        if (upSpeed != null) {
            this.maxDownloadSpeed = upSpeed;
        }
    }

    /**
     * @return the clientThreads
     */
    @Override
    public Integer getClientThreads() {
        return clientThreads;
    }

    /**
     * @param threadPoolSize the clientThreads to set
     */
    @Override
    public void setClientThreads(final Integer threadPoolSize) {
        this.clientThreads = threadPoolSize;
    }

    /**
     * @return the serverThreads
     */
    @Override
    public Integer getServerThreads() {
        return serverThreads;
    }

    /**
     * @param threadPoolSize the serverThreads to set
     */
    @Override
    public void setServerThreads(final Integer threadPoolSize) {
        this.serverThreads = threadPoolSize;
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

    @Override
    public Integer getMaxConnections() {
        return this.maxConnections;
    }

    @Override
    public Integer getMaxDownloadSpeed() {
        return this.maxDownloadSpeed;
    }

    @Override
    public Integer getMaxUploadSpeed() {
        return this.maxUploadSpeed;
    }

    @Override
    public void setMaxConnections(final Integer maxConn) {
        this.maxConnections = maxConn;
    }

    @Override
    public void setMaxDownloadSpeed(final Integer maxSpeed) {
        this.maxDownloadSpeed = maxSpeed;
    }

    @Override
    public void setMaxUploadSpeed(final Integer maxSpeed) {
        this.maxUploadSpeed = maxSpeed;
    }
}
