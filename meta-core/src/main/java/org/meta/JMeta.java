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

import org.meta.api.common.exceptions.MetaException;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;
import org.meta.api.configuration.exceptions.InvalidConfigurationFileException;
import org.meta.configuration.MetaConfiguration;
import org.meta.controler.MetaController;
import org.meta.plugin.MetaPluginLoader;
import org.meta.plugin.exceptions.PluginLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just the main class.
 */
public final class JMeta {

    private static final Logger logger = LoggerFactory.getLogger(JMeta.class);

    private JMeta() {
    }

    /**
     * @param args Do we really need to describe this?
     */
    public static void main(final String[] args) {

        try {
            logger.info("Reading configuration files");
            MetaConfiguration.initConfiguration();
        } catch (InvalidConfigurationFileException | InvalidConfigurationException ex) {
            logger.error("Failed to initialize configuration from files.", ex);
            return;
        }
        logger.info("Starting JMeta");
        MetaController controller = new MetaController();
        try {
            controller.initAndStartAll();
        } catch (MetaException ex) {
            logger.error("Failed to start JMeta!", ex);
            return;
        }
        logger.info("Loading plugins...");
        MetaPluginLoader pluginLoader = new MetaPluginLoader(MetaConfiguration.getPluginsConfiguration(),
                controller);
        try {
            pluginLoader.loadPlugins();
        } catch (PluginLoadException ex) {
            logger.error("Failed to load plugins!", ex);
            return;
        }
        logger.info("JMeta started!");
    }
}
