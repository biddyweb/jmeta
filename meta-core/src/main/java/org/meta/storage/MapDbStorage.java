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

import java.io.File;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.storage.MetaStorage;
import org.meta.storage.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MapDb implementation for {@link MetaStorage}.
 *
 * @author dyslesiq
 */
public class MapDbStorage implements MetaStorage {

    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(MapDbStorage.class);

    /**
     * The MapDb database ID.
     */
    private static final String DATABASE_ID = "ID";

    /**
     * The model configuration.
     */
    private final ModelConfiguration configuration;

    private DB mapDb;

    /**
     * The actual Map where entries are stored.
     */
    private HTreeMap<byte[], byte[]> dataBase;

    /**
     * Instantiate the storage layer with the given configuration.
     *
     * Initializes the database.
     *
     * @param config the configuration
     *
     * @throws StorageException if the database failed to initialize
     */
    public MapDbStorage(final ModelConfiguration config) throws StorageException {
        this.configuration = config;
        initDataBase();
    }

    /**
     * Initialize database connection.
     *
     * @throws StorageException
     */
    private void initDataBase() throws StorageException {
        String databaseFile = this.configuration.getDatabasePath();
        //avoid dummy error, if database file parent does not exist, create one
        File databaseDir = new File(databaseFile).getParentFile();
        if (!databaseDir.isDirectory()) {
            databaseDir.mkdir();
        }
        mapDb = DBMaker.fileDB(new File(databaseFile)).fileMmapEnableIfSupported().
                make();
        dataBase = mapDb.hashMapCreate(DATABASE_ID).keySerializer(Serializer.BYTE_ARRAY).valueSerializer(Serializer.BYTE_ARRAY)
                .make();
    }

    @Override
    public byte[] get(final byte[] key) {
        return dataBase.get(key);
    }

    @Override
    public boolean store(final byte[] key, final byte[] value) {
        try {
            dataBase.put(key, value);
            return true;
        } catch (final Exception ex) {
            return false;
        }
    }

    @Override
    public boolean remove(final byte[] key) {
        try {
            dataBase.remove(key);
            return true;
        } catch (final Exception ex) {
            return false;
        }
    }

    @Override
    public long removeBulk(final byte[]... keys) {
        return 0;
    }

    @Override
    public byte[] pop(final byte[] key) {
        return dataBase.remove(key);
    }

    @Override
    public long count() {
        return dataBase.sizeLong();
    }

    @Override
    public boolean begin() {
        return true;
    }

    @Override
    public boolean commit() {
        mapDb.commit();
        return true;
    }

    @Override
    public boolean rollback() {
        mapDb.rollback();
        return true;
    }

    @Override
    public void close() {
        commit();
        dataBase.close();
        mapDb.close();
    }

}
