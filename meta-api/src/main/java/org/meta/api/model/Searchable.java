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
package org.meta.api.model;

import org.meta.api.common.HashIdentifiable;
import org.meta.api.common.MetHash;

/**
 * Interface representing the ability for an object to be used as a source for a search.
 *
 * @author faquin
 */
public abstract class Searchable extends HashIdentifiable {

    /**
     *
     * @param hash the hash of the searchable
     */
    protected Searchable(final MetHash hash) {
        super(hash);
    }

    /**
     * Enum to lists the different possible states of a model object.
     */
//    public enum ObjectState {
//
//        /**
//         * Object has been instantiated, it does not comes from network or database.
//         */
//        CREATED,
//        /**
//         * Object comes from network.
//         */
//        FROM_NETWORK,
//        /**
//         * Object comes from database and is up to date.
//         */
//        UP_TO_DATE,
//        /**
//         * Object comes from database but has been modified.
//         */
//        DIRTY;
//    };
}
