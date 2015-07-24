/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.api.model;

import org.meta.api.common.MetHash;

/**
 *
 * @author nico
 */
public interface Model {

    /**
     * Close the model and do some clean-up.
     */
    void close();

    /**
     *
     * @param hash of something you want to find in the db
     * @return A searchable object, or null if not found.
     */
    Searchable get(MetHash hash);

    /**
     * @param hash the hash of a data file
     * @return a DataString object or null if the hash does not exists
     */
    DataFile getDataFile(MetHash hash);

    /**
     *
     * @param hash the hash of a data string
     * @return a DataString object or null if the hash does not exists
     */
    DataString getDataString(MetHash hash);

    /**
     *
     * @return The model factory.
     */
    ModelFactory getFactory();

    /**
     *
     * @param hash the hash of a meta data
     * @return a MetaData pointed by his hash or null if the hash is pointed on nothing or if the hash is
     * pointed on a non MetaData object
     */
    MetaData getMetaData(MetHash hash);

    /**
     *
     * @param hash the hash of a search
     * @return a search pointed by his hash. Return null if not found or if the hash is not pointed a Search
     * object
     */
    Search getSearch(MetHash hash);

    /**
     *
     * @param hash the hash of a searchable
     * @return the Search linked to the hash or null if not found
     */
    Searchable getSearchable(MetHash hash);

    /**
     * Delete an object in DB.
     *
     * @param searchable the object to remove from db
     * @return true on success, false otherwise
     */
    boolean remove(Searchable searchable);

    /**
     * Delete an object in DB.
     *
     * @param hash The hash to remove from db
     * @return true on success, false otherwise
     */
    boolean remove(MetHash hash);

    /**
     * Creates or updates a searchable object in database. All children of given object are also
     * created/updated.
     *
     * @param searchable The object to create / update
     *
     * @return true on success, false otherwise
     */
    boolean set(Searchable searchable);

}
