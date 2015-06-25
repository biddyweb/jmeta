/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 JMeta
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
package org.meta;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.meta.controler.Controler;
import org.meta.dht.BootstrapOperation;
import org.meta.configuration.MetaConfiguration;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;
import org.meta.model.exceptions.ModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just the main class.
 */
public class JMeta {

    private static final Logger logger = LoggerFactory.getLogger(JMeta.class);

    public static void main(String[] args) {

        try {
            MetaConfiguration.initConfiguration();
        } catch (IOException ex) {
            logger.error("Failed to initialize configuration, exiting.", ex);
            System.exit(1);
        }

        MetaDHT dht = MetaDHT.getInstance();
        dht.setConfiguration(MetaConfiguration.getDHTConfiguration());

        try {
            dht.start();
        } catch (IOException ex) {
            logger.error("DHT failed to start, exiting.", ex);
            System.exit(1);
        }

        BootstrapOperation bootstrapOperation = dht.bootstrap();
        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                logger.error("Bootstrap oeration failed, exiting.");
                System.exit(1);
            }

            @Override
            public void complete(BootstrapOperation operation) {
                for (MetaPeer peer : operation.getBootstrapTo()) {
                    logger.debug("Bootstraped to : " + peer);
                }
            }
        });

        //Wait for boostraping to finish.
        bootstrapOperation.awaitUninterruptibly();

        try {
            logger.debug("Starting controler");
            Controler controler = new Controler();
        } catch (IOException | URISyntaxException ex) {
            logger.error("Error while starting controler", ex);
            System.exit(1);
        } catch (ModelException ex) {
            logger.error("Failed to start the model", ex);
        }
    }
}
