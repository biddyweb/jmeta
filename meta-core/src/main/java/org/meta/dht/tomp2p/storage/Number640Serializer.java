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
package org.meta.dht.tomp2p.storage;

import java.io.Serializable;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import org.meta.api.storage.Serializer;

/**
 *
 * @author dyslesiq
 */
public class Number640Serializer implements Serializer<Number640>, Serializable {

    /**
     * The static instance of the serializer used by the Serializable Number640Comparator.
     */
    public static final Number640Serializer INSTANCE = new Number640Serializer();

    /**
     * Convenience method to serialize in the given destination array at offset.
     *
     * @param nb the Number640 to serialize
     * @param dst the destination array
     * @param offset offset with the array
     */
    public void serialize(final Number640 nb, final byte[] dst, final int offset) {
        nb.locationKey().toByteArray(dst, offset);
        nb.domainKey().toByteArray(dst, offset + Number160.BYTE_ARRAY_SIZE);
        nb.contentKey().toByteArray(dst, offset + (2 * Number160.BYTE_ARRAY_SIZE));
        nb.versionKey().toByteArray(dst, offset + (3 * Number160.BYTE_ARRAY_SIZE));
    }

    @Override
    public byte[] serialize(final Number640 nb) {
        byte[] data = new byte[Number640.BYTE_ARRAY_SIZE];

        serialize(nb, data, 0);
        return data;
    }

    /**
     * Convenience method to deserialize from given array at offset.
     *
     * @param data the source array
     * @param offset the offset within the array
     * @return the deserialized Number640 or null
     */
    public Number640 deserialize(final byte[] data, final int offset) {
        Number160 locationKey = new Number160(data, offset, Number160.BYTE_ARRAY_SIZE);
        Number160 domainKey = new Number160(data, offset + Number160.BYTE_ARRAY_SIZE,
                Number160.BYTE_ARRAY_SIZE);
        Number160 contentKey = new Number160(data, offset + (2 * Number160.BYTE_ARRAY_SIZE),
                Number160.BYTE_ARRAY_SIZE);
        Number160 versionKey = new Number160(data, offset + (3 * Number160.BYTE_ARRAY_SIZE),
                Number160.BYTE_ARRAY_SIZE);

        return new Number640(locationKey, domainKey, contentKey, versionKey);
    }

    @Override
    public Number640 deserialize(final byte[] data) {
        return deserialize(data, 0);
    }

}
