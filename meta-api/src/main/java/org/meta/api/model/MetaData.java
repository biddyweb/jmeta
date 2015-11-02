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

import java.util.Map;

/**
 * A MetaData is a key value object used in two cases :
 *
 * - add a search criterion
 *
 * - add information to an object (Data)
 *
 * Since MetaData is not storable as is in the DB, it does not contain a hash value.
 *
 * It implements Comparable to be ordered by key:value in a TreeSet or other sorted collections.
 *
 * @author Thomas LAVOCAT
 */
public final class MetaData implements Comparable<MetaData>, Map.Entry<String, String> {

    /**
     * Char separator between key and value.
     */
    public static final char SEPARATOR = ':';

    private String key;
    private String value;

    /**
     * Default empty constructor.
     */
    public MetaData() {
    }

    /**
     * @param k the key of the property
     * @param val the value of the property
     */
    public MetaData(final String k, final String val) {
        this.key = k;
        this.value = val;
    }

    /**
     * copy constructor.
     *
     * @param other the MetaProperty to copy
     */
    public MetaData(final MetaData other) {
        this.key = other.key;
        this.value = other.value;
    }

    /**
     * @return the key
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * @param k the key to set
     */
    public void setKey(final String k) {
        this.key = k;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(final String val) {
        String oldVal = this.value;

        this.value = val;
        return oldVal;
    }

    @Override
    public int compareTo(final MetaData o) {
        //compare to another property
        // on key:value concatenation
        int keyCompare = key.compareTo(o.key);
        if (keyCompare != 0) {
            return keyCompare;
        }
        return value.compareTo(o.value);
    }

}
