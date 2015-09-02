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
package org.meta.utils;

/**
 * Utility methods to (de)serialize various types to raw data for storage or network.
 *
 * @author dyslesiq
 */
public final class SerializationUtils {

    private SerializationUtils() {

    }

    /**
     * Convert a long to a byte array.
     *
     * @param l the long to convert to byte[]
     * @return the byte array representing the long
     */
    public static byte[] longToBytes(final long l) {
        long c = l;
        byte[] result = new byte[8];

        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (c & 0xFF);
            c >>= 8;
        }
        return result;
    }

    /**
     * Convert a byte array to long.
     *
     * @param b the byte[] to convert to long
     * @return the long value
     */
    public static long bytesToLong(final byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    /**
     * Convert an int to a byte array.
     *
     * @param i the int to convert to byte[]
     * @return the byte array representing the int
     */
    public static byte[] intToBytes(final int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);
        return result;
    }

}
