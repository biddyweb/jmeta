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
 * @author nico
 * @version $Id: $
 */
public class SearchResultIntegrityCheckerHook implements OperationListener<SearchOperation> {

    private Logger logger = Logger.getLogger(SearchResultIntegrityCheckerHook.class);

    private Map<String, String> metaDataFilters;

    /**
     * <p>Constructor for SearchResultIntegrityCheckerHook.</p>
     *
     * @param filters the meta-data filters that where given to the peer
     */
    public SearchResultIntegrityCheckerHook(final Map<String, String> filters) {
        this.metaDataFilters = filters;
    }

    /** {@inheritDoc} */
    @Override
    public void failed(final SearchOperation operation) {
        //nothing to do here
    }

    /** {@inheritDoc} */
    @Override
    public void complete(final SearchOperation operation) {
        /*
         * Check if every data respects the filters
         */
        for (Entry<MetaPeer, Set<Data>> entry : operation.getPeerResultsMap().entrySet()) {
            for (Data data : entry.getValue()) {
                if (!ModelUtils.matchDataMetaData(data, metaDataFilters)) {
                    logger.warn("The peer " + entry.getKey() + " has given a non wishes data");
                    //TODO invalidate results and add statistic to the peer and potentially add it to black list
                }
            }
        }
    }

}
