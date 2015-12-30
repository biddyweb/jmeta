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

import java.util.HashSet;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.ModelStorage;
import org.meta.api.model.Searchable;
import org.meta.api.storage.MetaStorage;
import org.meta.model.GenericData;
import org.meta.model.MetaObjectModelFactory;
import org.meta.model.MetaSearch;
import org.meta.model.ModelType;
import org.meta.storage.serializers.DataBsonSerializer;
import org.meta.storage.serializers.ModelBsonExtractor;
import org.meta.storage.serializers.SearchBsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The {@link ModelStorage} implementation using low-level database as underlying storage.
 *
 */
public class MetaModelStorage implements ModelStorage {

    private static final Logger logger = LoggerFactory.getLogger(MetaModelStorage.class);

    private final MetaStorage storage;

    private final DataBsonSerializer dataSerializer;

    private final SearchBsonSerializer searchSerializer;

    /**
     *
     */
    private final MetaObjectModelFactory factory;

    /**
     * Instantiate a new model with the given backing storage unit.
     *
     * @param storageDb the storage unit to uses
     */
    public MetaModelStorage(final MetaStorage storageDb) {
        this.storage = storageDb;
        factory = new MetaObjectModelFactory();
        this.dataSerializer = new DataBsonSerializer();
        this.searchSerializer = new SearchBsonSerializer();
    }

    /**
     * Close the model and do some clean-up.
     */
    @Override
    public void close() {
        storage.close();
    }

    /**
     * @return The model factory.
     */
    @Override
    public MetaObjectModelFactory getFactory() {
        return this.factory;
    }

    /**
     *
     * @param hash of something you want to find in the db
     * @return A searchable object, or null if not found.
     */
    @Override
    public Searchable get(final MetHash hash) {
        return load(hash.toByteArray());
    }

    @Override
    public MetaSearch getSearch(final MetHash hash) {
        Searchable s = load(hash.toByteArray());

        if (s instanceof MetaSearch) {
            return (MetaSearch) s;
        }
        return null;
    }

    @Override
    public GenericData getData(final MetHash hash) {
        Searchable s = load(hash.toByteArray());

        if (s instanceof GenericData) {
            return (GenericData) s;
        }
        return null;
    }

    /**
     * Utility function to retrieve a DataFile.
     *
     * @param hash the hash of a data file
     * @return a DataFile object or null if the hash does not exists or if the hash does not point to a
     * DataFile.
     */
    @Override
    public DataFile getDataFile(final MetHash hash) {
        GenericData data = this.getData(hash);

        if (data == null) {
            logger.debug("getDataFile: sub-data is null!");
            return null;
        }
        return this.factory.getDataFile(data);
    }

    @Override
    public Searchable getSearchable(final MetHash hash) {
        return load(hash.toByteArray());
    }

    /**
     * Recursive method Load. This method will retrieve an Searchable object from DB.
     *
     * @param hash the hash to load.
     * @return a searchable object if found or null if not.
     */
    private Searchable load(final byte[] hash) {
        byte[] serializedData = storage.get(hash);
        if (serializedData == null) {
            logger.debug("Storage returned null.");
            return null;
        }
        ModelBsonExtractor extractor = new ModelBsonExtractor(serializedData,
                dataSerializer, searchSerializer);

        switch (extractor.getType()) {
            case DATA:
                logger.debug("Load: DATA");
                return this.extractData(extractor);
            case SEARCH:
                logger.debug("Load: SEARCH");
                return this.extractSearch(extractor);
            default:
                logger.warn("Unable to extract correct type while de-serializing.");
                return null;
        }
    }

    private Searchable extractData(final ModelBsonExtractor extractor) {
        //Data are serialized fully in one data bucket, that's all there is to do
        return extractor.getData();
    }

    private Searchable extractSearch(final ModelBsonExtractor extractor) {
        //Get the search, and extracts source object and results
        MetaSearch search = extractor.getSearch();

        if (search != null) {
            MetHash srcHash = extractor.getSearchSourceHash();
            logger.debug("Extractor got search source: " + srcHash.toString());
            Searchable src = this.load(srcHash.toByteArray());
            search.setSource(src);
            Set<MetHash> resultHashs = extractor.getSearchResultsHash();
            Set<Data> results = new HashSet<>();

            for (MetHash resultHash : resultHashs) {
                results.add(this.getData(resultHash));
            }
            search.addResults(results);
        } else {
            logger.debug("Extractor returned null for search.");
        }
        return search;
    }

    /**
     * Creates or updates a searchable object in database. All children of given object are also
     * created/updated.
     *
     * @param searchable The object to create / update
     *
     * @return true on success, false otherwise
     */
    @Override
    public boolean set(final Searchable searchable) {
        return set(searchable, 0L);
    }

    @Override
    public boolean set(final Searchable searchable, final long timeout) {
        //Unused timeout for now...
        return set(searchable, true);
    }

    /**
     *
     * @param searchable The object to set to db.
     *
     * @param startTx If true starts a transaction for all writes to db. UNUSED.
     *
     * @return true on success, false otherwise
     *
     * If false is returned, the database remains untouched.
     */
    private boolean set(final Searchable searchable, final boolean startTx) {
        if (searchable == null || searchable.getHash() == MetHash.ZERO) {
            logger.warn("Tried to store null or invalid object hash.");
            return false;
        }
        if (startTx) {
            if (!this.storage.begin()) {
                return false;
            }
        }
        boolean status = false;
        //Based on the object's type, redirects to specific set method.
        switch (ModelType.getType(searchable)) {
            case DATA:
                logger.debug("set: isDATA: " + searchable.getHash());
                status = this.setData((Data) searchable);
                break;
            case SEARCH:
                logger.debug("set: isSearch: " + searchable.getHash());
                status = this.setSearch((MetaSearch) searchable);
                break;
            default:
                logger.warn("Unknown object type.");
        }
        if (startTx) {
            if (!this.storage.commit()) {
                return this.storage.rollback();
            }
        }
        return status;
    }

    /**
     * Calls the 'set' method for each of the search's children.
     *
     * @param search A MetaSearch object.
     * @return true on success, false on error
     */
    private boolean setSearch(final MetaSearch search) {
        logger.debug("SET SEARCH " + search.getHash());
        for (Data data : search.getResults()) {
            if (!this.set(data, false)) {
                return false;
            }
        }
        if (!this.set(search.getSource(), false)) {
            return false;
        }
        byte[] serialized = this.searchSerializer.serialize(search);
        if (serialized != null) {
            return storage.store(search.getHash().toByteArray(), serialized);
        }
        return false;
    }

    /**
     * Serializes and store the given data into db.
     *
     * @param data the data
     * @return true on success, false on error
     */
    private boolean setData(final Data data) {
        logger.debug("setData: " + data.getHash());
        byte[] serialized = this.dataSerializer.serialize(data);

        if (serialized != null) {
            return this.storage.store(data.getHash().toByteArray(), serialized);
        } else {
            logger.error("SERIALIZED DATA IS NULL! :(");
        }
        return false;
    }

    /**
     * Delete an object in DB.
     *
     * @param searchable the object to remove from db
     * @return true on success, false otherwise
     */
    @Override
    public boolean remove(final Searchable searchable) {
        return remove(searchable.getHash());
    }

    /**
     * Delete an object in DB.
     *
     * @param hash The hash to remove from db
     * @return true on success, false otherwise
     */
    @Override
    public boolean remove(final MetHash hash) {
        //startTransaction(true);
        return storage.remove(hash.toByteArray());
        //return commitTransaction(status) && status;
    }

    /**
     * Start a db transaction.
     *
     * @param startTx if actually starting the transaction or not.
     * @return true on success, false otherwise.
     */
//    private boolean startTransaction(final boolean startTx) {
//        if (!startTx) {
//            return true;
//        }
//        if (!storage.begin()) {
//            logger.error("FAILED TO START TX!");
//            return false;
//        }
//        return true;
//    }
    /**
     *
     * @param commit if true commits the current transaction if false rollback.
     *
     * @return true on success, false otherwise
     */
//    private boolean commitTransaction(final boolean commit) {
//        if (commit) {
//            return storage.commit();
//        } else {
//            return storage.rollback();
//        }
//    }
    @Override
    public MetaStorage getStorage() {
        return storage;
    }
}
