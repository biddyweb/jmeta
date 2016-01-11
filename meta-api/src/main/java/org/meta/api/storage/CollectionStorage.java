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
package org.meta.api.storage;

import java.util.SortedSet;

/**
 * Interface describing functionalities of a Java SortedSet backed by a database provider with transactional
 * support.
 *
 * This interface is to be implemented by providers of {@link MetaDatabase}.
 *
 * A CollectionStorage is just a different, convenient way of retrieving records from a database. Records are
 * shared with {@link KVStorage} bond to the same database.
 *
 * @param <T> The type of this collection elements
 *
 * @author dyslesiq
 */
public interface CollectionStorage<T> extends SortedSet<T> {

    /**
     *
     * @return the name of the database this CollectionStorage is bond to
     */
    String getDatabaseName();

    /**
     *
     * @return the serializer used by this CollectionStorage
     */
    Serializer<T> getSerializer();

    /**
     * Begins a transaction.
     *
     * Once a transaction is opened, it MUST be closed either by calling {@link commit()} or
     * {@link rollback()}, otherwise the behavior of following transactions is undefined.
     *
     * Calling {@link begin()} twice without closing the transaction first will result in a RuntimeException.
     *
     * @return true if the transaction was created successfully, false otherwise
     */
    MetaTx begin();

    /**
     * Commit pending changes to the underlying storage.
     *
     * The behavior is provider-specific but implementations MUST ensure ACID.
     *
     * @param tx the transaction to commit
     * @return true if the transaction has commit successfully, false otherwise
     */
    boolean commit(MetaTx tx);

    /**
     * Rollback pending changes.
     *
     * The behavior is provider-specific but implementations MUST ensure ACID.
     *
     * @param tx the transaction to rollback
     * @return true if the transaction has rollback successfully, false otherwise
     */
    boolean rollback(MetaTx tx);

}
