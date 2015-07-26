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
 * Enum for model factory, lists objects type from model.
 *
 * Only instantiable classes must be listed here!
 */
public enum ModelType {

    /**
     *
     */
    SEARCH(Search.class),
    /**
     *
     */
    DATASTRING(DataString.class),
    /**
     *
     */
    DATAFILE(DataFile.class),
    /**
     *
     */
    METADATA(MetaData.class);

    private final Class<?> clazz;

    private ModelType(final Class<?> claz) {
        this.clazz = claz;
    }

    /**
     *
     * @return the {@link Class} for this type.
     */
    public Class<?> getTypeClass() {
        return this.clazz;
    }

    /**
     *
     * @param claz the {@link Class} to get type for.
     * @return the associated type, or null if unknown.
     */
    public static ModelType fromClass(final Class<?> claz) {
        for (ModelType type : ModelType.values()) {
            if (type.getTypeClass() == claz) {
                return type;
            }
        }
        return null;
    }

}
