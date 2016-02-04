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
import org.meta.api.storage.KVStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.api.storage.MetaTx;
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
 * The {@link ModelStorage} implementation using low-level {@link KVStorage} as underlying storage.
 *
 * @author nico
 * @version $Id: $
 */
public class MetaModelStorage implements ModelStorage {

    private static final Logger logger = LoggerFactory.getLogger(MetaModelStorage.class);

    private static final String MODEL_DB_NAME = "mainModel";

    private final KVStorage storage;

    private final DataBsonSerializer dataSerializer;

    private final SearchBsonSerializer searchSerializer;

    /**
     *
     */
    private final MetaObjectModelFactory factory;

    /**
     * Instantiate a new model with the given backing storage unit.
     *
     * @param database a {@link org.meta.api.storage.MetaDatabase} object.
     */
    public MetaModelStorage(final MetaDatabase database) {
        this.storage = database.getKVStorage(MODEL_DB_NAME);
        factory = new MetaObjectModelFactory();
        this.dataSerializer = new DataBsonSerializer();
        this.searchSerializer = new SearchBsonSerializer();
    }

    /**
     * {@inheritDoc}
     *
     * Close the model and do some clean-up.
     */
    @Override
    public void close() {
        storage.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaObjectModelFactory getFactory() {
        return this.factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Searchable get(final MetHash hash) {
        return load(hash.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaSearch getSearch(final MetHash hash) {
        Searchable s = load(hash.toByteArray());

        if (s instanceof MetaSearch) {
            return (MetaSearch) s;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericData getData(final MetHash hash) {
        Searchable s = load(hash.toByteArray());

        if (s instanceof GenericData) {
            return (GenericData) s;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Utility function to retrieve a DataFile.
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

    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     *
     * Creates or updates a searchable object in database. All children of given object are also
     * created/updated.
     */
    @Override
    public boolean set(final Searchable searchable) {
        return set(searchable, 0L);
    }

    /**
     * {@inheritDoc}
     */
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
        MetaTx tx = null;
        if (startTx) {
            tx = this.storage.begin();
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
            if (status) {
                if (!this.storage.commit(tx)) {
                    this.storage.rollback(tx);
                    return false;
                }
            } else {
                this.storage.rollback(tx);
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
            return storage.store(null, search.getHash().toByteArray(), serialized);
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
            return this.storage.store(null, data.getHash().toByteArray(), serialized);
        } else {
            logger.error("SERIALIZED DATA IS NULL! :(");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Delete an object in DB.
     */
    @Override
    public boolean remove(final Searchable searchable) {
        return remove(searchable.getHash());
    }

    /**
     * {@inheritDoc}
     *
     * Delete an object in DB.
     */
    @Override
    public boolean remove(final MetHash hash) {
        return this.storage.remove(null, hash.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KVStorage getStorage() {
        return storage;
    }
}
