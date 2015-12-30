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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetaPeer;
import org.meta.api.model.Data;

/**
 * An asynchronous operation representing the process of searching {@link Data} results through the
 * peer-to-peer protocol and/or the local storage.
 *
 * @author dyslesiq
 */
public class SearchOperation extends AsyncOperation implements Iterable<Data>{

    protected HashMap<MetaPeer, Set<Data>> results;
    private   int                          nbResults = 0; 

    public SearchOperation(){
        results  = new HashMap<MetaPeer, Set<Data>>();
    }

    /**
     *
     * @param res the results
     */
    public void addResults(final MetaPeer peer, final Set<Data> res) {
        if(res.size() > 0){
            this.results.put(peer, res);
            nbResults = nbResults + res.size();
        }
    }

    /**
     *
     * @return the peers we searched from
     */
    public Set<MetaPeer> getPeers() {
        return this.results.keySet();
    }

    public HashMap<MetaPeer, Set<Data>> getPeerResultMap() {
        return results;
    }
    
    public int getNbResults(){
        return nbResults;
    }
    @Override
    public Iterator<Data> iterator() {
        //This need to be tested TODO
        return new Iterator<Data>() {
            private Iterator<MetaPeer> itResults = null;
            private MetaPeer           cMetaPeer = null;
            private Iterator<Data>     itData    = null;
            private int nbRead = 0;
            
            //The iterator call is only supposed to append when the data retrieving
            //is over, so it only will be a read only opperation whithout concurency
            //problem
            @Override
            public boolean hasNext() {
                return nbRead < nbResults;
            }

            @Override
            public Data next() {
                nbRead++;
                /*
                 * if the first iterator is null, it's the first time we are called
                 */
                if(itResults == null){
                    itResults = results.keySet().iterator();
                }
                /*
                 * if cMetaPeer equals null, it means it's the first time we call
                 * next.
                 * initialise cMetaPeer with the first row key
                 * then initialise first row value iterator itData
                 * 
                 * otherwise, it means that we already have call this method.
                 * So we are somewhere on a row, we need to check if we are at
                 * the end of the row and if we can go further on the next one.
                 */
                if(cMetaPeer == null){
                    cMetaPeer = itResults.next();
                    itData    = results.get(cMetaPeer).iterator();
                }else{
                    //Make a step to the next row only if the current one is
                    //done and if there is a next one
                    if(!itData.hasNext() && itResults.hasNext()){
                        cMetaPeer = itResults.next();
                        itData    = results.get(cMetaPeer).iterator();
                    }
                }
                //At this point, may or may not give a next result.
                return itData.hasNext() ? itData.next() : null;
            }
        };
    }
}
