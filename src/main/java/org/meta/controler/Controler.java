package org.meta.controler;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.meta.common.MetHash;
import org.meta.common.MetaProperties;
import org.meta.model.Model;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.tcp.SingletonTCPReader;
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
 * @author Thomas LAVOCAT
 *
 */
public class Controler {

    private Model model = null;
    private String pluginsPropertiesFile = "conf/plugins.prop";
    private ArrayList<String> lstPluginsNames = null;
    private HashMap<String, AbstractPluginTCPControler> mapTCPControler = null;
    private HashMap<String, AbstractPluginWebServiceControler> mapWebServiceControler = null;
    private SingletonWebServiceReader webServiceReader = null;
    private SingletonTCPReader tcpReader = null;
    private static final Logger logger = LoggerFactory.getLogger(Controler.class);
    /**
     *
     * @throws LibraryException
     * @throws IOException
     * @throws URISyntaxException
     */
    public Controler()
            throws IOException,
            URISyntaxException {
        this.model = Model.getInstance();
        tcpReader = SingletonTCPReader.getInstance();
        tcpReader.initializePortAndRun(Integer.parseInt(MetaProperties.getProperty("port")));

        webServiceReader = SingletonWebServiceReader.getInstance();
        pluginInitialisation();
    }

    public void stop() {
        tcpReader.kill();
        webServiceReader.kill();
    }

    /**
     * TODO add custom class loader
     */
    private void pluginInitialisation() {
        logger.debug("Controler: entering pluginInitialisation");
        lstPluginsNames = new ArrayList<String>();
        mapTCPControler = new HashMap<String, AbstractPluginTCPControler>();
        mapWebServiceControler = new HashMap<String, AbstractPluginWebServiceControler>();

        Properties pluginsProperties = MetaProperties.get(pluginsPropertiesFile);
        Enumeration<Object> keys = pluginsProperties.keys();
        while (keys.hasMoreElements()) {
            String key = ((String) keys.nextElement());
            //TODO split on point &  take last element
            if (key.contains(".name")) {
                //plugin founded
                lstPluginsNames.add(pluginsProperties.getProperty(key));
                   //load TCP class
                String strTCPClass = pluginsProperties.getProperty(key.replaceAll(".name", "") + ".TCPClass");
                //load web service class
                String strWSClass = pluginsProperties.getProperty(key.replaceAll(".name", "") + ".WSClass");

                try {
                    Class<?> clazzTCP = Class.forName(strTCPClass);
                    Class<?> clazzWS = Class.forName(strWSClass);

                    //load TCPControler
                    AbstractPluginTCPControler tcpControler
                            = (AbstractPluginTCPControler) clazzTCP.newInstance();
                    //load webServiceControler
                    AbstractPluginWebServiceControler webServiceControler
                            = (AbstractPluginWebServiceControler) clazzWS.newInstance();

                    //Set parameters
                    tcpControler.setModel(model);
                    //Set parameters
                    webServiceControler.setModel(model);
                    //TODO rebirth P2PControler ?
                    webServiceControler.setTcpControler(tcpControler);
                    //init TCP and WS parts
                    tcpControler.init(key.replaceAll(".name", ""));
                    tcpReader.registerPlugin(key.replaceAll(".name", ""), tcpControler);

                    webServiceControler.init(key.replaceAll(".name", ""));
                    webServiceReader.registerPlugin(key.replaceAll(".name", ""), webServiceControler);

                } catch (ClassNotFoundException e) {
                    System.out.println("The plugin " + key + " is not available");
                    mapTCPControler.remove(key);
                    mapWebServiceControler.remove(key);
                    lstPluginsNames.remove(key);
                } catch (InstantiationException e) {
                    System.out.println(
                            "Error during instanciation of th plugin : " + key);
                    mapTCPControler.remove(key);
                    mapWebServiceControler.remove(key);
                    lstPluginsNames.remove(key);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }
}
