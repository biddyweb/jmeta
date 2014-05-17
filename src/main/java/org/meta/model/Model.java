package org.meta.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kyotocabinet.DB;
import org.bson.BSONDecoder;
import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
import org.bson.types.BasicBSONList;
import org.meta.common.MetaProperties;
import org.meta.model.exceptions.ModelException;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Thomas LAVOCAT
 *
 */
public class Model {

    private static final String DEFAULT_DATABASE_FILE = "db/jmeta.kch";
    DB kyotoDB;
    private BSONDecoder bsonDecoder;
    private BSONEncoder bsonEncoder;

    /**
     * Instanciate a new model. Init the dataBaseConnection.
     *
     * @throws org.meta.model.exceptions.ModelException
     */
    public Model() throws ModelException {
        initDataBase();
        bsonDecoder = new BasicBSONDecoder();
        bsonEncoder = new BasicBSONEncoder();
    }

    /**
     * Initialize data base connection
     *
     * @throws LibraryException
     */
    private void initDataBase() throws ModelException {
        String databaseFile = MetaProperties.getProperty("database_path", DEFAULT_DATABASE_FILE);
        kyotoDB = new DB();
        if (!kyotoDB.open(databaseFile, DB.OREADER | DB.OWRITER | DB.OAUTOTRAN | DB.OCREATE)) {
            throw new ModelException("Unable to open database file : " + databaseFile);
        }
    }

    /**
     *
     * @param hash
     * @return a search pointed by his hash. Return null if not found or if the
     * hash is not pointed a Search object
     */
    public Search getSearch(String hash) {
        Search search = null;
        try {
            //Try to load the search true the data base
            search = (Search) load(hash);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return search;
    }

    /**
     *
     * @param hash
     * @return a MetaData pointed by his hash or null is the hash is pointed on
     * nothing or if the hash is pointed on a non MetaData object
     */
    public MetaData getMetaData(String hash) {
        MetaData metaData = null;
        try {
            metaData = (MetaData) load(hash);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return metaData;
    }

    /**
     *
     * @param hash
     * @return a Data pointed by the hash or null if the hash is pointed on
     * nothing or on a non Data Object
     */
    public Data getData(String hash) {
        Data data = null;
        try {
            data = (Data) load(hash);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     *
     * @param hash
     * @return the Search linked to the hash or null if not found
     */
    public Searchable getSearchable(String hash) {
        Searchable foundedObject = null;
        try {
            foundedObject = load(hash);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return foundedObject;
    }

    /**
     * Recursive synchronized method Load.
     *
     * @param hash
     * @return a searchale object if found or null if not.
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Searchable load(String hash) throws
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        byte[] serializedData = kyotoDB.get(hash.getBytes());
        if (serializedData == null) {
            return null;
        }
        BSONObject bsonObject = bsonDecoder.readObject(serializedData);
        Searchable searchable = null;

        /*
         * Every Searchable object have the same globals datas :
         * - className
         * - hash
         */
        String className = bsonObject.get("class").toString();
        //The class name is used to instantiate an object
        Class<?> clazz = Class.forName(className);
        searchable = (Searchable) clazz.newInstance();
        //Set his hash code
        searchable.setHashCode(hash);

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
        return searchable;
    }

    /**
     *
     * @param searchable
     */
    private void extractDataString(Searchable searchable, BSONObject bsonObject) {
        DataString data = (DataString) searchable;
        data.setString(bsonObject.get("string").toString());
    }

    /**
     * Extract a data from Searchable object
     *
     * @param searchable
     * @param jsonSearcheable
     */
    private void extractDataFile(Searchable searchable, BSONObject bsonObject) {
        DataFile data = (DataFile) searchable;
        String filePath = bsonObject.get("file").toString();
        File file = new File(filePath);
        data.setFile(file);
    }

    /**
     * Extract a metadata from a searchale object
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
        List<Data> linkedData = new ArrayList<Data>();
        BasicBSONList bsonLinkedData = (BasicBSONList) bsonObject.get("linkedData");
        for (String key : bsonLinkedData.keySet()) {
            Data toAdd = (Data) load(bsonLinkedData.get(key).toString());
            linkedData.add(toAdd);
        }
        BasicBSONList bsonProperties = (BasicBSONList) bsonObject.get("properties");
        BSONObject tmp;
        List<MetaProperty> properties = new ArrayList<MetaProperty>();
        for (String key : bsonProperties.keySet()) {
            tmp = (BSONObject) bsonProperties.get(key);
            MetaProperty toAdd = new MetaProperty(tmp.get("name").toString(), tmp.get("value").toString());
            properties.add(toAdd);
        }
        metaData.setLinkedData(linkedData);
        metaData.setProperties(properties);
    }

    /**
     * Extract a search from a searchable object
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
        Searchable source = load(bsonObject.get("source").toString());
        List<MetaData> results = new ArrayList<MetaData>();
        BasicBSONList bsonResultsList = (BasicBSONList) bsonObject.get("results");
        for (String key : bsonResultsList.keySet()) {
            //System.out.println("EXTRACT SEARCH, MetaData hash = " + bsonResultsList.get(key).toString());
            Searchable result = load(bsonResultsList.get(key).toString());
            if (result != null) {
                results.add((MetaData) result);
            }
        }
        search.setSource(source);
        search.setResults(results);
    }

    /**
     * update a search in DB with all dependencies created / updated
     *
     * @param search the object to create / update
     */
    public synchronized void updateInDataBase(Searchable searchable) {
        // No need for it, we just have to call 'set'

        //only if the searchable have to be updated 
//        if (searchable.haveToUpdate()) {
//            //delete the old data
//            BSONObj oldJson = searchable.getOldJson();
//            connection.remove(
//                    "JMeta",
//                    "JMeta",
//                    oldJson.getString("_id"),
//                    oldJson.getString("_revision"));
//            //insert the new
//            connection.insert("JMeta", "JMeta", searchable.toJson());
//            //And look for anything new to create
//            ArrayList<Searchable> lstChildsToCreate
//                    = searchable.getChildsToCreate();
//            for (Searchable itemToCreate : lstChildsToCreate) {
//                createInDataBase(itemToCreate);
//            }
//        }
    }

    /**
     * delete a search in DB with all dependencies created / updated
     *
     * @param search the object to create / update
     * @return true on success, false otherwise
     */
    public synchronized boolean remove(Searchable searchable) {
        return true;
    }

    public Searchable get(String hash) {
        Searchable ret = null;
        try {
            ret = load(hash);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * Create a search in DB with all dependencies created / updated
     *
     * @param searchable the object to create / update
     * @return true on success, false otherwise
     */
    public boolean set(Searchable searchable) {
        // Instead of having "childs to create" and call manually "updateInDatabase", we could have a custom method per 
        // Object 'set'  method that would check if the childs of the object (results of a search for example) are to be created or not.
        // This would keep the database up to date and consistente all the time, but would make the 'set' a little slower. 

        // TODO keep database consistent and up to date.
        if (searchable == null) {
            return false;
        }
        BSONObject bsonObject = searchable.getBson();
        byte[] data = bsonEncoder.encode(bsonObject);
        if (data == null) {
            return false;
        }
        return kyotoDB.set(searchable.getHashCode().getBytes(), data);
    }

    public static String hash(byte[] bloc) {
        // TODO create this method further
        return "TODO_hash_a_faire";
    }

    public static boolean checkHash(String hash, byte[] bloc) {
        return hash.equals(hash(bloc));
    }
}
