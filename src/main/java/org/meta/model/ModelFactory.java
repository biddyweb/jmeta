 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.model;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.meta.common.MetHash;
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

    public Search getSearch() {
        return (Search) pools.get(ModelType.SEARCH).getInstance();
    }

    /**
     * Convenience search builder
     *
     * @param hash The hash of the created search
     *
     * @return The search with specified hash.
     */
    public Search getSearch(MetHash hash) {
        Search search = this.getSearch();

        search.setHash(hash);
        return search;
    }

    /**
     * Convenience Search builder.
     *
     * @param hash
     * @param source
     * @param results
     *
     * @return The fully-initialized search
     */
    public Search getSearch(MetHash hash, Searchable source, MetaData result) {
        Search search = this.getSearch();

        search.setHash(hash);
        search.setSource(source);
        search.setResult(result);
        return search;
    }

    public DataString getDataString() {
        return (DataString) pools.get(ModelType.DATASTRING).getInstance();
    }

    /**
     * Convenience datastring builder
     *
     * @param hash The hash of the created datastring
     *
     * @return The datastring with specified hash.
     */
    public DataString getDataString(MetHash hash) {
        DataString data = this.getDataString();

        data.setHash(hash);
        return data;
    }

    /**
     * Convenience DataString builder.
     *
     * @param hash
     * @param data
     *
     * @return The fully-initialized dataString
     */
    public DataString getDataString(MetHash hash, String data) {
        DataString dataString = this.getDataString();

        dataString.setHash(hash);
        dataString.setString(data);
        return dataString;
    }

    /**
     * @return An instance of DataFile.
     */
    public DataFile getDataFile() {
        return (DataFile) pools.get(ModelType.DATAFILE).getInstance();
    }

    /**
     * Convenience datafile builder
     *
     * @param hash The hash of the created datafile
     *
     * @return The datafile with specified hash.
     */
    public DataFile getDataFile(MetHash hash) {
        DataFile data = this.getDataFile();

        data.setHash(hash);
        return data;
    }

    /**
     * Convenience DataFile builder.
     *
     * @param hash
     * @param file
     *
     * @return The fully-initialized dataFile
     */
    public DataFile getDataFile(MetHash hash, File file) {
        DataFile dataFile = this.getDataFile();

        dataFile.setHash(hash);
        dataFile.setFile(file);
        return dataFile;
    }

    public MetaData getMetaData() {
        return (MetaData) pools.get(ModelType.METADATA).getInstance();
    }

    /**
     * Convenience MetaData builder.
     *
     * @param hash
     *
     * @return An instance of metaData with specified hash.
     */
    public MetaData getMetaData(MetHash hash) {
        MetaData metaData = this.getMetaData();

        metaData.setHash(hash);
        return metaData;
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
    public MetaData getMetaData(MetHash hash, List<Data> datas, List<MetaProperty> props) {
        MetaData metaData = this.getMetaData();

        metaData.setHash(hash);
        metaData.setLinkedData(datas);
        metaData.setProperties(props);
        return metaData;
    }
}
