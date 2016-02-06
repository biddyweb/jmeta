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
package org.meta.api.common;

import java.nio.ByteBuffer;

/**
 * Class representing a Hash (either a model object's hash, or a piece of data's hash, or anything really), to
 * be used throughout the application.
 *
 * Copied and adapted from TomP2P's Number160.
 *
 * @author nico
 * @version $Id: $
 */
public class MetHash implements Comparable<MetHash> {

    /**
     * This key has *always* 160 bit. Do not change.
     */
    public static final int BITS = 160;

    private static final int INT_SIZE = 32;

    private static final int CHAR_SIZE = 8;

    private static final int HEXA_STRING_SIZE = 42; //40 + 2 (0x)

    /**
     * The max value of a hash.
     */
    public static final MetHash MAX_VALUE
            = new MetHash(new int[]{-1, -1, -1, -1, -1});

    /**
     * Bitwise mask for long.
     */
    protected static final long LONG_MASK = 0xffffffffL;

    /**
     * Bitwise mask for byte.
     */
    protected static final int BYTE_MASK = 0xff;

    /**
     * Bitwise mask for char.
     */
    protected static final int CHAR_MASK = 0xf;

    /**
     * A map used for String <-> Key conversion.
     */
    protected static final byte[] DIGITS = "0123456789abcdef".getBytes();

    /**
     * Size of the backing integer array.
     */
    public static final int INT_ARRAY_SIZE = BITS / INT_SIZE;

    /**
     * Size of a byte array.
     */
    public static final int BYTE_ARRAY_SIZE = BITS / CHAR_SIZE;

    /**
     * Constants zero hash.
     */
    public static final MetHash ZERO = new MetHash(0);

    /**
     * Constants one hash.
     */
    public static final MetHash ONE = new MetHash(1);

    /**
     * backing integer array.
     */
    private final byte[] val;

    /**
     * Create a Key with value 0.
     */
    public MetHash() {
        this.val = new byte[BYTE_ARRAY_SIZE];
    }

    /**
     * Create an instance with an integer array. This integer array will be copied into the backing array.
     *
     * @param ints The value to copy to the backing array. Since this class stores 160bit numbers, the array
     * needs to be of size 5 or smaller.
     * @throws java.lang.IllegalArgumentException if the array is invalid.
     */
    public MetHash(final int... ints) throws IllegalArgumentException {
        if (ints.length > INT_ARRAY_SIZE) {
            throw new IllegalArgumentException(
                    "Array of int too long. Max : " + INT_ARRAY_SIZE
                    + ". Your array has " + ints.length
            );
        }
        this.val = new byte[BYTE_ARRAY_SIZE];
        final int len = ints.length;
        for (int i = len - 1, j = BYTE_ARRAY_SIZE - 1; i >= 0; i--, j -= 4) {
            this.val[j - 3] = (byte) (ints[i] >> 24);
            this.val[j - 2] = (byte) (ints[i] >> 16);
            this.val[j - 1] = (byte) (ints[i] >> 8);
            this.val[j] = (byte) (ints[i] /* >> 0*/);
        }
    }

    /**
     * Create a Hash from a string. The string has to be of length 40 to fit into the backing array. Note that
     * this string is *always* in hexadecimal, there is no 0x... required before the number.
     *
     * @param pVal The characters allowed are [0-9a-f], which is in hexadecimal
     */
    public MetHash(final String pVal) {
        int length = pVal.length();
        if (pVal.length() > HEXA_STRING_SIZE) {
            throw new IllegalArgumentException(
                    "Can only deal with strings of size smaller or equal than 42."
                    + "Your string has " + pVal.length());
        }
        if ((pVal.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }
        this.val = new byte[length / 2];
        for (int idx = 0; idx < length; idx += 2) {
            this.val[idx / 2] = (byte) ((Character.digit(pVal.charAt(idx), 16) << 4)
                    + Character.digit(pVal.charAt(idx + 1), 16));
        }
    }

    /**
     * Creates a new Hash using the byte array.
     *
     * @param byteArray The array to be copied to the backing byte[]
     */
    public MetHash(final byte[] byteArray) {
        this(byteArray, 0, byteArray.length);
    }

    /**
     * Creates a new Hash using the byte array. The array is copied to the backing int[] starting at the given
     * offest.
     *
     * @param byteArray The array to be copied to the backing byte[].
     * @param offset The offset where to start.
     * @param length The length to use in array.
     */
    public MetHash(final byte[] byteArray, final int offset, final int length) {
        if (length > BYTE_ARRAY_SIZE) {
            throw new IllegalArgumentException(
                    "Can only deal with byte arrays of size smaller or equal than 20."
                    + "Your array has " + length);
        }
        this.val = new byte[BYTE_ARRAY_SIZE];
        for (int i = 0; i < BYTE_ARRAY_SIZE; ++i) {
            this.val[i] = byteArray[offset + i];
        }
    }

    /**
     * Creates a new Hash using the buffer as source. All necessary bytes are taken from the ByteBuffer and
     * copied in the backing byte[].
     *
     * This constructor is to avoid unnecessary copies of a byte[] when manipulating ByteBuffers.
     *
     * @param buffer the byte buffer as a source of a byte[]
     * @param length the maximum length to take from the byte buffer
     */
    public MetHash(final ByteBuffer buffer, final short length) {
        if (buffer.remaining() < BYTE_ARRAY_SIZE) {
            throw new IllegalArgumentException("Given ByteBuffer doesn't have necessary"
                    + "remaining content to create a hash");
        }
        this.val = new byte[BYTE_ARRAY_SIZE];
        buffer.get(val);
    }

    /**
     * Fills the given byte array with this number.
     *
     * @param me The byte array to fill.
     * @param offset The offset in the byte array.
     * @return the last index written in the array.
     */
    public final int toByteArray(final byte[] me, final int offset) {
        if (offset + BYTE_ARRAY_SIZE > me.length) {
            throw new RuntimeException("array too small");
        }
        for (int i = 0; i < BYTE_ARRAY_SIZE; i++) {
            me[offset + i] = this.val[i];
        }
        return offset + BYTE_ARRAY_SIZE;
    }

    /**
     * <p>
     * toByteBuffer</p>
     *
     * @param buf the buffer to fill
     */
    public final void toByteBuffer(final ByteBuffer buf) {
        if (buf.remaining() < BYTE_ARRAY_SIZE) {
            throw new RuntimeException("ByteBuffer too small");
        }
        buf.put(this.val);
    }

    /**
     * Returns a byte array, which is always of size 20.
     *
     * @return a byte array
     */
    public final byte[] toByteArray() {
        return this.val;
    }

    /**
     * {@inheritDoc}
     *
     * The string representation of a hash. Shows the content in a human readable manner.
     */
    @Override
    public final String toString() {
        byte[] hexChars = new byte[BYTE_ARRAY_SIZE * 2];

        for (int j = 0; j < BYTE_ARRAY_SIZE; j++) {
            int v = val[j] & 0xFF;
            hexChars[j * 2] = DIGITS[v >>> 4];
            hexChars[j * 2 + 1] = DIGITS[val[j] & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Checks if this number is zero.
     *
     * @return True if this number is zero, false otherwise
     */
    public final boolean isZero() {
        for (int i = 0; i < BYTE_ARRAY_SIZE; i++) {
            if (this.val[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Comparison with the given hash.
     */
    @Override
    public final int compareTo(final MetHash metHash) {
        for (int i = 0; i < BYTE_ARRAY_SIZE; i++) {
            byte cmp = (byte) (val[i] - metHash.val[i]);

            if (cmp < 0) {
                return -1;
            } else if (cmp > 0) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MetHash)) {
            return false;
        }
        final MetHash key = (MetHash) obj;
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            if (key.val[i] != val[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            hashCode = (int) (31 * hashCode + (val[i] & LONG_MASK));
        }
        return hashCode;
    }
}
