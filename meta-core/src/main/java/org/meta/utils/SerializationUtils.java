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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.meta.api.common.MetaPeer;

/**
 * Utility methods to (de)serialize various types to raw data for storage or network.
 *
 * @author dyslesiq
 */
public final class SerializationUtils {

    /**
     * Hexadecimal chars for utility.
     */
    protected static final char[] HEX_CHAR_ARRAY = "0123456789ABCDEF".toCharArray();

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

    /**
     * Decode the content of the given ByteBuffer, from its position to its limit, to a String.
     *
     * @param buf the UTF-8 encoded buffer to convert
     * @return the newly decoded string
     */
    public static String decodeUTF8(final ByteBuffer buf) {
        return Charset.forName("UTF-8").decode(buf).toString();
    }

    /**
     * Encode the given String into UTF-8.
     *
     * @param str the string to encode in UTF-8
     * @return the ByteBuffer containing encoded UTF8 data
     */
    public static ByteBuffer encodeUTF8(final String str) {
        return Charset.forName("UTF-8").encode(str);
    }

    /**
     * Creates an hexadecimal string representation of the given ByteBuffer.
     *
     * @param buffer the buffer
     * @return the hexadecimal string
     */
    public static String toHexString(final ByteBuffer buffer) {
        ByteBuffer buf = buffer.asReadOnlyBuffer();
        buf.rewind();
        char[] hexChars = new char[buffer.limit() * 2];

        for (int j = 0; j < buf.limit(); j++) {
            int v = buf.get() & 0xFF;
            hexChars[j * 2] = HEX_CHAR_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHAR_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Creates a ByteBuffer whose content is represented as hexadecimal in the given String.
     *
     * @param hexaString the hexadecimal string
     * @return the created ByteBuffer
     */
    public static ByteBuffer fromHexString(final String hexaString) {
        if ((hexaString.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }
        int length = hexaString.length();
        ByteBuffer buf = ByteBuffer.allocate(length / 2);

        for (int i = 0; i < length; i += 2) {
            buf.put((byte) ((Character.digit(hexaString.charAt(i), 16) << 4)
                    + Character.digit(hexaString.charAt(i + 1), 16)));
        }
        return buf;
    }

    /**
     * Serialize an ip/port couple into a byte array.
     *
     * @param port The udp port
     * @param addr The ipv4/ipv6 address
     *
     * @return the serialized ip/port couple
     */
    public static byte[] serializeAddress(final Short port, final InetAddress addr) {
        byte[] addrBytes = addr.getAddress();
        short dataSize = (short) (2 + addrBytes.length);
        byte[] data = new byte[dataSize];

        data[0] = (byte) (port & 0x00ff);
        data[1] = (byte) ((port >> 8) & 0x00ff);
        for (short i = 2; i < dataSize; ++i) {
            data[i] = addrBytes[i - 2];
        }
        return data;
    }

    /**
     * De-serialize the Ip/port couple from the given data into a {@link  MetaPeer}.
     *
     * @param data The serialized ip:port couple.
     * @return the created peer or null if invalid data.
     */
    public static MetaPeer peerFromData(final byte[] data) {
        MetaPeer peer = new MetaPeer();
        short addrSize = (short) (data.length - 2);
        byte[] addrBytes = new byte[addrSize];
        short port = (short) (((data[1] & 0xFF) << 8) | (data[0] & 0xFF));

        for (int i = 0; i < addrSize; ++i) {
            addrBytes[i] = data[i + 2];
        }
        try {
            InetAddress inetAddr = InetAddress.getByAddress(addrBytes);
            if (inetAddr == null) {
                return null;
            }
            peer.setAddress(new InetSocketAddress(inetAddr, port));
        } catch (UnknownHostException ex) {
            return null;
        }
        return peer;
    }

}
