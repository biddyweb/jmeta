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

import com.sleepycat.je.CacheMode;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentFailureException;
import com.sleepycat.je.TransactionConfig;
import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.KVMapStorage;
import org.meta.api.storage.KVStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.api.storage.Serializer;
import org.meta.storage.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle Berkeley java edition database implementation of MetaDatabase.
 *
 * @author dyslesiq
 */
public class BerkeleyDatabase implements MetaDatabase {

    private final Logger logger = LoggerFactory.getLogger(BerkeleyDatabase.class);

    private final ModelConfiguration configuration;

    private Environment dbEnv;

    private final TransactionConfig txConfig;

    private final CursorConfig cursorConfig;

    private final Map<String, Database> databases;

    /**
     *
     * @param config the Model configuration
     * @throws StorageException if an error occurred
     */
    public BerkeleyDatabase(final ModelConfiguration config) throws StorageException {
        this.configuration = config;
        openEnv();
        databases = new HashMap<>();
        txConfig = new TransactionConfig();
        txConfig.setSyncVoid(true); //Ensures ACID. Writes commit log directly to disk on commit()
        txConfig.setReadUncommittedVoid(true);
        cursorConfig = new CursorConfig();
        cursorConfig.setReadUncommittedVoid(true);
    }

    private void openEnv() throws StorageException {
        File dbDir = checkCreateDbDir();
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true); //Creates the db environment if needed
            envConfig.setTransactional(true); //We need explicit transactions
            envConfig.setTxnWriteNoSyncVoid(true);
            envConfig.setCacheMode(CacheMode.DEFAULT); //LRU caching by default
            envConfig.setSharedCache(false);
            this.dbEnv = new Environment(dbDir, envConfig);

        } catch (final EnvironmentFailureException ex) {
            throw new StorageException("Failed to initialize Berkeley DB environment", ex);
        }
    }

    /**
     * Check that the required database directory exists. If not try to create it.
     *
     * @throws StorageException if failed to create the directory
     * @return the File object of the database directory
     */
    private File checkCreateDbDir() throws StorageException {
        File dbDir = new File(this.configuration.getDatabasePath());
        if (!dbDir.exists()) {
            if (!dbDir.mkdir()) {
                throw new StorageException("Failed to create database file/directory.");
            }
        }
        return dbDir;
    }

    private DatabaseConfig getDbConfig() {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactionalVoid(true); //We need explicit transactions
        dbConfig.setSortedDuplicatesVoid(false); //Only 1 <-> 1 mapping of key/value
        dbConfig.setAllowCreateVoid(true); //Create the database if it does not exists
        dbConfig.setReplicated(false);
        return dbConfig;
    }

    /**
     * Internal use. Creates a database of the given name.
     *
     * @param dbName the db Name
     * @return the opened Database.
     */
    Database openDatabase(final String dbName, final Comparator<byte[]> comparator) {
        Database db = this.databases.get(dbName);

        if (db == null) {
            DatabaseConfig c = getDbConfig();
            if (comparator != null) {
                c.setOverrideBtreeComparatorVoid(true);
                c.setBtreeComparatorVoid(comparator);
            }
            db = this.dbEnv.openDatabase(null, dbName, c);
            this.databases.put(dbName, db);
        }
        return db;
    }

    @Override
    public KVStorage getKVStorage(final String name) {
        return new BerkeleyKVStorage(this, name, null);
    }

    @Override
    public KVStorage getKVStorage(final String name, final Comparator<byte[]> comparator) {
        return new BerkeleyKVStorage(this, name, comparator);
    }

    @Override
    public <T> CollectionStorage<T> getCollection(final String name, final Serializer<T> serializer) {
        return getCollection(name, serializer, null);
    }

    @Override
    public <T> CollectionStorage<T> getCollection(final String name, final Serializer<T> serializer,
            final Comparator<byte[]> comparator) {
        return new BerkeleyCollectionStorage<>(this, name, serializer, comparator);
    }

    @Override
    public <K, V> KVMapStorage<K, V> getKVMapStorage(final String name, final Serializer<K> keySerializer,
            final Serializer<V> valueSerializer) {
        return getKVMapStorage(name, keySerializer, valueSerializer, null);
    }

    @Override
    public <K, V> KVMapStorage<K, V> getKVMapStorage(final String name, final Serializer<K> keySerializer,
            final Serializer<V> valueSerializer, final Comparator<byte[]> comparator) {
        KVStorage backingStore = getKVStorage(name, comparator);
        return new MetaKVMapStorage<>(backingStore, keySerializer, valueSerializer);
    }

    @Override
    public void close() {
        try {
            for (Database db : this.databases.values()) {
                db.close();
            }
            this.dbEnv.close();
        } catch (final DatabaseException ex) {
            logger.warn("Exception while closing Berkeley databases", ex);
        }
    }

    /**
     *
     * @return the shared transaction configuration
     */
    TransactionConfig getTxConfig() {
        return this.txConfig;
    }

    /**
     *
     * @return the shared cursor configuration
     */
    CursorConfig getCursorConfig() {
        return this.cursorConfig;
    }
}
