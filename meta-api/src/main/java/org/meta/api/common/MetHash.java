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
import java.util.Random;

/**
 * Class representing a Hash (either a model object's hash, or a piece of data's hash, or anything really), to
 * be used throughout the application.
 */
public class MetHash extends Number implements Comparable<MetHash> {

    /**
     * This key has *always* 160 bit. Do not change.
     */
    public static final int BITS = 160;

    private static final int INT_SIZE = 32;

    private static final int CHAR_SIZE = 8;

    private static final int HEXA_STRING_SIZE = 42;

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
    protected static final char[] DIGITS
            = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f'};

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
    private final int[] val;

    /**
     * Create a Key with value 0.
     */
    public MetHash() {
        this.val = new int[INT_ARRAY_SIZE];
    }

    /**
     * Create an instance with an integer array. This integer array will be copied into the backing array.
     *
     * @param ints The value to copy to the backing array. Since this class stores 160bit numbers, the array
     * needs to be of size 5 or smaller.
     *
     * @throws IllegalArgumentException if the array is invalid.
     */
    public MetHash(final int... ints) throws IllegalArgumentException {
        if (ints.length > INT_ARRAY_SIZE) {
            throw new IllegalArgumentException(
                    "Array of int too long. Max : " + INT_ARRAY_SIZE
                    + ". Your array has " + ints.length
            );
        }
        this.val = new int[INT_ARRAY_SIZE];
        final int len = ints.length;
        for (int i = len - 1, j = INT_ARRAY_SIZE - 1; i >= 0; i--, j--) {
            this.val[j] = ints[i];
        }
    }

    /**
     * Create a Hash from a string. The string has to be of length 40 to fit into the backing array. Note that
     * this string is *always* in hexadecimal, there is no 0x... required before the number.
     *
     * @param pVal The characters allowed are [0-9a-f], which is in hexadecimal
     */
    public MetHash(final String pVal) {
        if (pVal.length() > HEXA_STRING_SIZE) {
            throw new IllegalArgumentException(
                    "Can only deal with strings of size smaller or equal than 42."
                    + "Your string has " + pVal.length());
        }
        if (pVal.indexOf("0x") != 0) {
            throw new IllegalArgumentException(pVal
                    + " is not in hexadecimal form."
                    + "Decimal form is not supported yet");
        }
        this.val = new int[INT_ARRAY_SIZE];
        final char[] tmp = pVal.toCharArray();
        final int len = tmp.length;
        for (int i = HEXA_STRING_SIZE - len, j = 2; i < 40; i++, j++) {
            this.val[i >> 3] <<= 4;
            int digit = Character.digit(tmp[j], 16);
            if (digit < 0) {
                throw new RuntimeException("Not a hexadecimal number \""
                        + tmp[j] + "\". The range is [0-9a-f]");
            }
            // += or |= does not matter here
            this.val[i >> 3] += digit & CHAR_MASK;
        }
    }

    /**
     * Creates a Hash with the integer value.
     *
     * @param intVal integer value.
     */
    public MetHash(final int intVal) {
        this.val = new int[INT_ARRAY_SIZE];
        this.val[INT_ARRAY_SIZE - 1] = intVal;
    }

    /**
     * Creates a Hash with the long value.
     *
     * @param longVal the long value.
     */
    public MetHash(final long longVal) {
        this.val = new int[INT_ARRAY_SIZE];
        this.val[INT_ARRAY_SIZE - 1] = (int) longVal;
        this.val[INT_ARRAY_SIZE - 2] = (int) (longVal >> 32);
    }

    /**
     * Creates a new Hash using the byte array.
     *
     * @param byteArray The array to be copied to the backing int[]
     */
    public MetHash(final byte[] byteArray) {
        this(byteArray, 0, byteArray.length);
    }

    /**
     * Creates a new Hash using the byte array. The array is copied to the backing int[] starting at the given
     * offest.
     *
     * @param byteArray The array to be copied to the backing int[].
     * @param offset The offset where to start.
     * @param length The length to use in array.
     */
    public MetHash(final byte[] byteArray, final int offset, final int length) {
        if (length > 20) {
            throw new IllegalArgumentException(
                    "Can only deal with byte arrays of size smaller or equal than 20."
                    + "Your array has " + length);
        }
        this.val = new int[INT_ARRAY_SIZE];
        // += or |= does not matter here
        for (int i = length + offset - 1, j = 20 - 1, k = 0; i >= offset; i--, j--, k++) {
            this.val[j >> 2] |= (byteArray[i] & BYTE_MASK) << ((k % 4) << 3);
        }
    }

    /**
     * Creates a new Hash using the buffer as source. All necessary bytes are taken from the ByteBuffer and
     * copied in the backing int[].
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
        this.val = new int[INT_ARRAY_SIZE];
        for (int i = 0; i < INT_ARRAY_SIZE; ++i) {
            this.val[i] = buffer.getInt();
        }
    }

    /**
     * Creates a new Hash with random values in it.
     *
     * @param random The object to create pseudo random numbers. For testing and debugging, the seed in the
     * random class can be set to make the random values repeatable.
     */
    public MetHash(final Random random) {
        this.val = new int[INT_ARRAY_SIZE];
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            this.val[i] = random.nextInt();
        }
    }

    /**
     * Returns a copy of the backing array, which is always of size 5.
     *
     * @return a copy of the backing array
     */
    public final int[] toIntArray() {
        final int[] retVal = new int[INT_ARRAY_SIZE];
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            retVal[i] = this.val[i];
        }
        return retVal;
    }

    /**
     * Fills the given byte array with this number.
     *
     * @param me The byte array to fill.
     * @param offset The offset in the byte array.
     *
     * @return the last index written in the array.
     */
    public final int toByteArray(final byte[] me, final int offset) {
        if (offset + BYTE_ARRAY_SIZE > me.length) {
            throw new RuntimeException("array too small");
        }
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            // multiply by four
            final int idx = offset + (i << 2);
            me[idx + 0] = (byte) (val[i] >> 24);
            me[idx + 1] = (byte) (val[i] >> 16);
            me[idx + 2] = (byte) (val[i] >> 8);
            me[idx + 3] = (byte) (val[i]);
        }
        return offset + BYTE_ARRAY_SIZE;
    }

    /**
     * Returns a byte array, which is always of size 20.
     *
     * @return a byte array
     */
    public final byte[] toByteArray() {
        final byte[] retVal = new byte[BYTE_ARRAY_SIZE];
        toByteArray(retVal, 0);
        return retVal;
    }

    /**
     * The string representation of a hash. Shows the content in a human readable manner.
     *
     * @return the string representation.
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            toHex(val[i], sb);
        }
        return sb.toString();
    }

    /**
     * Checks if this number is zero.
     *
     * @return True if this number is zero, false otherwise
     */
    public final boolean isZero() {
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            if (this.val[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the number of bits used to represent this number. All leading (leftmost) zero bits are
     * ignored
     *
     * @return The bits used
     */
    public final int bitLength() {
        int bits = 0;
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            if (this.val[i] != 0) {
                bits += 32 - Integer.numberOfLeadingZeros(this.val[i]);
                bits += 32 * (INT_ARRAY_SIZE - ++i);
                break;
            }
        }
        return bits;
    }

    /**
     * The double representation of this hash.
     *
     * @return The double representation.
     */
    @Override
    public final double doubleValue() {
        double d = 0;
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            d *= LONG_MASK + 1;
            d += this.val[i] & LONG_MASK;
        }
        return d;
    }

    /**
     * The float representation of this hash.
     *
     * @return The float representation.
     */
    @Override
    public final float floatValue() {
        return (float) doubleValue();
    }

    /**
     * The int representation of this hash.
     *
     * @return The int representation.
     */
    @Override
    public final int intValue() {
        return this.val[INT_ARRAY_SIZE - 1];
    }

    /**
     * The long representation of this hash.
     *
     * @return The long representation.
     */
    @Override
    public final long longValue() {
        return ((this.val[INT_ARRAY_SIZE - 1] & LONG_MASK) << 32)
                + (this.val[INT_ARRAY_SIZE - 2] & LONG_MASK);
    }

    /**
     * Comparison with the given hash.
     *
     * @param metHash the hash to compare to.
     *
     * @return < 0 if this is considered less than metHash, > 0 if considered greater, or 0 if they are
     * equals.
     */
    @Override
    public final int compareTo(final MetHash metHash) {
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            long b1 = val[i] & LONG_MASK;
            long b2 = metHash.val[i] & LONG_MASK;
            if (b1 < b2) {
                return -1;
            } else if (b1 > b2) {
                return 1;
            }
        }
        return 0;
    }

    /**
     *
     * @param obj The object to compare.
     *
     * @return true if they are strictly equals, false otherwise.
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
     *
     * @return The int hashcode if this hash.
     */
    @Override
    public final int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            hashCode = (int) (31 * hashCode + (val[i] & LONG_MASK));
        }
        return hashCode;
    }

    /**
     * Convert an integer to hex value.
     *
     * @param integer The integer to convert
     * @param removeLeadingZero indicate if leading zeros should be ignored
     * @param sb The string builder where to store the result
     */
    private static void toHex(final Integer integer, final StringBuilder sb) {
        // 4 bits form a char, thus we have 160/4=40 chars in a key, with an
        // integer array size of 5, this gives 8 chars per integer
        final int charsPerInt = 8;
        int value = integer;
        final char[] buf = new char[charsPerInt];
        int charPos = charsPerInt;
        for (int i = 0; i < charsPerInt; i++) {
            buf[--charPos] = DIGITS[value & CHAR_MASK];
            // for hexadecimal, we have 4 bits per char, which ranges from
            // [0-9a-f]
            value >>>= 4;
        }
        sb.append(buf, charPos, (charsPerInt - charPos));
    }
}
