/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Thomas LAVOCAT/Nicolas Michon/Pablo Joubert
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.controler;

import java.io.IOException;
import org.meta.api.common.exceptions.MetaException;
import org.meta.configuration.MetaConfiguration;
import org.meta.api.dht.BootstrapOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.meta.api.dht.MetaDHT;
import org.meta.dht.exceptions.BootstrapException;
import org.meta.dht.exceptions.DHTException;
import org.meta.dht.tomp2p.TomP2pDHT;
import org.meta.model.KyotoCabinetModel;
import org.meta.api.model.ModelFactory;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.tcp.AMPServer;
import org.meta.plugin.tcp.AMPWriterImpl;
import org.meta.plugin.webservice.WebServiceReader;

/**
 * TODO
 */
public class MetaController {

    private final Logger logger = LoggerFactory.getLogger(MetaController.class);

    private MetaDHT dht = null;
    private KyotoCabinetModel model = null;
    private AMPServer ampServer = null;
    private AMPWriterImpl ampWriter = null;
    private WebServiceReader wsReader = null;

    /**
     * Global meta controller constructor.
     */
    public MetaController() {
    }

    /**
     *
     */
    public void initAndStartAll() throws MetaException {
        try {
            dht = initDht();
        } catch (DHTException ex) {
            throw new MetaException("Failed to initialize DHT.", ex);
        }

        try {
            model = initModel();
        } catch (ModelException ex) {
            throw new MetaException("Failed to initialize Model.", ex);
        }

        //TODO add and check exception
        wsReader = initWebService();

        //TODO add and check exception
        ampServer = initAMPServer();

        ampWriter = initAMPWriter(model.getFactory());
    }

    /**
     * Initializes the DHT with the configuration and start bootstrap.
     *
     * @throws BootstrapException If an error occurred.
     */
    private static MetaDHT initDht() throws DHTException {

        MetaDHT dht = new TomP2pDHT(MetaConfiguration.getDHTConfiguration());

        try {
            dht.start();
        } catch (IOException ex) {
            throw new DHTException("DHT failed to start", ex);
        }

        //Maybe the boostrap should be done elsewhere ?
        BootstrapOperation bootstrapOperation = dht.bootstrap();
        //Wait for boostraping to finish.
        bootstrapOperation.awaitUninterruptibly();
        if (bootstrapOperation.isFailure()) {
            throw new BootstrapException("Bootstrap operation failed");
        }
        return dht;
    }

    /**
     * Initializes the KyotoCabinetModel.
     */
    private static KyotoCabinetModel initModel() throws ModelException {
        KyotoCabinetModel model = new KyotoCabinetModel(MetaConfiguration.getModelConfiguration());
        return model;
    }

    /**
     * Initializes the AmpServer.
     */
    private static AMPServer initAMPServer() {
        AMPServer ampServer = new AMPServer(MetaConfiguration.getAmpConfiguration());

        ampServer.start();
        return ampServer;
    }

    /**
     * Initializes the AMPWriterImpl.
     */
    private static AMPWriterImpl initAMPWriter(ModelFactory factory) {
        AMPWriterImpl writer = new AMPWriterImpl(MetaConfiguration.getAmpConfiguration(), factory);

        return writer;
    }

    /**
     * Initializes the web service server.
     */
    private static WebServiceReader initWebService() {
        WebServiceReader wsReader = new WebServiceReader(MetaConfiguration.getWSConfiguration());

        wsReader.start();
        return wsReader;
    }

    /**
     * Clean stop of controler Call kill methods on tcpReader and WebService
     * reader.
     */
    public void stop() {
        this.ampServer.kill();
        this.wsReader.kill();
        this.model.close();
    }

    public KyotoCabinetModel getModel() {
        return model;
    }

    public WebServiceReader getWebServiceReader() {
        return wsReader;
    }

    public AMPServer getAmpServer() {
        return ampServer;
    }

    public MetaDHT getDht() {
        return dht;
    }

    public AMPWriterImpl getAmpWriter() {
        return ampWriter;
    }

}
