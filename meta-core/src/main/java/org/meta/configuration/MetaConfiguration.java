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
package org.meta.configuration;

import java.io.IOException;
import java.util.Properties;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;
import org.meta.api.configuration.exceptions.InvalidConfigurationFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main configuration class, holding sub-configuration objects per
 * components (amp, webservices, model, dht).
 */
public class MetaConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MetaConfiguration.class);

    /**
     * Default dht config file path.
     */
    private static final String DHT_CONFIG_PATH = "conf/dht.conf";

    /**
     * Default web service config file path.
     */
    private static final String WS_CONFIG_PATH = "conf/ws.conf";

    /**
     * Default AMP config file path.
     */
    private static final String AMP_CONFIG_PATH = "conf/amp.conf";

    /**
     * Default Model config file path.
     */
    private static final String MODEL_CONFIG_PATH = "conf/model.conf";

    /**
     * Default Plugins config file path.
     */
    private static final String PLUGINS_CONFIG_PATH = "conf/plugins.conf";

    /**
     * The class responsible for the dht configuration.
     */
    private static DHTConfigurationImpl dhtConfiguration;

    /**
     * The class responsible for the web service configuration.
     */
    private static WSConfigurationImpl wsConfiguration;

    /**
     * The class responsible for the amp stack configuration.
     */
    private static AMPConfigurationImpl ampConfiguration;

    /**
     * The class responsible for the model configuration.
     */
    private static ModelConfigurationImpl modelConfiguration;

    /**
     * The class responsible for the plugins configuration.
     */
    private static PluginConfigurationImpl pluginsConfiguration;

    /**
     * Default private constructor
     */
    private MetaConfiguration() {
    }

    /**
     * Initializes all configurations.
     *
     * @throws InvalidConfigurationFileException
     * @throws InvalidConfigurationException
     */
    public static void initConfiguration() throws InvalidConfigurationFileException, InvalidConfigurationException {
        try {
            Properties dhtProps = ConfigurationUtils.createProperties(DHT_CONFIG_PATH);
            dhtConfiguration = new DHTConfigurationImpl(dhtProps);

            Properties wsProps = ConfigurationUtils.createProperties(WS_CONFIG_PATH);
            wsConfiguration = new WSConfigurationImpl(wsProps);

            Properties ampProps = ConfigurationUtils.createProperties(AMP_CONFIG_PATH);
            ampConfiguration = new AMPConfigurationImpl(ampProps);

            Properties modelProps = ConfigurationUtils.createProperties(MODEL_CONFIG_PATH);
            modelConfiguration = new ModelConfigurationImpl(modelProps);

            Properties pluginsProps = ConfigurationUtils.createProperties(PLUGINS_CONFIG_PATH);
            pluginsConfiguration = new PluginConfigurationImpl(pluginsProps);
        } catch (IOException ex) {
            throw new InvalidConfigurationFileException(ex);
        }
    }

    /**
     * @return the DHT configuration object
     */
    public static DHTConfigurationImpl getDHTConfiguration() {
        return dhtConfiguration;
    }

    /**
     * @return The web service configuration object.
     */
    public static WSConfigurationImpl getWSConfiguration() {
        return wsConfiguration;
    }

    /**
     * @return The amp stack configuration object.
     */
    public static AMPConfigurationImpl getAmpConfiguration() {
        return ampConfiguration;
    }

    /**
     * @return The model configuration object.
     */
    public static ModelConfigurationImpl getModelConfiguration() {
        return modelConfiguration;
    }

    /**
     * @return The Plugins configuration object.
     */
    public static PluginConfigurationImpl getPluginsConfiguration() {
        return pluginsConfiguration;
    }

    /**
     *
     * @param dhtConfiguration
     */
    public static void setDhtConfiguration(DHTConfigurationImpl dhtConfiguration) {
        MetaConfiguration.dhtConfiguration = dhtConfiguration;
    }

    /**
     *
     * @param wSConfiguration
     */
    public static void setWSConfiguration(WSConfigurationImpl wSConfiguration) {
        MetaConfiguration.wsConfiguration = wSConfiguration;
    }

    /**
     *
     * @param ampConfiguration
     */
    public static void setAmpConfiguration(AMPConfigurationImpl ampConfiguration) {
        MetaConfiguration.ampConfiguration = ampConfiguration;
    }

    /**
     *
     * @param modelConfiguration
     */
    public static void setModelConfiguration(ModelConfigurationImpl modelConfiguration) {
        MetaConfiguration.modelConfiguration = modelConfiguration;
    }

    /**
     * 
     * @param pluginsConfiguration 
     */
    public static void setPluginsConfiguration(PluginConfigurationImpl pluginsConfiguration) {
        MetaConfiguration.pluginsConfiguration = pluginsConfiguration;
    }

}
