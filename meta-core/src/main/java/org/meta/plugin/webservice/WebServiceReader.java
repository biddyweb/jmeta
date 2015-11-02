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
package org.meta.plugin.webservice;

import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;
import java.util.HashMap;
import java.util.Iterator;
import org.bson.types.BasicBSONList;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.configuration.WSConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The web server part.
 *
 * @author faquin
 *
 */
public class WebServiceReader extends Thread {

    private HashMap<String, AbstractPluginWebServiceControler> mapPlugins = null;
    private Server server = null;
    private final Logger logger = LoggerFactory.getLogger(WebServiceReader.class);

    private final WSConfigurationImpl configuration;

    /**
     *
     * @param config the web service configuration
     */
    public WebServiceReader(final WSConfigurationImpl config) {
        this.configuration = config;
        mapPlugins = new HashMap<>();
    }

    /**
     * return the plugin pointed by the given parameters.
     *
     * @param pluginName the plugin name
     * @return the plugin if found, null otherwise
     */
    public AbstractPluginWebServiceControler getPlugin(final String pluginName) {
        return mapPlugins.get(pluginName);
    }

    /**
     *
     */
    @Override
    public void run() {
        //Launch the server on the right port
        //TODO get addresses from configuration and bind them all
        logger.info("Web server listening on port " + this.configuration.getWsPort());
        server = new Server(this.configuration.getWsPort());

        // serve statics files within 'static' directory
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        //give a way to serve the web site part
        resourceHandler.setResourceBase("static");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
            resourceHandler,
            new WebRequestHandler(this)
        });

        //Give the webrequestHandler to the server
        server.setHandler(handlers);
        //start and join the server
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Register a plugin pointed by the given plugin name.
     *
     * @param pluginName plugin name
     * @param abstractPluginWebServiceControler plugin webservice controler
     */
    public void registerPlugin(final String pluginName,
            final AbstractPluginWebServiceControler abstractPluginWebServiceControler) {
        mapPlugins.put(pluginName, abstractPluginWebServiceControler);
    }

    /**
     *
     * @return a plugin list as JSON
     */
    public String getPluginListAsJson() {
        BasicBSONList list = new BasicBSONList();
        for (Iterator<String> i = mapPlugins.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            list.add(key);
        }

        // Serialize BasicBSONList in JSON
        ObjectSerializer jsonSerializer = JSONSerializers.getStrict();
        return jsonSerializer.serialize(list);
    }

    /**
     * terminate the web server.
     */
    public void kill() {
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
