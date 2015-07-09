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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.meta.configuration.exceptions.InvalidConfigurationException;
import org.meta.configuration.exceptions.InvalidConfigurationFileException;
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
     * The class responsible for the dht configuration.
     */
    private static DHTConfiguration dhtConfiguration;

    /**
     * The class responsible for the web service configuration.
     */
    private static WSConfiguration wsConfiguration;

    /**
     * The class responsible for the amp stack configuration.
     */
    private static AMPConfiguration ampConfiguration;

    /**
     * The class responsible for the model configuration.
     */
    private static ModelConfiguration modelConfiguration;

    /**
     * Default private constructor
     */
    private MetaConfiguration() {
    }

    /* 
     * Initializes all configurations.
     *
     * @throws java.io.IOException
     */
    public static void initConfiguration() throws InvalidConfigurationFileException, InvalidConfigurationException {

        try {
            Properties dhtProps = createProperties(DHT_CONFIG_PATH);
            dhtConfiguration = new DHTConfiguration(dhtProps);

            Properties wsProps = createProperties(WS_CONFIG_PATH);
            wsConfiguration = new WSConfiguration(wsProps);

            Properties ampProps = createProperties(AMP_CONFIG_PATH);
            ampConfiguration = new AMPConfiguration(ampProps);

            Properties modelProps = createProperties(MODEL_CONFIG_PATH);
            modelConfiguration = new ModelConfiguration(modelProps);
        } catch (IOException ex) {
            throw new InvalidConfigurationFileException(ex);
        }
    }

    /**
     * @return the DHT configuration object
     */
    public static DHTConfiguration getDHTConfiguration() {
        return dhtConfiguration;
    }

    /**
     * @return The web service configuration object.
     */
    public static WSConfiguration getWSConfiguration() {
        return wsConfiguration;
    }

    /**
     * @return The amp stack configuration object.
     */
    public static AMPConfiguration getAmpConfiguration() {
        return ampConfiguration;
    }

    /**
     * @return The model configuration object.
     */
    public static ModelConfiguration getModelConfiguration() {
        return modelConfiguration;
    }

    /**
     *
     * @param dhtConfiguration
     */
    public static void setDhtConfiguration(DHTConfiguration dhtConfiguration) {
        MetaConfiguration.dhtConfiguration = dhtConfiguration;
    }

    /**
     *
     * @param wSConfiguration
     */
    public static void setWSConfiguration(WSConfiguration wSConfiguration) {
        MetaConfiguration.wsConfiguration = wSConfiguration;
    }

    /**
     *
     * @param ampConfiguration
     */
    public static void setAmpConfiguration(AMPConfiguration ampConfiguration) {
        MetaConfiguration.ampConfiguration = ampConfiguration;
    }

    /**
     * 
     * @param modelConfiguration 
     */
    public static void setModelConfiguration(ModelConfiguration modelConfiguration) {
        MetaConfiguration.modelConfiguration = modelConfiguration;
    }

    /**
     * @param propertiesPath The path to create the properties from.
     * @return The created Properties object
     *
     * @throws FileNotFoundException If invalid path given
     * @throws IOException If a file error occur
     */
    private static Properties createProperties(String propertiesPath) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(propertiesPath);
        Properties newProperties = new Properties();
        newProperties.load(fis);
        return newProperties;
    }
}
