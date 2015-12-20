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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Hybrid map/set implementation to simplify access to MetaData.
 *
 * Very simple and hybrid implementation of Map! The backing collection is an HashSet to keep keys sorted.
 *
 * Does not support null keys and access by keys is not optimized (although meta-data lists should be small
 * enough to ignore that).
 *
 * @author dyslesiq
 */
public class MetaDataMap extends AbstractMap implements Iterable<MetaData> {

    private static final int LIST_CAPACITY = 5;

    private Set<MetaData> list;

    /**
     * Default constructor.
     */
    public MetaDataMap() {
        this.list = new HashSet<>(LIST_CAPACITY);
    }

    /**
     * Default constructor.
     */
    public MetaDataMap(final Collection<MetaData> metaDatas) {
        this.list = new HashSet<>(LIST_CAPACITY);
        this.list.addAll(metaDatas);
    }

    /**
     * Copy constructor.
     */
    public MetaDataMap(final MetaDataMap map) {
        this.list = new HashSet<>(LIST_CAPACITY);
        this.list.addAll(map.list);
    }

    /**
     *
     * @return the entry set of MetaData
     */
    @Override
    public Set<MetaData> entrySet() {
        return list;
    }

    /**
     * Specialization of the underlying generic Object -> Object get() operation.
     *
     * @param key the meta data key
     * @return the meta data value if found, or null
     */
    public String get(final String key) {
        return (String) this.get((Object) key);
    }

    /**
     * Specialization of the underlying generic Object -> Object get() operation.
     *
     * @param key the meta data key
     * @return the meta data value if found, or null
     */
    public MetaData getMetaData(final String key) {
        for (MetaData md : list) {
            if (md.getKey().equals(key)) {
                return md;
            }
        }
        return null;
    }

    /**
     * Put the given MetaData to the underlying container.
     *
     * @param metaData the MetaData to add
     * @return the old
     */
    public String put(final MetaData metaData) {
        MetaData md = null;

        for (MetaData entry : this.list) {
            if (metaData.getKey().equals(entry.getKey())) {
                md = entry;
                break;
            }
        }
        String oldValue = null;
        if (md != null) {
            oldValue = md.getValue();
            md.setValue(metaData.getValue());
        } else {
            this.list.add(metaData);
        }
        return oldValue;
    }

    /**
     *
     * @param key the key of the meta data
     * @param value the meta data value
     * @return the old value, if any
     */
    public String put(final String key, final String value) {
        return put(new MetaData(key, value));
    }

    /**
     * Add all the given metaData into this map.
     *
     * @param metaDataList the list to add
     * @return true if the underlying collection changed after adding entries, false otherwise
     */
    public boolean addAll(final Collection<MetaData> metaDataList) {
        return this.list.addAll(metaDataList);
    }

    @Override
    public Iterator<MetaData> iterator() {
        return this.list.iterator();
    }
}
