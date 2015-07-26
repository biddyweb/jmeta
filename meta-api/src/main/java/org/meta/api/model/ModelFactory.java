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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * This factory allows the manipulation of model objects easier.
 *
 * It contains utility methods to create model objects and instance pools.
 *
 */
public class ModelFactory {

    private final Map<ModelType, InstancePool> pools;

    /**
     * Default constructor.
     */
    public ModelFactory() {
        pools = new HashMap<>();
        for (ModelType type : ModelType.values()) {
            pools.put(type, new InstancePool(type.getTypeClass()));
        }
    }

    /**
     *
     * @param type The model type to get
     *
     * @return An instance of the given type
     */
    public final Searchable getInstance(final ModelType type) {
        return pools.get(type).getInstance();
    }

    /**
     *
     * @return a fresh search from pool
     */
    protected final Search getSearch() {
        return (Search) pools.get(ModelType.SEARCH).getInstance();
    }

    /**
     * Create a Search with given parameters.
     *
     * @param source search's source
     * @param metaData search's metaData
     * @param datas search's results
     *
     * @return a brand new search
     */
    public final Search createSearch(final Searchable source,
            final MetaData metaData, final List<Data> datas) {
        Search search = (Search) pools.get(ModelType.SEARCH).getInstance();
        search.setSource(source);
        search.setMetaData(metaData);
        if (datas != null) {
            search.addLinkedData(datas);
        }
        return search;
    }

    /**
     *
     * @return a fresh dataString
     */
    protected final DataString getDataString() {
        return (DataString) pools.get(ModelType.DATASTRING).getInstance();
    }

    /**
     * Build a new DataString with the given parameters.
     *
     * @param data a string containing what it please you
     *
     * @return The fully-initialized dataString
     */
    public final DataString createDataString(final String data) {
        DataString dataString = this.getDataString();
        dataString.setString(data);
        return dataString;
    }

    /**
     * @return a fresh DataFile
     */
    protected final DataFile getDataFile() {
        return (DataFile) pools.get(ModelType.DATAFILE).getInstance();
    }

    /**
     * Build a new DataString with given parameters.
     *
     * @param file The file you want DataFile to point to
     *
     * @return The fully-initialized dataFile
     */
    public final DataFile createDataFile(final File file) {
        DataFile dataFile = this.getDataFile();
        dataFile.setFile(file);
        return dataFile;
    }

    /**
     *
     * @return a fresh MetaData
     */
    protected final MetaData getMetaData() {
        return (MetaData) pools.get(ModelType.METADATA).getInstance();
    }

    /**
     * Build a MetaData with given parameters.
     *
     * @param props the MetaProperties representing the metaData
     *
     * @return The fully-initialized MetaData.
     */
    public final MetaData createMetaData(final TreeSet<MetaProperty> props) {
        MetaData metaData = this.getMetaData();
        metaData.setProperties(props);
        return metaData;
    }

    /**
     * Create a new instance of a Content type Model object have protected constructor, so you can't call a
     * clazz.newinstance on those ? extends Searchable.class ;-)
     *
     * @param clazz the class to instantiate
     * @return an instance of the given Class
     * @throws InstantiationException if invalid class
     * @throws IllegalAccessException if invalid class
     */
    public final Searchable newInstance(final Class clazz)
            throws InstantiationException, IllegalAccessException {
        return (Searchable) clazz.newInstance();
    }

    /**
     * After retrieving an search from network, you may want to give him his childs Call this method.
     *
     * TODO check that the given children have the same hash that the search want
     *
     * @param search
     * @param source
     * @param metaData
     * @param linked
     */
    public void updateFromNewtork(Search search, Searchable source, MetaData metaData, ArrayList<Data> linked) {
        search.set(source, metaData, linked);
    }
}
