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

import java.util.Properties;
import org.meta.api.configuration.ModelConfiguration;

/**
 *
 * Class holding general configuration entries for the Model.
 *
 */
public final class ModelConfigurationImpl extends PropertiesConfiguration implements ModelConfiguration {

    /**
     * The default configuration file path.
     */
    public static final String DEFAULT_DB_PATH = "db/meta.kch";

    /**
     * The key for DEFAULT_DB_PATH in model.conf.
     */
    public static final String MODEL_PATH_KEY = "databasePath";

    /**
     * The default maximum file size for the database. (in bytes)
     */
    public static final Integer DEFAULT_DB_MAX_SIZE = 1024 * 1024 * 512;

    /**
     * The key for DEFAULT_DB_MAX_SIZE in model.conf.
     */
    public static final String MODEL_MAX_SIZE_KEY = "maxDatabaseSize";

    private String databasePath = DEFAULT_DB_PATH;
    private Integer maxSize = DEFAULT_DB_MAX_SIZE;

    /**
     * Empty initialization with default values.
     */
    public ModelConfigurationImpl() {
    }

    /**
     * Initializes the Model config from properties.
     *
     * @param properties The properties to take configuration from.
     */
    public ModelConfigurationImpl(final Properties properties) {
        super(properties);
        if (properties != null) {
            initFromProperties();
        }
    }

    @Override
    void initFromProperties() {
        String dbPath = this.getValue(MODEL_PATH_KEY);
        if (dbPath != null) {
            this.databasePath = dbPath;
        }

        Integer dbMaxSize = this.getInt(MODEL_MAX_SIZE_KEY);
        if (dbMaxSize != null) {
            this.maxSize = dbMaxSize;
        }
    }

    /**
     *
     * @return the database path.
     */
    @Override
    public String getDatabasePath() {
        return databasePath;
    }

    /**
     * @param dbPath The new database path.
     */
    @Override
    public void setDatabasePath(final String dbPath) {
        this.databasePath = dbPath;
    }

    /**
     *
     * @return The database file max size.
     */
    @Override
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     *
     * @param dbMaxSize The new database file max size.
     */
    @Override
    public void setMaxSize(final Integer dbMaxSize) {
        this.maxSize = dbMaxSize;
    }
}
