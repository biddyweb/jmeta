package org.meta.hooks.search;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
import org.meta.api.common.MetaPeer;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.plugin.SearchOperation;
import org.meta.model.ModelUtils;

/**
 * Completion handler that is registered by meta-core to check that results returned are consistent.
 *
 * @author dyslesiq
 */
public class SearchResultIntegrityCheckerHook implements OperationListener<SearchOperation> {

    private Logger logger = Logger.getLogger(SearchResultIntegrityCheckerHook.class);

    private Map<String, String> metaDataFilters;

    /**
     *
     * @param filters the meta-data filters that where given to the peer
     */
    public SearchResultIntegrityCheckerHook(final Map<String, String> filters) {
        this.metaDataFilters = filters;
    }

    @Override
    public void failed(final SearchOperation operation) {
        //nothing to do here
    }

    @Override
    public void complete(final SearchOperation operation) {
        /*
         * Check if every data respects the filters
         */
        for (Entry<MetaPeer, Set<Data>> entry : operation.getPeerResultMap().entrySet()) {
            for (Data data : entry.getValue()) {
                if (!ModelUtils.matchDataMetaData(data, metaDataFilters)) {
                    logger.warn("The peer " + entry.getKey() + " has given a non wishes data");
                    //TODO invalidate results and add statistic to the peer and potentially add it to black list
                }
            }
        }
    }

}
