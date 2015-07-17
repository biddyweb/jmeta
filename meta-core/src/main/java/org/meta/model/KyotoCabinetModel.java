/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 JMeta
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import kyotocabinet.DB;
import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.DataString;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaProperty;
import org.meta.api.model.Model;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.ModelType;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.configuration.ModelConfigurationImpl;
import org.meta.model.exceptions.ModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The {@link Model} implementation using kyotocabinet database as underlying
 * storage.
 *
 */
public class KyotoCabinetModel implements Model {

    private static final Logger logger = LoggerFactory.getLogger(KyotoCabinetModel.class);

    /**
     * The kyoto DB object.
     */
    private DB kyotoDB;

    /**
     * The model configuration.
     */
    private ModelConfigurationImpl configuration;

    /**
     *
     */
    private final ModelFactory factory;

    /**
     * Instanciate a new model with the given configuration.
     *
     * Init the dataBase connection.
     *
     * @param config
     *
     * @throws org.meta.model.exceptions.ModelException
     */
    public KyotoCabinetModel(ModelConfigurationImpl config) throws ModelException {
        this.configuration = config;
        initDataBase();
        factory = new ModelFactory();
    }

    /**
     * Close the model and do some clean-up
     */
    @Override
    public void close() {
        kyotoDB.close();
    }

    /**
     *
     * @return The model factory.
     */
    @Override
    public ModelFactory getFactory() {
        return this.factory;
    }

    /**
     * Log why kyoto as failed
     */
    private void kyotoError() {
        kyotocabinet.Error error = kyotoDB.error();

        if (error != null) {
            logger.error("Kyotocabinet error: " + error.code() + ":" + error.name() + " " + error.message());
        }
    }

    /**
     * Initialize data base connection.
     *
     * @throws ModelException
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    private void initDataBase() throws ModelException {
        String databaseFile = this.configuration.getDatabasePath();
        //avoid dummy error, if database file parent does not exist, create one
        File databaseDir = new File(databaseFile).getParentFile();
        if (!databaseDir.isDirectory()) {
            databaseDir.mkdir();
        }
        kyotoDB = new DB();
        //Open DB with read/write rights
        if (!kyotoDB.open(databaseFile, DB.OREADER | DB.OWRITER | DB.OCREATE | DB.MSET | DB.OTRYLOCK)) {
            logger.error("Failed to open kyotocabinet database.");
            kyotoError();
            throw new ModelException("Unable to start kyoto cabinet with database file : " + databaseFile);
        }
    }

    /**
     *
     * @param hash
     * @return a search pointed by his hash. Return null if not found or if the
     * hash is not pointed a Search object
     */
    @Override
    public Search getSearch(MetHash hash) {
        Search search = null;
        try {
            //Try to load the search in the data base
            search = (Search) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return search;
    }

    /**
     *
     * @param hash
     * @return a MetaData pointed by his hash or null if the hash is pointed on
     * nothing or if the hash is pointed on a non MetaData object
     */
    @Override
    public MetaData getMetaData(MetHash hash) {
        MetaData metaData = null;
        try {
            metaData = (MetaData) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return metaData;
    }

    /**
     *
     * @param hash
     * @return a DataString object or null if the hash does not exists
     */
    @Override
    public DataString getDataString(MetHash hash) {
        DataString data = null;

        try {
            data = (DataString) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return data;
    }

    /**
     *
     * @param hash
     * @return a DataString object or null if the hash does not exists
     */
    @Override
    public DataFile getDataFile(MetHash hash) {
        DataFile data = null;

        try {
            data = (DataFile) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return data;
    }

    /**
     *
     * @param hash
     * @return the Search linked to the hash or null if not found
     */
    @Override
    public Searchable getSearchable(MetHash hash) {
        Searchable foundedObject = null;
        try {
            foundedObject = load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return foundedObject;
    }

    /**
     * Recursive synchronized method Load. This method will retrieve an
     * Searchable object from DB.
     *
     * @param hash
     * @return a searchale object if found or null if not.
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Searchable load(byte[] hash) throws
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        byte[] serializedData = kyotoDB.get(hash);
        if (serializedData == null) {
            return null;
        }
        BSONObject bsonObject = null;
        try {
            bsonObject = BSON.decode(serializedData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        Searchable searchable = null;

        String typeName = bsonObject.get("type").toString();
        ModelType type = ModelType.valueOf(typeName);
        searchable = factory.getInstance(type);
        //Set the hash code
        searchable.setHash(new MetHash(hash));
        /*
         * Now we're looking for what is this object ? And extract the
         * good one.
         */
        if (searchable instanceof Search) {
            extractSearch(searchable, bsonObject);
        } else if (searchable instanceof MetaData) {
            extractMetaData(searchable, bsonObject);
        } else if (searchable instanceof DataFile) {
            extractDataFile(searchable, bsonObject);
        } else if (searchable instanceof DataString) {
            extractDataString(searchable, bsonObject);
        }
        searchable.setState(Searchable.ObjectState.UP_TO_DATE);
        //Freshly out from db : up to date
        return searchable;
    }

    /**
     * rebuild a DataString from BSON object call extractData to complete parent
     * operations
     *
     * @param searchable
     */
    private void extractDataString(Searchable searchable, BSONObject bsonObject) {
        DataString data = (DataString) searchable;
        extractData(data, bsonObject);
        data.setString(bsonObject.get("string").toString());
    }

    /**
     * rebuild a DataFile from BSON Object call extractData to complete parent
     * operations
     *
     * @param searchable
     * @param jsonSearcheable
     */
    private void extractDataFile(Searchable searchable, BSONObject bsonObject) {
        DataFile data = (DataFile) searchable;
        extractData(data, bsonObject);
        String filePath = bsonObject.get("file").toString();
        File file = new File(filePath);
        data.setFile(file);
    }

    /**
     * rebuild a Data from BSONObject, Data is a abstract class, this method
     * only take care of common description
     *
     * @param data
     * @param bsonObject
     */
    private void extractData(Data data, BSONObject bsonObject) {
        BasicBSONList bsonProperties = (BasicBSONList) bsonObject.get("description");
        BSONObject tmp;
        ArrayList<MetaProperty> properties = new ArrayList<MetaProperty>();
        for (String key : bsonProperties.keySet()) {
            tmp = (BSONObject) bsonProperties.get(key);
            MetaProperty toAdd = new MetaProperty(tmp.get("name").toString(), tmp.get("value").toString());
            properties.add(toAdd);
        }

        data.setDescription(properties);
    }

    /**
     * Extract a metadata from a BSONObject
     *
     * @param searchable
     * @param jsonSearcheable
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private void extractMetaData(Searchable searchable, BSONObject bsonObject)
            throws
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        MetaData metaData = (MetaData) searchable;

        BasicBSONList bsonProperties = (BasicBSONList) bsonObject.get("properties");
        BSONObject tmp;
        TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
        for (String key : bsonProperties.keySet()) {
            tmp = (BSONObject) bsonProperties.get(key);
            MetaProperty toAdd = new MetaProperty(tmp.get("name").toString(), tmp.get("value").toString());
            properties.add(toAdd);
        }

        metaData.setProperties(properties);
    }

    /**
     * Extract a search from a BSONObject object
     *
     * @param searchable
     * @param jsonSearcheable
     * @param hashSource
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessExceptionn
     */
    private void extractSearch(Searchable searchable, BSONObject bsonObject)
            throws ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        Search search = (Search) searchable;
        //load the source from her hash
        MetHash hashSource = new MetHash(bsonObject.get("source").toString());
        Searchable source = load(hashSource.toByteArray());
        //load metaData
        MetHash hashMetaData = new MetHash(bsonObject.get("metaData").toString());
        MetaData metaData = (MetaData) load(hashMetaData.toByteArray());
        //update search
        search.setSource(source);
        search.setMetaData(metaData);

        List<Data> linkedData = new ArrayList<Data>();
        BasicBSONList bsonLinkedData = (BasicBSONList) bsonObject.get("linkedData");
        for (String key : bsonLinkedData.keySet()) {
            MetHash hash = new MetHash(bsonLinkedData.get(key).toString());
            Data toAdd = (Data) load(hash.toByteArray());
            linkedData.add(toAdd);
        }
        search.addLinkedData(linkedData);
    }

    /**
     *
     * @param hash of something you want to find in the db
     * @return A searchable object, or null if not found.
     */
    @Override
    public Searchable get(MetHash hash) {
        Searchable ret = null;
        try {
            ret = load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logger.error(null, ex);
        }
        return ret;
    }

    /**
     * Start a db transaction
     *
     * @param startTx
     * @return
     */
    private boolean startTransaction(boolean startTx) {
        if (!startTx) {
            return true;
        }
        if (!kyotoDB.begin_transaction(false)) {
            logger.error("KYOTO FAILED TO START TX! " + kyotoDB.error());
            return false;
        }
        return true;
    }

    /**
     *
     * @param commit if true commits the current transaction if false rollback.
     *
     * @return true on success, false otherwise
     */
    private boolean commitTransaction(boolean commit) {
        return kyotoDB.end_transaction(commit);
    }

    /**
     *
     * @see Model.set
     *
     * @param searchable The object to set to db.
     *
     * @param startTx If true starts a transaction for all writes to db.
     *
     * @return true on success, false otherwise
     *
     * If false is returned, the database remains untouched.
     */
    private boolean set(Searchable searchable, boolean startTx) {
        boolean status = startTransaction(startTx);
        //Based on the object's type, redirects to specific set method.
        switch (ModelType.fromClass(searchable.getClass())) {
            case DATAFILE:
            case METADATA:
            case DATASTRING:
                //Datas have no childs, nothing to do here
                break;
            case SEARCH:
                status = this.setSearch((Search) searchable);
                break;
            default:
                return false;
        }
        //Finally encode the object itself and put it to db
        BSONObject bsonObject = searchable.getBson();
        byte[] data = null;
        try {
            data = BSON.encode(bsonObject);
            status = status && data != null;
        } catch (Exception e) {
            logger.error("ERROR ENCODE BSON object : " + bsonObject.toString());
            logger.error(e.getMessage(), e);
            status = false;
        }
        if (status) {
            byte[] key = searchable.getHash().toByteArray();
            byte[] existingData = kyotoDB.get(key);
            if (existingData == null || !Arrays.equals(data, existingData)) {
                status = status && kyotoDB.set(key, data);
            }
        }
        if (startTx) {
            status = commitTransaction(status) && status;
        }
        return status;
    }

    /**
     * Creates or updates a searchable object in database. All children of given
     * object are also created/updated.
     *
     * @param searchable The object to create / update
     *
     * @return true on success, false otherwise
     */
    @Override
    public boolean set(Searchable searchable) {
        if (searchable == null) {
            return false;
        }
        return set(searchable, true);
    }

    /**
     * Calls the 'set' method for each of the search's children.
     *
     * @param search A Search object.
     *
     */
    private boolean setSearch(Search search) {
        boolean status = true;

        if (search.getSource().getState() != Searchable.ObjectState.UP_TO_DATE) {
            status = status && this.set(search.getSource(), false);
        }
        if (search.getMetaData().getState() != Searchable.ObjectState.UP_TO_DATE) {
            status = status && this.set(search.getMetaData(), false);
        }

        for (Data data : search.getLinkedData()) {
            if (data.getState() != Searchable.ObjectState.UP_TO_DATE) {
                status = status && this.set(data, false);
            }
        }
        return status;
    }

    /**
     * Delete an object in DB
     *
     * @param searchable the object to remove from db
     * @return true on success, false otherwise
     */
    @Override
    public boolean remove(Searchable searchable) {
        return remove(searchable.getHash());
    }

    /**
     * Delete an object in DB
     *
     * @param hash The hash to remove from db
     * @return true on success, false otherwise
     */
    @Override
    public boolean remove(MetHash hash) {
        startTransaction(true);
        boolean status = kyotoDB.remove(hash.toByteArray());
        return commitTransaction(status) && status;
    }
}
