/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Nicolas Michon
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.api.common;

/**
 *
 * Identity class, derived from MetHash, useful to represent a hash as an identity.
 *
 * @author nico
 */
public final class Identity extends MetHash {

    /**
     * Creates an empty identity.
     */
    public Identity() {
        super();
    }

    /**
     * Creates an identity from a hash.
     *
     * @param hash The MetHash object.
     */
    public Identity(final MetHash hash) {
        this(hash.toByteArray());
    }

    /**
     * Creates an identity with an integer array.
     *
     * @param val The integer array.
     */
    public Identity(final int... val) {
        super(val);
    }

    /**
     * Creates an identity from an hexadecimal string.
     *
     * @param val The hexadecimal string.
     */
    public Identity(final String val) {
        super(val);
    }

    /**
     *
     * Creates an identity with the integer value.
     *
     * @param val The integer value.
     */
    public Identity(final int val) {
        super(val);
    }

    /**
     * Creates an identity with a long value.
     *
     * @param val The Long value.
     */
    public Identity(final long val) {
        super(val);
    }

    /**
     * Creates an identity with a bytes array.
     *
     * @param val The bytes array.
     */
    public Identity(final byte[] val) {
        super(val);
    }
}
