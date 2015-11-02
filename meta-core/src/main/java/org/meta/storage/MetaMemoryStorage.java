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

import java.util.HashMap;
import java.util.Map;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.storage.MetaStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The (really) simple in-memory implementation for {@link MetaStorage}.
 *
 * Should not be used as-is, only for tests.
 *
 * @author dyslesiq
 */
public class MetaMemoryStorage implements MetaStorage {

    private static final Logger logger = LoggerFactory.getLogger(MetaMemoryStorage.class);

    /**
     * The model configuration.
     */
    private final ModelConfiguration configuration;

    private final Map<byte[], byte[]> map;

    /**
     * Instantiate the storage layer with the given configuration.
     *
     * Initializes the dataBase connection.
     *
     * @param config the configuration
     *
     */
    public MetaMemoryStorage(final ModelConfiguration config) {
        this.configuration = config;
        map = new HashMap<>();
    }

    @Override
    public byte[] get(final byte[] key) {
        return map.get(key);
    }

//    @Override
//    public byte[][] getBulk(final byte[]... keys) {
//        throw new UnsupportedOperationException();
//    }
    @Override
    public boolean store(final byte[] key, final byte[] value) {
        map.put(key, value);
        return true;
    }

//    @Override
//    public long storeBulk(final byte[][] keys, final byte[][] values) {
//        throw new UnsupportedOperationException();
//    }
    @Override
    public boolean remove(final byte[] key) {
        map.remove(key);
        return true;
    }

    @Override
    public long removeBulk(final byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] pop(final byte[] key) {
        return map.remove(key);
    }

    @Override
    public boolean begin() {
        return true;
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public boolean rollback() {
        return true;
    }

    @Override
    public void close() {
        map.clear();
    }

    @Override
    public long count() {
        return this.map.size();
    }

}
