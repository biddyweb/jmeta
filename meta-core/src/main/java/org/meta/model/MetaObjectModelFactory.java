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
package org.meta.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.SearchCriteria;
import org.meta.api.model.Searchable;

/**
 * This factory provides convenient creation of model objects.
 *
 * It contains utility methods to create model objects and provides instance pools.
 *
 * @author nico
 * @version $Id: $
 */
public class MetaObjectModelFactory implements ModelFactory {

    /**
     * <p>getSearch</p>
     *
     * @return a fresh search from pool
     */
    protected final MetaSearch getSearch() {
        return new MetaSearch();
    }

    /** {@inheritDoc} */
    @Override
    public final MetaSearch createSearch(final Searchable source,
            final SearchCriteria criteria, final List<Data> datas) {
        return new MetaSearch(criteria, source, datas);
    }

    /** {@inheritDoc} */
    @Override
    public MetaSearch createSearch(final Searchable source, final SearchCriteria criteria) {
        return new MetaSearch(criteria, source, null);
    }

    /** {@inheritDoc} */
    @Override
    public MetaSearch createSearch(final Searchable source, final SearchCriteria criteria,
            final Data... datas) {
        MetaSearch search = new MetaSearch(criteria, source, null);

        if (datas != null) {
            for (Data data : datas) {
                if (data != null) {
                    search.addResult(data);
                }
            }
        }
        return search;
    }

    /** {@inheritDoc} */
    @Override
    public final Data getData() {
        return new GenericData();
    }

    /** {@inheritDoc} */
    @Override
    public final Data getData(final String data) {
        return new GenericData(data);
    }

    /** {@inheritDoc} */
    @Override
    public Data getData(final ByteBuffer buffer) {
        return new GenericData(buffer);
    }

    /** {@inheritDoc} */
    @Override
    public Data getData(final MetHash hash, final ByteBuffer buffer, final int size) {
        return new GenericData(hash, buffer, size);
    }

    /** {@inheritDoc} */
    @Override

    public DataFile getDataFile(final File file) {
        return new MetaFile(file);
    }

    /** {@inheritDoc} */
    @Override
    public DataFile getDataFile(final URI uri) {
        return new MetaFile(uri);
    }

    /** {@inheritDoc} */
    @Override
    public DataFile getDataFile(final Data data) {
        try {
            return new MetaFile(data);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public DataFile getDataFile(final MetHash hash, final URI uri, final int size) {
        return new MetaFile(hash, uri, size);
    }

    /**
     * <p>getCriteria</p>
     *
     * @return an empty SearchCriteria
     */
    public SearchCriteria getCriteria() {
        return new SearchCriteria();
    }

    /**
     * {@inheritDoc}
     *
     * Build a SearchCriteria with given parameters.
     */
    @Override
    public final SearchCriteria createCriteria(final Set<MetaData> props) {
        return new SearchCriteria(props);
    }

    /** {@inheritDoc} */
    @Override
    public SearchCriteria createCriteria(final MetaData... criteria) {
        SearchCriteria searchCriteria = new SearchCriteria();

        if (criteria != null) {
            searchCriteria.addCriteria(Arrays.asList(criteria));
        }
        return searchCriteria;
    }

}
