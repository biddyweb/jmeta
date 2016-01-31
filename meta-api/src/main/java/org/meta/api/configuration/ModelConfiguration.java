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
package org.meta.api.configuration;

/**
 * <p>ModelConfiguration interface.</p>
 *
 * @author nico
 * @version $Id: $
 */
public interface ModelConfiguration {

    /**
     * <p>getDatabasePath</p>
     *
     * @return the database path.
     */
    String getDatabasePath();

    /**
     * <p>getMaxSize</p>
     *
     * @return The database file max size.
     */
    Integer getMaxSize();

    /**
     * <p>setDatabasePath</p>
     *
     * @param databasePath The new database path.
     */
    void setDatabasePath(String databasePath);

    /**
     * <p>setMaxSize</p>
     *
     * @param maxSize The new database file max size.
     */
    void setMaxSize(Integer maxSize);
    
}
