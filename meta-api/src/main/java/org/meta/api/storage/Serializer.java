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
package org.meta.api.storage;

/**
 * Interface describing (de)/serialization capacity of objects.
 *
 * Serializers are used on the Meta storage layer.
 *
 * @param <T> the type to serialize/deserialize
 * @author dyslesiq
 * @version $Id: $
 */
public interface Serializer<T> {

    /**
     * <p>serialize</p>
     *
     * @param object the model object to serialize
     * @return the serialized object as a byte[] or null if unable to process
     * @param <T> a T object.
     */
    byte[] serialize(final T object);

    /**
     * <p>deserialize</p>
     *
     * @param data the byte[] to de-serialize.
     * @return the extracted type, or null if unable to process
     */
    T deserialize(final byte[] data);

}
