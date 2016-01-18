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
package org.meta.dht.storage;

import org.meta.api.common.MetHash;
import org.meta.api.storage.Serializer;
import org.meta.dht.DHTPushElement;
import org.meta.utils.SerializationUtils;

/**
 * Serializer for the DHT push element used by {@link DHTPushManager} storage components.
 *
 * The DHTPushManager algorithm rely on the ordering of elements (lower timestamps first).
 *
 * This serializer provides such ordering by putting {@link DHTPushElement#getNextPushTime()} first In
 * serialization order and then using a {@link LongComparator} in {@link CollectionStorage}.
 */
public class PushElementSerializer implements Serializer<DHTPushElement> {

    private static final int ELEMENT_SIZE = MetHash.BYTE_ARRAY_SIZE + (2 * Long.BYTES);

    @Override
    public byte[] serialize(final DHTPushElement element) {
        byte[] data = new byte[ELEMENT_SIZE];

        SerializationUtils.longToBytes(element.getNextPushTime(), data, 0);
        SerializationUtils.longToBytes(element.getExpiration(), data, Long.BYTES);
        //TODO better implementation without ByteBuffer ?
        //ByteBuffer buf = ByteBuffer.wrap(data);
        //In order to respect lexicographic order, we need to put the next push timestamp first
//        buf.putLong(element.getNextPushTime());
//        buf.putLong(element.getExpiration());
        element.getHash().toByteArray(data, 2 * Long.BYTES);
        return data;
    }

    @Override
    public DHTPushElement deserialize(final byte[] data) {
        if (data == null || data.length != ELEMENT_SIZE) {
            //If the data doesn't match the size of a DHTPushElement, do nothing as it isn't one
            return null;
        }
        //TODO better implementation without ByteBuffer ?
//        ByteBuffer buf = ByteBuffer.wrap(data);
//        long l1 = buf.getLong();
//        long l2 = buf.getLong();
//        MetHash hash = new MetHash(buf, (short) MetHash.BYTE_ARRAY_SIZE);
        long l1 = SerializationUtils.bytesToLong(data);
        long l2 = SerializationUtils.bytesToLong(data, Long.BYTES);
        MetHash hash = new MetHash(data, 2 * Long.BYTES, MetHash.BYTE_ARRAY_SIZE);
        return new DHTPushElement(hash, l1, l2);
    }
}
