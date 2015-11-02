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

/**
 * Interface to be implemented by any object that can be uniquely identified by a {@link MetHash}.
 *
 * Internal data identified by the hash and hash calculation is let to the implementing class, but MUST
 * respect the following statements:
 *
 * Let A and B be abstract data identified by the hash.
 *
 * If A == B, then hash(A) == hash(B).
 *
 * If A != B, then hash(A) != hash(B).
 *
 * @author dyslesiq
 */
public abstract class HashIdentifiable {

    /**
     * The hash of a searchable.
     */
    protected MetHash hash;

    /**
     *
     * @param theHash the hash
     */
    protected HashIdentifiable(final MetHash theHash) {
        this.hash = theHash;
    }

    /**
     *
     * @return The hash
     */
    public MetHash getHash() {
        return this.hash;
    }

    /**
     *
     * @param metHash the hash
     */
    public void setHash(final MetHash metHash) {
        this.hash = metHash;
    }

    /**
     * Generates the hash from internal data.
     *
     * The result of two consecutive calls to this method without changing internal data MUST be identical.
     *
     * @return the newly generated hash
     */
    public abstract MetHash hash();

}
