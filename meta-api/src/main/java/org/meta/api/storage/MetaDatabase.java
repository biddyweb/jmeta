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

import java.util.Comparator;

/**
 * This interface describe storage capacities on a backing database.
 *
 * Implementations should be thread-safe and respect ACID.
 *
 * For now, only two types of storage exists: {@link KVStorage} and {@link CollectionStorage}.
 *
 * Important note: records are shared between Key/value storage and collection Storage bond to the same
 * 'Storage Unit'. The name provided to each method defines the name of this unit.
 *
 * Records are not shared across different 'Storage Units' i.e different databases names.
 *
 * @author dyslesiq
 */
public interface MetaDatabase {

    /**
     * Get or creates a {@link KVStorage}.
     *
     * This storage operations will be directly reflected on the underlying database of the given name.
     *
     * @param name the name of the database this storage is bond to
     * @return the Key/Value storage
     */
    KVStorage getKVStorage(final String name);

    /**
     * Get or creates a {@link KVStorage}.
     *
     * This storage operations will be directly reflected on the underlying database of the given name.
     *
     * @param name the name of the database this storage is bond to
     * @param comparator the comparator for storage entries. The comparator will be associated to the database
     * of the given name.
     * @return the Key/Value storage
     */
    KVStorage getKVStorage(final String name, final Comparator<byte[]> comparator);

    /**
     * Get or creates a {@link KVMapStorage}.
     *
     * This storage operations will be directly reflected on the associated underlying database of the given
     * name.
     *
     * @param <K> key type
     * @param <V> value type
     * @param name the name of the database this storage is bond to
     * @param keySerializer serializer for key type
     * @param valueSerializer serializer for value type
     * @return the KVMapStorage initialized with given parameters
     */
    <K, V> KVMapStorage<K, V> getKVMapStorage(final String name, final Serializer<K> keySerializer,
            final Serializer<V> valueSerializer);

    /**
     * Get or creates a {@link KVMapStorage}.
     *
     * This storage operations will be directly reflected on the associated underlying database of the given
     * name.
     *
     * @param <K> key type
     * @param <V> value type
     * @param name the name of the database this storage is bond to
     * @param keySerializer serializer for key type
     * @param valueSerializer serializer for value type
     * @param comparator the comparator for storage entries. The comparator will be associated to the database
     * of the given name.
     * @return the KVMapStorage initialized with given parameters
     */
    <K, V> KVMapStorage<K, V> getKVMapStorage(final String name, final Serializer<K> keySerializer,
            final Serializer<V> valueSerializer, final Comparator<byte[]> comparator);

    /**
     * Get or creates a storage-linked {@link java.util.SortedSet}.
     *
     * This collection operations will be directly reflected on the associated underlying database of the
     * given name.
     *
     * Although it implements {@link java.util.SortedSet}, the ordering of elements is not guaranteed.
     *
     * The {@link CollectionStorage#comparator()} will always be null. Ordering is done by a byte-by-byte
     * lexicographic comparison of the serialized data. Thus, it is the serializer's responsibility to provide
     * such ordering or not.
     *
     * @param <T> the type of elements
     * @param name the name of the database this collection is bond to
     * @param serializer the serializer used to (de)/serialize objects into byte arrays
     * @return the CollectionStorage initialized with given parameters
     */
    <T> CollectionStorage<T> getCollection(final String name, final Serializer<T> serializer);

    /**
     * Get or creates a storage-linked {@link java.util.SortedSet}.
     *
     * This collection operations will be directly reflected on the associated underlying database of the
     * given name.
     *
     * Although it implements {@link java.util.SortedSet}, the ordering of elements is not guaranteed.
     *
     * The {@link CollectionStorage#comparator()} will always be null. Ordering is done by a byte-by-byte
     * lexicographic comparison of the serialized data. Thus, it is the serializer's responsibility to provide
     * such ordering or not.
     *
     * @param <T> the type of elements
     * @param name the name of the database this collection is bond to
     * @param serializer the serializer used to (de)/serialize objects into byte arrays
     * @param comparator the comparator for storage entries. The comparator will be associated to the database
     * of the given name.
     * @return the CollectionStorage initialized with given parameters
     */
    <T> CollectionStorage<T> getCollection(final String name, final Serializer<T> serializer,
            final Comparator<byte[]> comparator);

    /**
     * Close properly this database. This will also close all related {@link KVStorage}, {@link KVMapStorage}
     * and {@link CollectionStorage}.
     */
    void close();

}
