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

import org.meta.controler.Controler;
import org.meta.dht.BootstrapOperation;
import org.meta.configuration.MetaConfiguration;
import org.meta.configuration.exceptions.InvalidConfigurationFileException;
import org.meta.dht.MetaDHT;
import org.meta.dht.exceptions.BootstrapException;
import org.meta.model.exceptions.ModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just the main class.
 */
public class JMeta {

    private static final Logger logger = LoggerFactory.getLogger(JMeta.class);

    private static void initDht() throws BootstrapException {
        MetaDHT dht = MetaDHT.getInstance();
        dht.setConfiguration(MetaConfiguration.getDHTConfiguration());

        try {
            dht.start();
        } catch (IOException ex) {
            logger.error("DHT failed to start, exiting.", ex);
            System.exit(1);
        }

        BootstrapOperation bootstrapOperation = dht.bootstrap();
        //Wait for boostraping to finish.
        bootstrapOperation.awaitUninterruptibly();
        if (bootstrapOperation.isFailure()) {
            throw new BootstrapException("Bootstrap operation failed");
        }
    }

    public static void main(String[] args) {

        try {
            MetaConfiguration.initConfiguration();
        } catch (InvalidConfigurationFileException ex) {
            logger.error("Failed to initialize configuration from files.", ex);
            System.exit(1);
        }

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
