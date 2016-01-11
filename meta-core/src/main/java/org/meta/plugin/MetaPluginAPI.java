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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.FindPeersOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.ModelStorage;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.api.plugin.DownloadOperation;
import org.meta.api.plugin.MetAPI;
import org.meta.api.plugin.SearchOperation;
import org.meta.controler.MetaController;
import org.meta.hooks.search.SearchResultIntegrityCheckerHook;
import org.meta.p2pp.client.MetaP2PPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the bridge between plugins and the Meta operations.
 *
 * Every operation a plugin needs to execute should appear here.
 *
 * @author dyslesiq
 */
public final class MetaPluginAPI implements MetAPI {

    private final Logger logger = LoggerFactory.getLogger(MetaPluginAPI.class);

    private final MetaController metaController;

    /**
     *
     * @param controller the meta controller
     */
    public MetaPluginAPI(final MetaController controller) {
        this.metaController = controller;
    }

    @Override
    public ModelStorage getModel() {
        return this.metaController.getModel();
    }

    @Override
    public MetaDHT getDHT() {
        return this.metaController.getDht();
    }

    @Override
    public MetaP2PPClient getP2PPClient() {
        return this.metaController.getP2PPClient();
    }

    @Override
    public SearchOperation search(final MetHash searchHash, final boolean searchLocal,
            final boolean getData, final Set<String> metaDataKeys, final Map<String, String> metaDataFilters) {
        CompositeSearchOperation op = new CompositeSearchOperation();

        if (searchLocal) {
            op.addResults(null, getLocalResults(searchHash));
        }

        //Find peers for the given hash
        FindPeersOperation peersOperation = getDHT().findPeers(searchHash);
        peersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(final FindPeersOperation operation) {
                op.setFailed(operation.getFailureMessage());
            }

            @Override
            public void complete(final FindPeersOperation operation) {
                // foreach peer found, launch a contact to fetch results
                Collection<MetaPeer> peers = operation.getPeers();

                if (peers.isEmpty()) {
                    logger.debug("Search: empty peers!");
                    op.complete();
                } else {
                    for (MetaPeer peer : peers) {
                        SearchOperation peerSearchOperation;

                        if (!getData && metaDataKeys == null) {
                            peerSearchOperation = getP2PPClient().search(peer, metaDataFilters, searchHash);
                        } else if (!getData && metaDataKeys != null) {
                            peerSearchOperation = getP2PPClient().searchMeta(peer, metaDataFilters, metaDataKeys, searchHash);
                        } else {
                            peerSearchOperation = getP2PPClient().searchGet(peer, metaDataFilters, metaDataKeys, searchHash);
                        }
                        op.addSearchOperation(peerSearchOperation);
                        //Add all core listeners to search operation
                        //Notice that invocation order should be kept !
                        peerSearchOperation.addListener(new SearchResultIntegrityCheckerHook(metaDataFilters));
                    }
                }
            }
        });
        return op;
    }

    @Override
    public DownloadOperation download(final DataFile destination) {
        DownloadOperation d = new DownloadOperation(destination);

        //Find peers for the given hash
        FindPeersOperation peersOperation = getDHT().findPeers(destination.getHash());
        peersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(final FindPeersOperation operation) {
                d.setFailed(operation.getFailureMessage());
            }

            @Override
            public void complete(final FindPeersOperation operation) {
                Collection<MetaPeer> peers = operation.getPeers();

                if (peers == null || peers.isEmpty()) {
                    d.setFailed("No peers to download from");
                } else {
                    new DownloadManager(getP2PPClient(), d, peers).start();
                }
            }
        });
        return d;
    }

    @Override
    public DownloadOperation download(final DataFile destination, final Collection<MetaPeer> peers) {
        DownloadOperation d = new DownloadOperation(destination);

        new DownloadManager(getP2PPClient(), d, peers).start();
        return d;
    }

    /**
     * Get results from the local database.
     *
     * The given hash is interpreted as a search.
     *
     * If not a search, or inexistent in database, an empty set of results is returned.
     *
     * @param searchHash the hash of the search
     * @return the results for the given search
     */
    @Override
    public Set<Data> getLocalResults(final MetHash searchHash) {
        Set<Data> results = new HashSet<>();
        Search search = getModel().getSearch(searchHash);

        if (search != null) {
            results.addAll(search.getResults());
        }
        return results;
    }

    @Override
    public Data consolidateData(final Data data) {
        Data resultDB = getModel().getData(data.getHash());

        if (resultDB != null) {
            //TODO talk about meta-data merge strategy
            //get new description
            data.getMetaDataMap().addAll(resultDB.getMetaDataMap().entrySet());
        }
        return data;
    }

    @Override
    public Search consolidateSearch(final Search newSearch) {
        //try to get the same from model
        //TODO talk about search merge strategy
        Search searchDB = getModel().getSearch(newSearch.getHash());
        if (searchDB == null) {
            searchDB = newSearch;
        }
        searchDB.addResults(newSearch.getResults());
        return searchDB;
    }

    @Override
    public boolean storePush(final Searchable searchable) {
        boolean status = getModel().set(searchable);
        getDHT().push(searchable.getHash());
        return status;
    }

}
