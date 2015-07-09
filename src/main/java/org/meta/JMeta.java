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

import org.meta.common.exceptions.MetaException;

import org.meta.controler.MetaController;
import org.meta.configuration.MetaConfiguration;
import org.meta.configuration.exceptions.InvalidConfigurationException;
import org.meta.configuration.exceptions.InvalidConfigurationFileException;
import org.meta.plugin.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just the main class.
 */
public class JMeta {

    private static final Logger logger = LoggerFactory.getLogger(JMeta.class);

    public static void main(String[] args) {

        try {
            logger.info("Reading configuration files");
            MetaConfiguration.initConfiguration();
        } catch (InvalidConfigurationFileException | InvalidConfigurationException ex) {
            logger.error("Failed to initialize configuration from files.", ex);
            System.exit(1);
        }

        logger.info("Starting META");

        MetaController controller = new MetaController();

        try {
            controller.initAndStartAll();
        } catch (MetaException ex) {
            logger.error("Failed to start JMeta!", ex);
        }

        logger.info("Loading plugins...");
        PluginLoader pluginLoader = new PluginLoader(null, controller);
        pluginLoader.initPlugins();

        logger.info("META started!");
    }
}
