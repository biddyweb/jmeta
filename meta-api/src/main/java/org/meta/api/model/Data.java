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

import java.nio.ByteBuffer;
import org.meta.api.common.MetHash;

/**
 * Abstract data container.
 *
 * Contains a size, a type and a list of associated meta-data.
 *
 * @author Thomas LAVOCAT
 * @version $Id: $
 */
public abstract class Data extends Searchable {

    /**
     * The size of this data.
     */
    protected int size;

    /**
     * The type of this Data object.
     */
    protected DataType type;

    /**
     *
     */
    protected MetaDataMap metaDataMap;

    /**
     * Instantiate Data, with empty initial content and metaDataMap.
     *
     * @param hash the hash of the data
     */
    public Data(final MetHash hash) {
        super(hash);
        metaDataMap = new MetaDataMap();
    }

    /**
     * As metaDataMap does not count in the hash calculation, this method is public and can be called by
     * anyone.
     *
     * @param map the map of MetaData representing complementary information about the data.
     *
     * This can be a title, a comment, anything really...
     */
    public final void setMetaData(final MetaDataMap map) {
        this.metaDataMap = map;
    }

    /**
     * <p>Getter for the field <code>metaDataMap</code>.</p>
     *
     * @return the metaDataMap of the Data
     */
    public final MetaDataMap getMetaDataMap() {
        return metaDataMap;
    }

    /**
     * <p>getMetaData</p>
     *
     * @param key the key of the meta data to retrieve
     * @return the meta data object for the given key, if any, or null
     */
    public final MetaData getMetaData(final String key) {
        return this.metaDataMap.getMetaData(key);
    }

    /**
     * Add the given meta data to the internal Set.
     *
     * @param property the meta-data
     */
    public final void addMetaData(final MetaData property) {
        this.metaDataMap.put(property.getKey(), property.getValue());
    }

    /**
     * Add the given key/value pair to the internal meta data.
     *
     * @param key the meta data key
     * @param value the meta data value
     */
    public final void addMetaData(final String key, final String value) {
        this.metaDataMap.put(key, value);
    }

    /**
     * Add the given Set of meta-data to the internal Set.
     *
     * @param properties the Set of meta-data to add
     */
//    public final void addMetaData(final Set<MetaData> properties) {
//        this.metaDataMap.addAll(properties);
//    }
    /**
     * <p>Getter for the field <code>size</code>.</p>
     *
     * @return the size of this data
     */
    public int getSize() {
        return size;
    }

    /**
     * <p>Setter for the field <code>size</code>.</p>
     *
     * @param dataSize the size of this data
     */
    public void setSize(final int dataSize) {
        this.size = dataSize;
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return The type of this data object
     */
    public DataType getType() {
        return type;
    }

    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param dataType The type of this data object
     */
    public void setType(final DataType dataType) {
        this.type = dataType;
    }

    /**
     * Copies the internal data to a byte array.
     *
     * @return the internal data as a byte array
     */
    public abstract byte[] getBytes();

    /**
     * Retrieve the internal data as a {@link ByteBuffer}.
     *
     * The returned buffer is usable as-is, and is read-only and its position will be zero.
     *
     * @return the {@link ByteBuffer}.
     */
    public abstract ByteBuffer getBuffer();

    /**
     * {@inheritDoc}
     *
     * Convert the underlying data to a String.
     *
     * Internal data will be decoded from UTF-8.
     *
     * If underlying data does not contains a valid UTF-8 sequence, the content of the returned string is
     * undefined.
     */
    @Override
    public abstract String toString();

}
