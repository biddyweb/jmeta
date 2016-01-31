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
package org.meta.storage;

import org.meta.api.storage.KVMapStorage;
import org.meta.api.storage.KVStorage;
import org.meta.api.storage.MetaTx;
import org.meta.api.storage.Serializer;

/**
 * Implementation of {@link KVMapStorage}.
 *
 * This is just a wrapper class around {@link KVStorage} that automatically transforms keys and values types
 * to byte arrays and vice versa.
 *
 * Explicit serializers for keys and values must be provided.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author dyslesiq
 * @version $Id: $
 */
public class MetaKVMapStorage<K, V> implements KVMapStorage<K, V> {

    private final KVStorage storage;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;

    /**
     * <p>Constructor for MetaKVMapStorage.</p>
     *
     * @param kvStorage the backing key/value storage
     * @param kSerializer the key serializer
     * @param vSerializer the value serializer
     * @param <K> a K object.
     * @param <V> a V object.
     */
    public MetaKVMapStorage(final KVStorage kvStorage, final Serializer<K> kSerializer,
            final Serializer<V> vSerializer) {
        this.storage = kvStorage;
        this.keySerializer = kSerializer;
        this.valueSerializer = vSerializer;
    }

    /** {@inheritDoc} */
    @Override
    public KVStorage getKVStorage() {
        return this.storage;
    }

    /** {@inheritDoc} */
    @Override
    public Serializer<K> getKeySerializer() {
        return keySerializer;
    }

    /** {@inheritDoc} */
    @Override
    public Serializer<V> getValueSerializer() {
        return valueSerializer;
    }

    /** {@inheritDoc} */
    @Override
    public V get(final K key) {
        byte[] keyData = this.keySerializer.serialize(key);
        if (keyData == null) {
            return null;
        }
        byte[] valueData = this.storage.get(keyData);
        if (valueData == null) {
            return null;
        }
        return this.valueSerializer.deserialize(valueData);
    }

    /** {@inheritDoc} */
    @Override
    public boolean put(final MetaTx tx, final K key, final V value) {
        byte[] keyData = this.keySerializer.serialize(key);
        byte[] valueData;
        if (keyData == null) {
            return true;
        }
        valueData = this.valueSerializer.serialize(value);
        if (valueData == null) {
            return true;
        }
        return this.storage.store(tx, keyData, valueData);
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(final MetaTx tx, final K key) {
        byte[] keyData = this.keySerializer.serialize(key);
        if (keyData == null) {
            return false;
        }
        return this.storage.remove(tx, keyData);
    }

    /** {@inheritDoc} */
    @Override
    public MetaTx begin() {
        return this.storage.begin();
    }

    /** {@inheritDoc} */
    @Override
    public boolean commit(final MetaTx tx) {
        return this.storage.commit(tx);
    }

    /** {@inheritDoc} */
    @Override
    public boolean rollback(final MetaTx tx) {
        return this.storage.rollback(tx);
    }

}
