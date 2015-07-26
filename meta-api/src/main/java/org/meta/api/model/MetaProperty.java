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

/**
 *
 * A MetaProperty is a key value object used in two cases :
 *
 * - add semantic to an object (MetaData)
 *
 * - add information to an object (Data)
 *
 * Since MetaProperty is not storable as is in the DB, it does not contain a hash value.
 *
 * it implements Comparable to be ordered by key:value in a TreeSet or other sorted collections.
 *
 * * @author Thomas LAVOCAT
 */
public final class MetaProperty implements Comparable<MetaProperty> {

    private String name = "";
    private String value = "";

    /**
     * @param key the key of the property
     * @param val the value of the property
     */
    public MetaProperty(final String key, final String val) {
        super();
        this.name = key;
        this.value = val;
    }

    /**
     * copy constructor.
     *
     * @param other the MetaProperty to copy
     */
    public MetaProperty(final MetaProperty other) {
        super();
        this.name = other.name;
        this.value = other.value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param key the name to set
     */
    public void setName(final String key) {
        this.name = key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param val the value to set
     */
    public void setValue(final String val) {
        this.value = val;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(final MetaProperty o) {
        //compare to another property
        // on key:value concatenation
        return (name + ":" + value).compareTo((o.name + ":" + o.value));
    }

}
