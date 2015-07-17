/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.api.common;

import java.util.Random;

/**
 * Class representing a Hash (either a model object's hash, or a piece of data's
 * hash, or anything really), to be used throughout the application.
 */
public class MetHash extends Number implements Comparable<MetHash> {

    /**
     * This key has *always* 160 bit. Do not change.
     */
    public final static int BITS = 160;

    /**
     * The max value of a hash.
     */
    public final static MetHash MAX_VALUE = new MetHash(new int[]{-1, -1, -1, -1, -1});

    /**
     * Bitwise mask for long.
     */
    protected final static long LONG_MASK = 0xffffffffL;

    /**
     * Bitwise mask for byte.
     */
    protected final static int BYTE_MASK = 0xff;

    /**
     * Bitwise mask for char.
     */
    protected final static int CHAR_MASK = 0xf;

    /**
     * a map used for String <-> Key conversion.
     */
    protected final static char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
        'f'};

    /**
     * size of the backing integer array.
     */
    public final static int INT_ARRAY_SIZE = BITS / 32;

    /**
     * size of a byte array.
     */
    public final static int BYTE_ARRAY_SIZE = BITS / 8;

    /**
     * backing integer array.
     */
    protected final int[] val;

    /**
     * Constants zero hash.
     */
    public final static MetHash ZERO = new MetHash(0);

    /**
     * Constants one hash.
     */
    public final static MetHash ONE = new MetHash(1);

    /**
     * Create a Key with value 0.
     */
    public MetHash() {
        this.val = new int[INT_ARRAY_SIZE];
    }

    /**
     * Create an instance with an integer array. This integer array will be
     * copied into the backing array.
     *
     * @param val The value to copy to the backing array. Since this class
     * stores 160bit numbers, the array needs to be of size 5 or smaller.
     */
    public MetHash(final int... val) {
        if (val.length > INT_ARRAY_SIZE) {
            throw new IllegalArgumentException("Can only deal with arrays of smaller or equal " + INT_ARRAY_SIZE
                    + ". Your array has " + val.length);
        }
        this.val = new int[INT_ARRAY_SIZE];
        final int len = val.length;
        for (int i = len - 1, j = INT_ARRAY_SIZE - 1; i >= 0; i--, j--) {
            this.val[j] = val[i];
        }
    }

    /**
     * Create a Hash from a string. The string has to be of length 40 to fit
     * into the backing array. Note that this string is *always* in hexadecimal,
     * there is no 0x... required before the number.
     *
     * @param val The characters allowed are [0-9a-f], which is in hexadecimal
     */
    public MetHash(final String val) {
        if (val.length() > 42) {
            throw new IllegalArgumentException(
                    "Can only deal with strings of size smaller or equal than 42. Your string has " + val.length());
        }
        if (val.indexOf("0x") != 0) {
            throw new IllegalArgumentException(val + " is not in hexadecimal form. Decimal form is not supported yet");
        }
        this.val = new int[INT_ARRAY_SIZE];
        final char[] tmp = val.toCharArray();
        final int len = tmp.length;
        for (int i = 42 - len, j = 2; i < 40; i++, j++) {
            this.val[i >> 3] <<= 4;
            int digit = Character.digit(tmp[j], 16);
            if (digit < 0) {
                throw new RuntimeException("Not a hexadecimal number \"" + tmp[j] + "\". The range is [0-9a-f]");
            }
            // += or |= does not matter here
            this.val[i >> 3] += digit & CHAR_MASK;
        }
    }

    /**
     * Creates a Hash with the integer value.
     *
     * @param val integer value
     */
    public MetHash(final int val) {
        this.val = new int[INT_ARRAY_SIZE];
        this.val[INT_ARRAY_SIZE - 1] = val;
    }

    /**
     * Creates a Hash with the long value.
     *
     * @param val
     */
    public MetHash(final long val) {
        this.val = new int[INT_ARRAY_SIZE];
        this.val[INT_ARRAY_SIZE - 1] = (int) val;
        this.val[INT_ARRAY_SIZE - 2] = (int) (val >> 32);
    }

    /**
     * Creates a new Hash using the byte array. The array is copied to the
     * backing int[]
     *
     * @param val
     */
    public MetHash(final byte[] val) {
        this(val, 0, val.length);
    }

    /**
     * Creates a new Hash using the byte array. The array is copied to the
     * backing int[] starting at the given offest.
     *
     * @param val
     * @param offset The offset where to start
     * @param length
     */
    public MetHash(final byte[] val, final int offset, final int length) {
        if (length > 20) {
            throw new IllegalArgumentException(
                    "Can only deal with byte arrays of size smaller or equal than 20. Your array has " + length);
        }
        this.val = new int[INT_ARRAY_SIZE];
        for (int i = length + offset - 1, j = 20 - 1, k = 0; i >= offset; i--, j--, k++) // += or |= does not matter here
        {
            this.val[j >> 2] |= (val[i] & BYTE_MASK) << ((k % 4) << 3);
        }
    }

    /**
     * Creates a new Hash with random values in it.
     *
     * @param random The object to create pseudo random numbers. For testing and
     * debugging, the seed in the random class can be set to make the random
     * values repeatable.
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
    public int[] toIntArray() {
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
    public int toByteArray(byte[] me, int offset) {
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
    public byte[] toByteArray() {
        final byte[] retVal = new byte[BYTE_ARRAY_SIZE];
        toByteArray(retVal, 0);
        return retVal;
    }

    /**
     * Shows the content in a human readable manner
     *
     * @param removeLeadingZero Indicates of leading zeros should be removed
     * @return A human readable representation of this key
     */
    public String toString(boolean removeLeadingZero) {
        final StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            toHex(val[i], removeLeadingZero, sb);
            if (removeLeadingZero && val[i] != 0) {
                removeLeadingZero = false;
            }
        }
        return sb.toString();
    }

    /**
     * Checks if this number is zero.
     *
     * @return True if this number is zero, false otherwise
     */
    public boolean isZero() {
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            if (this.val[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the number of bits used to represent this number. All leading
     * (leftmost) zero bits are ignored
     *
     * @return The bits used
     */
    public int bitLength() {
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
     * The string representation of a hash.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * The double representation of this hash.
     *
     * @return The double representation.
     */
    @Override
    public double doubleValue() {
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
    public float floatValue() {
        return (float) doubleValue();
    }

    /**
     * The int representation of this hash.
     *
     * @return The int representation.
     */
    @Override
    public int intValue() {
        return this.val[INT_ARRAY_SIZE - 1];
    }

    /**
     * The long representation of this hash.
     *
     * @return The long representation.
     */
    @Override
    public long longValue() {
        return ((this.val[INT_ARRAY_SIZE - 1] & LONG_MASK) << 32) + (this.val[INT_ARRAY_SIZE - 2] & LONG_MASK);
    }

    /**
     * Comparison with the given hash.
     * 
     * @param o 
     * @return
     */
    @Override
    public int compareTo(final MetHash o) {
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            long b1 = val[i] & LONG_MASK;
            long b2 = o.val[i] & LONG_MASK;
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
     * @param obj
     * @return
     */
    @Override
    public boolean equals(final Object obj) {
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
     * @return
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < INT_ARRAY_SIZE; i++) {
            hashCode = (int) (31 * hashCode + (val[i] & LONG_MASK));
        }
        return hashCode;
    }

    /**
     * Convert an integer to hex value
     *
     * @param integer The integer to convert
     * @param removeLeadingZero idicate if leading zeros should be ignored
     * @param sb The string bulider where to store the result
     */
    private static void toHex(int integer, final boolean removeLeadingZero, final StringBuilder sb) {
        // 4 bits form a char, thus we have 160/4=40 chars in a key, with an
        // integer array size of 5, this gives 8 chars per integer
        final int CHARS_PER_INT = 8;
        final char[] buf = new char[CHARS_PER_INT];
        int charPos = CHARS_PER_INT;
        for (int i = 0; i < CHARS_PER_INT && !(removeLeadingZero && integer == 0); i++) {
            buf[--charPos] = DIGITS[integer & CHAR_MASK];
            // for hexadecimal, we have 4 bits per char, which ranges from
            // [0-9a-f]
            integer >>>= 4;
        }
        sb.append(buf, charPos, (CHARS_PER_INT - charPos));
    }

    /**
     * Create a new MetHash from the integer, which fills all the 160bits. A new
     * random object will be created.
     *
     * @param integerValue The value to hash from
     * @return A hash from based on pseudo random, to fill the 160bits
     */
    public static MetHash createHash(int integerValue) {
        Random r = new Random(integerValue);
        return new MetHash(r);
    }

    /**
     * Create a new MetHash from the long, which fills all the 160bit. A new
     * random object will be created, thus, its thread safe
     *
     * @param longValue The value to hash from
     * @return A hash based on pseudo random, to fill the 160bits
     */
    public static MetHash createHash(long longValue) {
        Random r = new Random(longValue);
        return new MetHash(r);
    }
}
