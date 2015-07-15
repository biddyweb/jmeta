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

import org.meta.api.plugin.MetaPlugin;
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.meta.api.amp.PluginAMPController;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.configuration.PluginConfigurationImpl;
import org.meta.controler.MetaController;
import org.meta.plugin.exceptions.PluginLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The META plugin load mechanism.
 *
 * Takes a directory from the configuration and tries to load plugins 
 * from all jars found.
 * TODO add exclude property.
 */
public class MetaPluginLoader {

    private final Logger logger = LoggerFactory.getLogger(MetaPluginLoader.class);

    /**
     * The plugin configuration.
     */
    private final PluginConfigurationImpl configuration;

    /**
     * The global Meta controller.
     */
    private final MetaController controller;

    /**
     * The URL/Classloader asssociation used to load plugins from jars.
     */
    private final Map<URL, ClassLoader> pluginsLoaders;

    /**
     * The list of plugins.
     */
    private final Map<String, MetaPlugin> plugins;

    /**
     * Initializes the plug-in loader.
     *
     * @param config The plugins configuration.
     * @param controller The initialized global Meta controller.
     */
    public MetaPluginLoader(PluginConfigurationImpl config, MetaController controller) {
        this.configuration = config;
        this.controller = controller;
        this.plugins = new HashMap<>();
        this.pluginsLoaders = new HashMap<>();
    }

    /**
     * List all jar files in the configured load directory. Return a Collection
     * of urls representing all valid jars found.
     */
    private Collection<URL> findPluginsJars() throws InvalidConfigurationException, MalformedURLException {
        File loadFileDir = new File(this.configuration.getLoadDirectory());

        if (!loadFileDir.isDirectory()) {
            throw new InvalidConfigurationException("Provided loadDirectory ('"
                    + this.configuration.getLoadDirectory()
                    + "') is not a directory!");
        }
        File[] files = loadFileDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (!file.isFile()) {
                    return false;
                }
                return file.getName().endsWith(".jar");
            }
        });
        if (files.length == 0) {
            throw new InvalidConfigurationException("No plugins found in loadDirectory ('"
                    + this.configuration.getLoadDirectory()
                    + "')");
        }
        ArrayList<URL> jarUrls = new ArrayList<>();
        for (File jar : files) {
            jarUrls.add(jar.toURI().toURL());
        }
        return jarUrls;
    }

    /**
     * Load and Initialize plugins.
     *
     * To install a plugin, refer to the documentation inside conf/plugin.conf.
     *
     * @throws org.meta.plugin.exceptions.PluginLoadException
     */
    public void loadPlugins() throws PluginLoadException {
        Collection<URL> jarsUrls = null;
        try {
            jarsUrls = findPluginsJars();
        } catch (InvalidConfigurationException | MalformedURLException ex) {
            throw new PluginLoadException("Failed to list plugins jars", ex);
        }
        for (URL jarUrl : jarsUrls) {
            this.loadPlugin(jarUrl);
        }
    }

    /**
     * Loads the {@link MetaPlugin} implementation from the given jar URL.
     *
     * @param jarUrl
     */
    private void loadPlugin(URL jarUrl) throws PluginLoadException {
        URLClassLoader urlCl = new URLClassLoader(new URL[]{jarUrl});
        ServiceLoader<MetaPlugin> serviceLoader = ServiceLoader.load(MetaPlugin.class, urlCl);
        MetaPlugin plugin = null;
        for (MetaPlugin p : serviceLoader) {
            if (p != null) {
                plugin = p;
                break;
            }
        }
        if (plugin == null) {
            throw new PluginLoadException("Unable to load plugin from jar: " + jarUrl.getFile());
        }
        if (this.plugins.containsKey(plugin.getName())) {
            throw new PluginLoadException("A plugin with name: '" + plugin.getName() + "' has already been loaded.");
        }
        registerPlugin(plugin);
        logger.info("Plugin successfully loaded: " + plugin.getName());
        this.plugins.put(plugin.getName(), plugin);
        //Also store URL and classloader to keep references on it.
        //Do not close classloader now as we don't know if the plugin can load more classes after its registration.
        this.pluginsLoaders.put(jarUrl, urlCl);
    }

    /**
     * Registers the given plugin to amp and web service stacks and inject any
     * required dependencies.
     *
     * @param plugin the plugin to register.
     */
    private void registerPlugin(MetaPlugin plugin) {
        PluginAMPController ampController = plugin.getAMPController();
        AbstractPluginWebServiceControler wsController = plugin.getWebServiceController();

        ampController.setModel(this.controller.getModel());
        //give a link to the model to the web service controler
        wsController.setModel(this.controller.getModel());
        wsController.setDht(this.controller.getDht());
        wsController.setAmpWriter(this.controller.getAmpWriter());
        //give a link to the tcp part of the plugin to the web
        //init AMP and WS parts
        ampController.init(plugin.getName());
        wsController.init(plugin.getName());
        //give the plugin to webservicereader and TcoReader
        this.controller.getWebServiceReader().registerPlugin(plugin.getName(), wsController);
        this.controller.getAmpServer().registerPlugin(plugin.getName(), ampController);

    }

    /**
     * @return the list of loaded plugins.
     */
    public Collection<MetaPlugin> getPlugins() {
        return plugins.values();
    }
}
