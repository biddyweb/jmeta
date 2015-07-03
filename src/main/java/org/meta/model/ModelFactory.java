 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.meta.model.DataFile;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.ModelType;
import org.meta.model.Search;
import org.meta.model.Searchable;

/**
 * This factory allow anyone to interract safely with the model
 * 
 * @author nico
 */
public class ModelFactory {

    Map<ModelType, InstancePool> pools;

    public ModelFactory() {
        pools = new HashMap<>();
        for (ModelType type : ModelType.values()) {
            pools.put(type, new InstancePool(type.getTypeClass()));
        }
    }

    /**
     *
     * @param type The model type to get
     *
     * @return An instance of the asked class.
     */
    public Searchable getInstance(ModelType type) {
        return pools.get(type).getInstance();
    }
    
    /**
     * 
     * @return a fresh search from pool
     */
    protected Search getSearch() {
        return (Search) pools.get(ModelType.SEARCH).getInstance();
    }
    
    /**
     * Create a Search with given params
     * @param source    search's source
     * @param metaData  search's metaData
     * @param datas     search's results
     * 
     * @return a brand new search
     */
    public Search createSearch(Searchable source, MetaData metaData, List<Data> datas){
         Search search = (Search) pools.get(ModelType.SEARCH).getInstance();
         search.setSource(source);
         search.setMetaData(metaData);
         if(datas != null)
             search.setLinkedData(datas);
         return search;
    }

    /**
     * 
     * @return a fresh dataString
     */
    protected DataString getDataString() {
        return (DataString) pools.get(ModelType.DATASTRING).getInstance();
    }

    /**
     * Build a new DataString with the given params
     * @param data a string containing what it please you
     *
     * @return The fully-initialized dataString
     */
    public DataString createDataString(String data) {
        DataString dataString = this.getDataString();
        dataString.setString(data);
        return dataString;
    }

    /**
     * @return a fresh DataFile
     */
    protected DataFile getDataFile() {
        return (DataFile) pools.get(ModelType.DATAFILE).getInstance();
    }

    /**
     * Build a new DataString with given params
     *
     * @param file  The file you want DataFile to point to
     *
     * @return The fully-initialized dataFile
     */
    public DataFile createDataFile(File file) {
        DataFile dataFile = this.getDataFile();
        dataFile.setFile(file);
        return dataFile;
    }

    /**
     * 
     * @return a fresh MetaData
     */
    protected MetaData getMetaData() {
        return (MetaData) pools.get(ModelType.METADATA).getInstance();
    }

    /**
     * Build a MetaData with given params
     *
     * @param props the MetaProperties representing the metaData
     *
     * @return The fully-initialized MetaData.
     */
    public MetaData createMetaData(TreeSet<MetaProperty> props) {
        MetaData metaData = this.getMetaData();
        metaData.setProperties(props);
        return metaData;
    }
    
    /**
     * Create a new instance of a Content type
     * Model object have protected constructor, so you can't call a 
     * clazz.newinstance on those ? extends Searchable.class ;-)
     * 
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Searchable newInstance(Class clazz) throws InstantiationException, IllegalAccessException {
        return (Searchable) clazz.newInstance();
    }

    /**
     * After retrieving an search from network, you may want to give him his childs
     * Call this method.
     * 
     * TODO check that the given children have the same hash that the search want
     * @param search
     * @param source
     * @param metaData
     * @param linked
     */
    public void updateFromNewtork(Search search, Searchable source, MetaData metaData, ArrayList<Data> linked) {
        search.set(source, metaData, linked);
    }
}
