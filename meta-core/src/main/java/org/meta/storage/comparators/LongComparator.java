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
package org.meta.storage.comparators;

import java.io.Serializable;
import java.util.Comparator;
import org.meta.utils.SerializationUtils;

/**
 *
 * Used by storage layers to provide comparison for Long (i.e. timestamps sorted elements) on byte[] entries.
 *
 * @author dyslesiq
 */
public class LongComparator implements Comparator<byte[]>, Serializable {

    /**
     *
     * @param left
     * @param right
     * @return
     */
    @Override
    public int compare(final byte[] left, final byte[] right) {
        if (left.length < Long.BYTES || right.length < Long.BYTES || left.length != right.length) {
            return left.length - right.length;
        }
        long l1 = SerializationUtils.bytesToLong(left);
        long l2 = SerializationUtils.bytesToLong(right);
        int cmp = (int) (l1 - l2);
        if (cmp == 0) {
            for (int i = Long.BYTES, j = Long.BYTES; i < left.length && j < right.length; i++, j++) {
                int a = (left[i] & 0xff);
                int b = (right[j] & 0xff);
                if (a != b) {
                    return a - b;
                }
            }
        }
        return cmp;
    }
}
