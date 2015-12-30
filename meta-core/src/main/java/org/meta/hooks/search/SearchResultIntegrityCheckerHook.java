package org.meta.hooks.search;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.meta.api.common.MetaPeer;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.plugin.SearchOperation;
import org.meta.model.ModelUtils;

public class SearchResultIntegrityCheckerHook implements OperationListener<SearchOperation>{
    
    private Logger logger = Logger.getLogger(SearchResultIntegrityCheckerHook.class);

    private Map<String, String> metaDataFilters;

    public SearchResultIntegrityCheckerHook( Map<String, String> metaDataFilters) {
        this.metaDataFilters = metaDataFilters;
    }

    @Override
    public void failed(SearchOperation operation) {
        //nothing to do here
    }

    @Override
    public void complete(SearchOperation operation) {
        /*
         * Check if every data respects the filters
         */
        for(Iterator<Entry<MetaPeer, Set<Data>>> i = operation.getPeerResultMap().entrySet().iterator(); i.hasNext();){
            Entry<MetaPeer, Set<Data>> entry = i.next();
            for(Data data : entry.getValue()){
                if(!ModelUtils.matchDataMetaData(data, metaDataFilters)){
                    logger.warn("The peer "+entry.getKey()+" has given a non wishes data");
                }
            }
        }
    }

}
