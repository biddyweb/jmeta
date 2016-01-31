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
package org.meta.configuration;

import java.util.Properties;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;

/**
 * Base class for configuration classes based on {@link Properties}.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public abstract class PropertiesConfiguration {

    /**
     * The properties class related to the configuration file.
     */
    protected Properties properties;

    /**
     * Initializes the configuration from the configuration file. If some entries are not present, uses
     * default values instead.
     *
     * @param props The properties instance related to the configuration file.
     */
    public PropertiesConfiguration(final Properties props) {
        this.properties = props;
    }

    /**
     * Empty initialization.
     */
    public PropertiesConfiguration() {
    }

    /**
     * Initializes this configuration content with the given properties.
     */
    abstract void initFromProperties() throws InvalidConfigurationException;

    /**
     * <p>getValue</p>
     *
     * @param propKey The key in the property file to fetch.
     * @return the value associated wit the given property key or null
     */
    public final String getValue(final String propKey) {
        if (this.properties == null || !this.properties.containsKey(propKey)) {
            return null;
        }
        return this.properties.getProperty(propKey);
    }

    /**
     * <p>getShort</p>
     *
     * @param propKey The key in the property file.
     * @return the short value or null if not found.
     */
    public final Short getShort(final String propKey) {
        String val = this.getValue(propKey);

        if (val == null) {
            return null;
        }
        try {
            return Short.valueOf(val);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * <p>getInt</p>
     *
     * @param propKey The key in the property file.
     * @return the integer value or null if not found.
     */
    public final Integer getInt(final String propKey) {
        String val = this.getValue(propKey);

        if (val == null) {
            return null;
        }
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * <p>getBoolean</p>
     *
     * @param propKey The key in the property file.
     * @return the boolean value or null if not found.
     */
    public final Boolean getBoolean(final String propKey) {
        String val = this.getValue(propKey);

        if (val == null) {
            return null;
        }
        return Boolean.valueOf(val);
    }
}
