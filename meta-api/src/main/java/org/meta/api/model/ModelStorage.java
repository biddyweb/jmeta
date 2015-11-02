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
package org.meta.api.model;

import org.meta.api.common.MetHash;
import org.meta.api.storage.MetaStorage;

/**
 *
 * Base interface representing storage operations on meta model objects.
 *
 * @author nico
 */
public interface ModelStorage {

    /**
     *
     * @return The model factory.
     */
    ModelFactory getFactory();

    /**
     * Close the model and do some clean-up.
     */
    void close();

    /**
     *
     * @param hash of something you want to find in the db
     * @return A searchable object, or null if not found.
     */
    Searchable get(MetHash hash);

    /**
     *
     * @param hash the hash of a data
     * @return a Data object or null if the hash does not exists
     */
    Data getData(MetHash hash);

    /**
     *
     * @param hash the hash of a data file
     * @return a DataFile object or null if the hash does not exists or if the hash does not point to a
     * DataFile.
     */
    DataFile getDataFile(MetHash hash);

    /**
     *
     * @param hash the hash of a search
     * @return a search pointed by his hash. Return null if not found or if the hash is not pointed a
     * MetaSearch object
     */
    Search getSearch(MetHash hash);

    /**
     *
     * @param hash the hash of a searchable
     * @return the MetaSearch linked to the hash or null if not found
     */
    Searchable getSearchable(MetHash hash);

    /**
     * Delete an object in DB.
     *
     * @param searchable the object to remove from db
     * @return true on success, false otherwise
     */
    boolean remove(Searchable searchable);

    /**
     * Delete an object in DB.
     *
     * @param hash The hash to remove from db
     * @return true on success, false otherwise
     */
    boolean remove(MetHash hash);

    /**
     * Creates or updates a searchable object in database.
     *
     * All children of given object are also created/updated.
     *
     * @param searchable The object to create / update
     *
     * @return true on success, false otherwise
     */
    boolean set(Searchable searchable);

    /**
     * Creates or updates a searchable object in database.
     *
     * All children of given object are also created/updated.
     *
     * The entry will be considered invalid after timeout mili-seconds
     *
     * @param searchable The object to create / update
     * @param timeout the timeout value in ms
     *
     * @return true on success, false otherwise
     */
    boolean set(Searchable searchable, long timeout);

    /**
     * @return the backing storage used by this model.
     */
    MetaStorage getStorage();

}
