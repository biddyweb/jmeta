/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.storage;

import java.io.File;
import kyotocabinet.DB;
import org.meta.api.configuration.ModelConfiguration;
import org.meta.api.storage.MetaStorage;
import org.meta.storage.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The kyoto cabinet implementation for {@link MetaStorage}.
 *
 * @author dyslesiq
 */
public class KyotoCabinetStorage implements MetaStorage {

    private static final Logger logger = LoggerFactory.getLogger(KyotoCabinetStorage.class);

    /**
     * The kyoto DB object.
     */
    private DB kyotoDB;

    /**
     * The model configuration.
     */
    private final ModelConfiguration configuration;

    /**
     * Instantiate the storage layer with the given configuration.
     *
     * Initializes the database.
     *
     * @param config the configuration
     *
     * @throws StorageException if the database failed to initialize
     */
    public KyotoCabinetStorage(final ModelConfiguration config) throws StorageException {
        this.configuration = config;
        initDataBase();
    }

    /**
     * Initialize database connection.
     *
     * @throws ModelException
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    private void initDataBase() throws StorageException {
        String databaseFile = this.configuration.getDatabasePath();
        //avoid dummy error, if database file parent does not exist, create one
        File databaseDir = new File(databaseFile).getParentFile();
        if (!databaseDir.isDirectory()) {
            databaseDir.mkdir();
        }
        kyotoDB = new DB();
        if (!kyotoDB.open(databaseFile, DB.OREADER
                | DB.OWRITER | DB.OCREATE | DB.MSET | DB.OTRYLOCK | DB.OAUTOTRAN)) {
            logger.error("Failed to open kyotocabinet database.");
            throw new StorageException("Unable to start kyoto cabinet with database file : " + databaseFile);
        }
    }

    @Override
    public byte[] get(final byte[] key) {
        return kyotoDB.get(key);
    }

//    @Override
//    public byte[][] getBulk(final byte[]... keys) {
//        return kyotoDB.get_bulk(keys, true);
//    }
    @Override
    public boolean store(final byte[] key, final byte[] value) {
        return kyotoDB.set(key, value);
    }

//    @Override
//    public long storeBulk(final byte[][] keys, final byte[][] values) {
//        return kyotoDB.set_bulk(keys, true);
//    }
    @Override
    public boolean remove(final byte[] key) {
        return kyotoDB.remove(key);
    }

    @Override
    public long removeBulk(final byte[]... keys) {
        return kyotoDB.remove_bulk(keys, true);
    }

    @Override
    public byte[] pop(final byte[] key) {
        return kyotoDB.seize(key);
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
        kyotoDB.synchronize(true, null);
        kyotoDB.close();
    }

    @Override
    public long count() {
        return this.kyotoDB.count();
    }

}
