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
import java.util.HashSet;
import java.util.Set;
import org.meta.api.common.MetHash;

/**
 * Abstract data container.
 *
 * Contains a size, a type and a list of associated meta-data.
 *
 * @author Thomas LAVOCAT
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
    protected Set<MetaData> metaData = null;

    /**
     * Instantiate Data, create an empty metaData list.
     *
     * @param hash the hash of the data
     */
    public Data(final MetHash hash) {
        super(hash);
        metaData = new HashSet<>();
    }

    /**
     * As metaData does not count in the hash calculation, this method is public and can be called by anyone.
     *
     * @param desc a Set of MetaData representing complementary information about the data.
     *
     * This may be a simple metaData, a title, a comment...
     */
    public final void setMetaData(final Set<MetaData> desc) {
        this.metaData = desc;
    }

    /**
     *
     * @return the metaData of the Data
     */
    public final Set<MetaData> getMetaData() {
        return metaData;
    }

    /**
     * Add the given meta data to the internal Set.
     *
     * @param property the meta-data
     */
    public final void addMetaData(final MetaData property) {
        this.metaData.add(property);
    }

    /**
     * Add the given Set of meta-data to the internal Set.
     *
     * @param properties the Set of meta-data to add
     */
    public final void addMetaData(final Set<MetaData> properties) {
        this.metaData.addAll(properties);
    }

    /**
     *
     * @return the size of this data
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param dataSize the size of this data
     */
    public void setSize(final int dataSize) {
        this.size = dataSize;
    }

    /**
     *
     * @return The type of this data object
     */
    public DataType getType() {
        return type;
    }

    /**
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
     * Convert the underlying data to a String.
     *
     * Internal data will be decoded from UTF-8.
     *
     * If underlying data does not contains a valid UTF-8 sequence, the content of the returned string is
     * undefined.
     *
     * @return the created String
     */
    @Override
    public abstract String toString();

}
