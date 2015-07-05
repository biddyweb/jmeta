package org.meta.controler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.meta.configuration.MetaProperties;
import org.meta.model.Model;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.tcp.SingletonTCPReader;
import org.meta.plugin.tcp.SingletonTCPWriter;
import org.meta.plugin.webservice.SingletonWebServiceReader;

/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Thomas LAVOCAT
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
/**
 *
 * Initialize JMeta's plugin part. Run TCPreader and Webservice
 *
 * @author Thomas LAVOCAT
 *
 */
public class Controler {

    private Model model = null;
    private String pluginsPropertiesFile = "conf/plugins.prop";
    private SingletonWebServiceReader webServiceReader = null;
    private SingletonTCPReader tcpReader = null;
    private Logger logger = LoggerFactory.getLogger(Controler.class);

    /**
     * Controler constructor. Start Model, tcpreader, webservice and initialize
     * installed plugins.
     *
     * @throws LibraryException
     * @throws IOException
     * @throws org.meta.model.exceptions.ModelException see @{@link Model}
     * @throws URISyntaxException
     */
    public Controler()
            throws IOException,
            URISyntaxException,
            ModelException {
        this.model = new Model();
        tcpReader = SingletonTCPReader.getInstance();
        tcpReader.start();

        //init SingletonTCPWriter
        SingletonTCPWriter.getInstance().setFactory(model.getFactory());

        webServiceReader = SingletonWebServiceReader.getInstance();
        pluginInitialisation();
    }

    /**
     * Clean stop of controler Call kill methods on tcpReader and WebService
     * reader.
     */
    public void stop() {
        tcpReader.kill();
        webServiceReader.kill();
        this.model.close();
    }

    /**
     * Initialize plugins.
     *
     * To install a plugin, refer to the documentation inside conf/plugin.prop
     *
     * TODO add custom class loader
     */
    private void pluginInitialisation() {
        logger.debug("Controler: entering pluginInitialisation");
        //INitialize containing lists

        //Read plugin properties file, and iterate over it
        Properties pluginsProperties = MetaProperties.get(pluginsPropertiesFile);
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
                    tcpControler.setModel(model);
                    //give a link to the model to the web service controler
                    webServiceControler.setModel(model);
                    //give a link to the tcp part of the plugin to the web 
                    //init TCP and WS parts
                    tcpControler.init(key);
                    webServiceControler.init(key);
                    //give the plugin to webservicereader and TcoReader
                    webServiceReader.registerPlugin(key, webServiceControler);
                    tcpReader.registerPlugin(key, tcpControler);
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

    public Model getModel() {
        return model;
    }
}
