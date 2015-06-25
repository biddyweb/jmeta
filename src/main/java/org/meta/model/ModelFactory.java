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
    
    protected Search getSearch() {
        return (Search) pools.get(ModelType.SEARCH).getInstance();
    }
    
    public Search createSearch(Searchable source, MetaData result, List<Data> datas){
         Search search = (Search) pools.get(ModelType.SEARCH).getInstance();
         search.setSource(source);
         search.setResult(result);
         search.setLinkedData(datas);
         return search;
    }

    protected DataString getDataString() {
        return (DataString) pools.get(ModelType.DATASTRING).getInstance();
    }

    /**
     * Convenience DataString builder.
     *
     * @param hash
     * @param data
     *
     * @return The fully-initialized dataString
     */
    public DataString createDataString(String data) {
        DataString dataString = this.getDataString();
        dataString.setString(data);
        return dataString;
    }

    /**
     * @return An instance of DataFile.
     */
    protected DataFile getDataFile() {
        return (DataFile) pools.get(ModelType.DATAFILE).getInstance();
    }

    /**
     * Convenience DataFile builder.
     *
     * @param hash
     * @param file
     *
     * @return The fully-initialized dataFile
     */
    public DataFile createDataFile(File file) {
        DataFile dataFile = this.getDataFile();
        dataFile.setFile(file);
        return dataFile;
    }

    protected MetaData getMetaData() {
        return (MetaData) pools.get(ModelType.METADATA).getInstance();
    }

    /**
     * Convenience MetaData builder.
     *
     * @param hash
     * @param datas
     * @param props
     *
     * @return The fully-initialized MetaData.
     */
    public MetaData createMetaData(TreeSet<MetaProperty> props) {
        MetaData metaData = this.getMetaData();
        metaData.setProperties(props);
        return metaData;
    }

    public Searchable newInstance(Class clazz) throws InstantiationException, IllegalAccessException {
        return (Searchable) clazz.newInstance();
    }

    public void updateFromNewtork(Search search, Searchable source, Searchable result, ArrayList<Data> linked) {
        search.set(source, result, linked);
    }
}
