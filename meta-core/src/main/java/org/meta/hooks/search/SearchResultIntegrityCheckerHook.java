package org.meta.hooks.search;

import java.util.Iterator;
import java.util.Map;

import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.plugin.SearchOperation;

public class SearchResultIntegrityCheckerHook implements OperationListener<SearchOperation>{

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
        for(Iterator<Data> i = operation.iterator(); i.hasNext();){
            Data data = i.next();
        }
    }

}
