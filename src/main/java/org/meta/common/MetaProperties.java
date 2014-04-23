package org.meta.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 *
 * @author nico
 */
public class MetaProperties {

    private static String DEFAULT_PROPERTY_PATH = "conf/jmeta.prop";
    private static Map<String, Properties> properties;

    static {
        properties = new HashMap<String, Properties>();
        createProperties(DEFAULT_PROPERTY_PATH);
    }

    private static void createProperties(String propertiesPath) {
        try {
            FileInputStream fis = new FileInputStream(propertiesPath);
            Properties newProperties = new Properties();
            newProperties.load(fis);
            properties.put(propertiesPath, newProperties);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MetaProperties.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetaProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get a properties object for the given path. If the properties doesn't
     * already exists, create it.
     *
     * @param propertiesPath The relative path to the properties file.
     *
     * @return The properties object associated with the given path.
     */
    public static synchronized Properties get(String propertiesPath) {
        if (!properties.containsKey(propertiesPath)) {
            createProperties(propertiesPath);
        }
        return properties.get(propertiesPath);
    }

    /**
     *
     * Get the given property name from the default properties file.
     *
     * @param propertyKey The key to get.
     *
     * @return The value associated with the key or null if not found.
     */
    public static String getProperty(String propertyKey) {
        return properties.get(DEFAULT_PROPERTY_PATH).getProperty(propertyKey);
    }

    /**
     *
     * Get the given property name from the default properties file.
     *
     * @param propertyKey The key to get.
     *
     * @return The value associated with the key or defaultValue if not found.
     */
    public static String getProperty(String propertyKey, String defaultValue) {
        return properties.get(DEFAULT_PROPERTY_PATH).getProperty(propertyKey, defaultValue);
    }
}
