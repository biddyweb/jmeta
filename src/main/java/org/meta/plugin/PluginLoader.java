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
package org.meta.plugin;

import java.util.Enumeration;
import java.util.Properties;
import org.meta.configuration.MetaProperties;
import org.meta.configuration.PluginConfiguration;
import org.meta.controler.MetaController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PluginLoader {

    private final Logger logger = LoggerFactory.getLogger(PluginLoader.class);

    /**
     * The plugin configuration.
     */
    private final PluginConfiguration configuration;

    /**
     * The global Meta controller.
     */
    private final MetaController controller;

    /**
     * Initializes the plug-in loader.
     *
     * @param config The plugins configuration.
     * @param controller The initialized global Meta controller.
     */
    public PluginLoader(PluginConfiguration config, MetaController controller) {
        this.configuration = config;
        this.controller = controller;
    }

    /**
     * Initialize plugins.
     *
     * To install a plugin, refer to the documentation inside conf/plugin.prop
     *
     * TODO read from PluginConfiguration
     * TODO add custom class loader
     */
    public void initPlugins() {
        logger.debug("Controler: entering pluginInitialisation");
        //INitialize containing lists

        //Read plugin properties file, and iterate over it
        Properties pluginsProperties = MetaProperties.get("conf/plugins.prop");
        Enumeration<Object> keys = pluginsProperties.keys();

        while (keys.hasMoreElements()) {
            //Look for the next plugin name wich is contained in KEY.name: KEY
            String key = ((String) keys.nextElement());
            if (key.contains(".name")) {
                key = key.replaceAll(".name", "");
                //plugin founded
                //retrieve TCP class
                String strTCPClass = pluginsProperties.getProperty(key + ".TCPClass");
                //retrieve web service class
                String strWSClass = pluginsProperties.getProperty(key + ".WSClass");
                /*
                 * Try to load the plugin, fail proof code.
                 */
                try {
                    //TODO custom class loader ?
                    Class<?> clazzTCP = Class.forName(strTCPClass);
                    Class<?> clazzWS = Class.forName(strWSClass);
                    //load TCPControler
                    AbstractPluginTCPControler tcpControler
                            = (AbstractPluginTCPControler) clazzTCP.newInstance();
                    //load webServiceControler
                    AbstractPluginWebServiceControler webServiceControler
                            = (AbstractPluginWebServiceControler) clazzWS.newInstance();

                    //give a link to the model to the TCP controler
                    tcpControler.setModel(this.controller.getModel());
                    //give a link to the model to the web service controler
                    webServiceControler.setModel(this.controller.getModel());
                    webServiceControler.setDht(this.controller.getDht());
                    webServiceControler.setAmpWriter(this.controller.getAmpWriter());
                    //give a link to the tcp part of the plugin to the web 
                    //init TCP and WS parts
                    tcpControler.init(key);
                    webServiceControler.init(key);
                    //give the plugin to webservicereader and TcoReader
                    this.controller.getWebServiceReader().registerPlugin(key, webServiceControler);
                    this.controller.getAmpServer().registerPlugin(key, tcpControler);
                } catch (ClassNotFoundException e) {
                    logger.error("The plugin " + key + " is not available", e);
                } catch (InstantiationException e) {
                    logger.error(
                            "Error during instanciation of th plugin : " + key,
                            e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

}
