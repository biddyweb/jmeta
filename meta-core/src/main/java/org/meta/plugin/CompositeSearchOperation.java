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
package org.meta.plugin;

import java.util.HashSet;
import java.util.Set;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.plugin.SearchOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite search operation.
 *
 * Manages several search operations and aggregate search results.
 *
 * Search results are aggregated asynchronously, meaning that this operation listener may be called several
 * times with a growing result set.
 *
 * This operation never fails but final results could be empty.
 *
 * @author dyslesiq
 */
public class CompositeSearchOperation extends SearchOperation {

    private final Logger logger = LoggerFactory.getLogger(CompositeSearchOperation.class);

    private int operations;

    private int completeOperations;

    private final CompositeSearchListener listener;

    /**
     *
     */
    public CompositeSearchOperation() {
        this.listener = new CompositeSearchListener();
        this.results = new HashSet<>();
        this.peers = new HashSet<>();
    }

    /**
     *
     * @param op the search operation to aggregate
     */
    public void addSearchOperation(final SearchOperation op) {
        operations++;
        op.addListener(listener);
    }

    /**
     * Add the given results to the final set of this operation.
     *
     * @param res results to add
     */
    public void addResults(final Set<Data> res) {
        if (res == null) {
            return;
        }
        this.results.addAll(res);
    }

    /**
     * Called when a sub-operation completed.
     */
    private void operationReceived() {
        this.completeOperations++;
        if (this.completeOperations == this.operations) {
            this.complete();
        } else {
            this.notifyListeners(false);
        }
    }

    /**
     *
     */
    private class CompositeSearchListener implements OperationListener<SearchOperation> {

        @Override
        public void failed(final SearchOperation operation) {
            //There is nothing to do here really, just notify the operation
            logger.debug("Search operation failed: "
                    + operation.getFailureMessage());
            synchronized (CompositeSearchOperation.this) {
                CompositeSearchOperation.this.operationReceived();
            }
        }

        @Override
        public void complete(final SearchOperation operation) {
            synchronized (CompositeSearchOperation.this) {
                CompositeSearchOperation.this.addResults(operation.getResults());
                CompositeSearchOperation.this.peers.addAll(operation.getPeers());
                CompositeSearchOperation.this.operationReceived();
            }
        }
    }
}
