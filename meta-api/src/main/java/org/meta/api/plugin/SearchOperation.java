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
package org.meta.api.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.log4j.Logger;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetaPeer;
import org.meta.api.model.Data;

/**
 * An asynchronous operation representing the process of searching {@link Data} results through the
 * peer-to-peer protocol and/or the local storage.
 *
 * @author dyslesiq
 */
public class SearchOperation extends AsyncOperation implements Iterable<Data> {

    private final Logger logger = Logger.getLogger(SearchOperation.class);

    /**
     * Peers to results association.
     */
    protected Map<MetaPeer, Set<Data>> results;

    private int nbResults;

    /**
     *
     */
    public SearchOperation() {
        results = new HashMap<>();
    }

    /**
     *
     * @param peer the peer that gave us the results
     * @param res the result list
     */
    public void addResults(final MetaPeer peer, final Set<Data> res) {
        if (res.size() > 0) {
            this.results.put(peer, res);
            nbResults += res.size();
        }
    }

    /**
     *
     * @return the peers we searched from
     */
    public Set<MetaPeer> getPeers() {
        return this.results.keySet();
    }

    /**
     *
     * @return the map associating peers and results
     */
    public Map<MetaPeer, Set<Data>> getPeerResultsMap() {
        return results;
    }

    /**
     *
     * @return the total number of results
     */
    public int getNbResults() {
        return this.nbResults;
    }

    @Override
    public Iterator<Data> iterator() {
        //This need to be tested TODO
        return new Iterator<Data>() {
            private Iterator<MetaPeer> itPeers = null;
            private Iterator<Data> itData = null;
            private int nbRead = 0;

            //The iterator call is only supposed to happen when the data retrieving
            //is over, so it only will be a read only opperation whithout concurency
            //problem => wrong! in case of a CompositeSearchOperation, we might iterate over incomplete data.
            //TODO see if there gonna be concurrency issues.
            @Override
            public boolean hasNext() {
                return nbRead < nbResults;
            }

            @Override
            public Data next() {
                nbRead++;
                /*
                 * if cMetaPeer equals null, it means it's the first time we call
                 * next.
                 * initialise cMetaPeer with the first row key
                 * then initialise first row value iterator itData
                 *
                 * otherwise, it means that we already have called this method.
                 * So we are somewhere on a row, we need to check if we are at
                 * the end of the row and if we can go further on the next one.
                 */
                if (itPeers == null) {
                    itPeers = results.keySet().iterator();
                    if (!itPeers.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    itData = results.get(itPeers.next()).iterator();
                } else if (!itData.hasNext() && itPeers.hasNext()) {
                    itData = results.get(itPeers.next()).iterator();
                }
                if (!itData.hasNext()) {
                    throw new NoSuchElementException();
                }
                return itData.next();
            }
        };
    }
}
