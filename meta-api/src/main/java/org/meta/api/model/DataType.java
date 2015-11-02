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

import java.util.Objects;

/**
 *
 * Class representing the type of a {@link Data}.
 *
 * @author dyslesiq
 */
public class DataType implements Comparable<DataType> {

    private final String type;

    /**
     * Creates the data Type with given type.
     *
     * @param dataType the string representation of the type
     */
    public DataType(final String dataType) {
        this.type = dataType;
    }

    /**
     *
     * @return the type as a string
     */
    @Override
    public String toString() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.type);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataType other = (DataType) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final DataType t) {
        return this.type.compareTo(t.type);
    }

}
