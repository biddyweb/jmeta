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
 * @author nico
 */
public final class PluginConfigurationImpl extends PropertiesConfiguration {

    /**
     * The entry key in the properties files for load directory.
     */
    public static final String LOAD_DIRECTORY_KEY = "loadDirectory";

    /**
     * The default plugins to exclude.
     */
    public static final String[] DEFAULT_EXCLUDE = null;

    /**
     * The entry key in the properties files for exclude.
     */
    public static final String EXCLUDE_KEY = "exclude";

    private String loadDirectory = null;
    private String[] excludes = null;

    /**
     * @param properties the properties to init from.
     * @throws InvalidConfigurationException if an invalid configuration entry is encountered.
     */
    public PluginConfigurationImpl(final Properties properties) throws InvalidConfigurationException {
        super(properties);
        initFromProperties();
    }

    @Override
    void initFromProperties() throws InvalidConfigurationException {
        String excludeVal = this.getValue(EXCLUDE_KEY);
        if (excludeVal != null) {
            this.excludes = ConfigurationUtils.asList(excludeVal);
        }
        String loadDir = this.getValue(LOAD_DIRECTORY_KEY);
        if (loadDir == null) {
            throw new InvalidConfigurationException("The entry '" + LOAD_DIRECTORY_KEY
                    + "' is mandatory for plugins configuration");
        }
        this.loadDirectory = loadDir;
    }

    /**
     * @return the plugins load directory
     */
    public String getLoadDirectory() {
        return loadDirectory;
    }

    /**
     * @param loadDir the new plugins loading directory.
     */
    public void setLoadDirectory(final String loadDir) {
        this.loadDirectory = loadDir;
    }

    /**
     *
     * @return the list of plugin names to exclude from loading.
     */
    public String[] getExcludes() {
        return excludes;
    }

    /**
     * @param excludedPlugins the new plugin names to exclude from loading.
     */
    public void setExcludes(final String[] excludedPlugins) {
        this.excludes = excludedPlugins;
    }

}
