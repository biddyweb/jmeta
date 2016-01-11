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
package org.meta.controler;

import java.io.IOException;
import org.meta.api.common.OperationListener;
import org.meta.api.common.exceptions.MetaException;
import org.meta.api.dht.BootstrapOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.model.ModelStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.configuration.MetaConfiguration;
import org.meta.dht.DHTPushManager;
import org.meta.dht.exceptions.DHTException;
import org.meta.dht.tomp2p.TomP2pDHT;
import org.meta.executors.MetaTimedExecutor;
import org.meta.p2pp.P2PPManager;
import org.meta.p2pp.client.MetaP2PPClient;
import org.meta.p2pp.exceptions.P2PPException;
import org.meta.plugin.MetaPluginAPI;
import org.meta.plugin.webservice.MetaWebServer;
import org.meta.storage.BerkeleyDatabase;
import org.meta.storage.BerkeleyKVStorage;
import org.meta.storage.MetaModelStorage;
import org.meta.storage.exceptions.ModelException;
import org.meta.storage.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMeta controller. Instantiate all parent objects.
 */
public class MetaController {

    private final Logger logger = LoggerFactory.getLogger(MetaController.class);

    private final MetaTimedExecutor executor;

    private TomP2pDHT dht = null;

    private MetaDatabase db;

    private BerkeleyKVStorage backendStorage;

    //private MetaCache cacheStorage;
    private MetaModelStorage model;

    private MetaWebServer wsReader;

    private P2PPManager p2ppManager;

    private MetaP2PPClient p2ppClientAccessor;

    private MetaPluginAPI pluginAPI;

    private DHTPushManager pushManager;

    /**
     * Global meta controller constructor.
     */
    public MetaController() {
        this.executor = new MetaTimedExecutor();
        //Listen JVM shutdown events and close eveything properly
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close();
            }
        }));
    }

    /**
     * Schedule all tasks that need to be executed regularly.
     *
     * (Cache cleanup, DHT data announce, etc)
     */
    private void scheduleExecutorTasks() {
        //StorageExpirationTask expirationTask = new StorageExpirationTask(cacheStorage);
        //this.executor.addTask(expirationTask);
        this.executor.addTask(pushManager);
    }

    /**
     *
     * @throws MetaException if an error occur.
     */
    public void initAndStartAll() throws MetaException {
        try {
            initStorage();
        } catch (ModelException ex) {
            throw new MetaException("Failed to initialize Model.", ex);
        }
        try {
            initDht();
        } catch (DHTException ex) {
            throw new MetaException("Failed to initialize DHT.", ex);
        }
        try {
            initP2PPServer();
        } catch (P2PPException ex) {
            throw new MetaException("Failed to initialize P2PP server.", ex);
        }

        //TODO add and check exceptions
        initWebService();

        //Create the P2PPClient accessor...
        this.p2ppClientAccessor = new MetaP2PPClient(p2ppManager.getClient());

        //Create the plugin API
        this.pluginAPI = new MetaPluginAPI(this);

        //Register and start timed tasks
        scheduleExecutorTasks();
    }

    /**
     * Initializes the DHT with the configuration and start bootstrap.
     *
     */
    private void initDht() throws DHTException {
        dht = new TomP2pDHT(MetaConfiguration.getDHTConfiguration(), null);
        pushManager = new DHTPushManager(dht, db, model);
        dht.setPushManager(pushManager);
        try {
            dht.start();
        } catch (IOException ex) {
            throw new DHTException("DHT failed to start", ex);
        }

        //TODO Maybe the boostrap should be done elsewhere ? => Yes, after routing table retrieval from storage, etc...
        BootstrapOperation bootstrapOperation = dht.bootstrap();
        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(final BootstrapOperation operation) {
                logger.warn("Bootstrap operation failed. Error: " + operation.getFailureMessage());
            }

            @Override
            public void complete(final BootstrapOperation operation) {
                logger.info("DHT bootstrap complete.");
            }
        });
    }

    /**
     * Initializes storage units.
     */
    private void initStorage() throws StorageException {
        this.db = new BerkeleyDatabase(MetaConfiguration.getModelConfiguration());
        model = new MetaModelStorage(this.db);
        //cacheStorage = new MetaCacheStorage(backendStorage, 1);
    }

    /**
     * Initializes the AmpServer.
     */
    private void initP2PPServer() throws P2PPException {
        this.p2ppManager = new P2PPManager(MetaConfiguration.getP2ppConfiguration(), this.model);
        this.p2ppManager.startServer();
    }

    /**
     * Initializes the web service server.
     */
    private void initWebService() {
        wsReader = new MetaWebServer(MetaConfiguration.getWSConfiguration());
        wsReader.start();
    }

    /**
     * Clean stop of controller. Stops and closes all components.
     */
    public void close() {
        logger.debug("Enter closing method");
        if (dht != null) {
            this.dht.close();
        }
        if (p2ppManager != null) {
            this.p2ppManager.getServer().close();
        }
        if (wsReader != null) {
            this.wsReader.close();
        }
        if (model != null) {
            this.model.close();
        }
        logger.debug("Closing method done properly");
    }

    /**
     *
     * @return the instance of the model
     */
    public ModelStorage getModel() {
        return model;
    }

    /**
     *
     * @return the instance of the web service reader
     */
    public MetaWebServer getWebServiceReader() {
        return wsReader;
    }

    /**
     *
     * @return the instance of the dht
     */
    public MetaDHT getDht() {
        return dht;
    }

    /**
     *
     * @return the peer-to-peer protocol client accessor
     */
    public MetaP2PPClient getP2PPClient() {
        return p2ppClientAccessor;
    }

    /**
     *
     * @return the plugin API
     */
    public MetaPluginAPI getPluginAPI() {
        return this.pluginAPI;
    }

    /**
     *
     * @return The timed executor
     */
    public MetaTimedExecutor getExecutor() {
        return executor;
    }

}
