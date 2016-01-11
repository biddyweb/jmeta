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
package org.meta.storage.serializers;

import org.meta.api.common.MetHash;
import org.meta.api.storage.Serializer;

/**
 * Utility class to define and access common objects Serializer instances.
 *
 * @author dyslesiq
 */
public class Serializers {

    /**
     * Default UTF-8 String serializer.
     */
    public static final Serializer<String> STRING = new StringSerializer();

    /**
     * The MetHash serializer.
     */
    public static final Serializer<MetHash> METHASH = new MetHashSerializer();

    /**
     *
     */
    public static class StringSerializer implements Serializer<String> {

        @Override
        public byte[] serialize(final String object) {
//            ByteBuffer buf = SerializationUtils.encodeUTF8(object);
//
//            if (buf.hasArray()) {
//                return buf.array();
//            } else {
//                byte[] data = new byte[buf.limit()];
//                buf.get(data);
//                return data;
//            }
            return object.getBytes();
        }

        @Override
        public String deserialize(final byte[] data) {
            return new String(data);
            //return SerializationUtils.decodeUTF8(ByteBuffer.wrap(data));
        }

    }

    /**
     *
     */
    public static class MetHashSerializer implements Serializer<MetHash> {

        @Override
        public byte[] serialize(final MetHash object) {
            return object.toByteArray();
        }

        @Override
        public MetHash deserialize(final byte[] data) {
            if (data == null || data.length != MetHash.BYTE_ARRAY_SIZE) {
                return null;
            }
            return new MetHash(data);
        }

    }

}
