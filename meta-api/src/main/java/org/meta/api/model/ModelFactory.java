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

import java.io.File;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import org.meta.api.common.MetHash;

/**
 * Interface describing the model object factory.
 *
 * This is the only way to retrieve model objects instances.
 *
 * //TODO add more convenience methods here
 *
 * @author dyslesiq
 * @version $Id: $
 */
public interface ModelFactory {

    /**
     * <p>getData</p>
     *
     * @return an empty Data
     */
    Data getData();

    /**
     * Build a new Data with the given string as content.
     *
     * @param data a string containing what it pleases you
     * @return The fully-initialized Data
     */
    Data getData(final String data);

    /**
     * Build a new Data with the given buffer as content.
     *
     * @param buffer the content of the data to create
     * @return The fully-initialized Data
     */
    Data getData(final ByteBuffer buffer);

    /**
     * Fully initialized a Data with hash, content and size.
     *
     * Note that the size can be different that buffer.limit().
     *
     * @param hash the hash
     * @param buffer the content of the data to create
     * @param size the real size of the data
     * @return The fully-initialized Data
     */
    Data getData(final MetHash hash, final ByteBuffer buffer, final int size);

    /**
     * Creates a new DataFile with the given file.
     *
     * @param file the file
     * @return the DataFile
     */
    DataFile getDataFile(final File file);

    /**
     * Creates a new DataFile with the given uri pointing to a local file.
     *
     * @param uri the URI
     * @return the DataFile
     */
    DataFile getDataFile(final URI uri);

    /**
     * Creates a new DataFile from the given data, from which the URI of the file will be extracted.
     *
     * @param data the data
     * @return the DataFile
     */
    DataFile getDataFile(final Data data);

    /**
     * Creates a new DataFile from the given data, from which the URI of the file will be extracted.
     *
     * @param hash the hash of the data file
     * @param uri the uri of the file
     * @param size the size of the file
     * @return the new DataFile
     */
    DataFile getDataFile(final MetHash hash, final URI uri, final int size);

    /**
     * Build a SearchCriteria with given parameters.
     *
     * @param props the MetaProperties representing the metaData
     * @return The fully-initialized SearchCriteria.
     */
    SearchCriteria createCriteria(final Set<MetaData> props);

    /**
     * Build a SearchCriteria with given array of criterion.
     *
     * @param criteria array of criterion. Can be null or empty.
     * @return The fully-initialized SearchCriteria.
     */
    SearchCriteria createCriteria(final MetaData... criteria);

    /**
     * Create a MetaSearch with given parameters and empty results.
     *
     * @param source search's source
     * @param metaData search's metaData
     * @return a brand new search
     */
    Search createSearch(final Searchable source, final SearchCriteria metaData);

    /**
     * Create a MetaSearch with given parameters.
     *
     * @param source search's source
     * @param metaData search's metaData
     * @param datas search's results
     * @return a brand new search
     */
    Search createSearch(final Searchable source, final SearchCriteria metaData, final List<Data> datas);

    /**
     * Create a MetaSearch with given parameters.
     *
     * @param source search's source
     * @param metaData search's metaData
     * @param datas search's results
     * @return a brand new search
     */
    Search createSearch(final Searchable source, final SearchCriteria metaData, Data... datas);

}
