package org.meta.plugin.webservice;

import java.util.HashMap;
import java.util.Iterator;

import org.bson.types.BasicBSONList;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.meta.api.ws.AbstractPluginWebServiceControler;

import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;

import org.meta.configuration.WSConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton who's launch the web server
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
     * @param config
     */
    public WebServiceReader(WSConfigurationImpl config) {
        this.configuration = config;
        mapPlugins = new HashMap<String, AbstractPluginWebServiceControler>();
    }

    /**
     * return the plugin pointed by the given parameters
     *
     * @param pluginName
     * @return the plugin if found, null otherwise
     */
    public AbstractPluginWebServiceControler getPlugin(String pluginName) {
        return mapPlugins.get(pluginName);
    }

    @Override
    public void run() {
        //Launch the server on the right port
        logger.info("Web server listening on port " + this.configuration.getWsPort());
        server = new Server(this.configuration.getWsPort());

        // serve statics files within 'static' directory
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        //give a way to serve the web site part
        resource_handler.setResourceBase("static");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
            resource_handler,
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
     * Register a plugin pointed by plugin name
     *
     * @param pluginName plugin name
     * @param abstractPluginWebServiceControler plugin webservice controler
     */
    public void registerPlugin(String pluginName,
            AbstractPluginWebServiceControler abstractPluginWebServiceControler) {
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
        ObjectSerializer json_serializer = JSONSerializers.getStrict();
        return json_serializer.serialize(list);
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
