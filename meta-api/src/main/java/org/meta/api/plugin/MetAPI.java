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
import java.util.Map;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.dht.MetaDHT;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.ModelStorage;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.api.p2pp.PluginP2PPClient;

/**
 * Interface representing all operations and objects provided by Meta to a plugin.
 *
 * An implementation should be given to each plugin.
 *
 * @author dyslesiq
 */
public interface MetAPI {

    /**
     *
     * @return the meta object model
     */
    ModelStorage getModel();

    /**
     *
     * @return the meta DHT
     */
    MetaDHT getDHT();

    /**
     *
     * @return the peer-to-peer protocol client accessor
     */
    PluginP2PPClient getP2PPClient();

    /**
     *
     * @param searchHash the hash of the search
     * @return the Set of results, might be empty
     */
    Set<Data> getLocalResults(final MetHash searchHash);

    /**
     * High-level search operation.
     *
     * Searches results for the given search hash.
     *
     * Only hash and size of the results will be included for non-local results.
     *
     * If searchLocal is true, get local results immediately.
     *
     * A lookup operation on the dht for the given hash is then performed, and results fetched from peers, if
     * any.
     *
     * @param search the hash to search results for
     * @param searchLocal true if including local results, false otherwise
     * @param getData true to retrieve results data content, false if only retrieving hash
     * @param metaDataKeys the meta data keys to fetch for the results. Can be null.
     * @param metaDataFilters the meta data keys and values the Data should respect.
     *                        only the matchings datas will be received.
     * @return the asynchronous operation of the search
     */
    SearchOperation search(final MetHash search, final boolean searchLocal, final boolean getData,
            final Set<String> metaDataKeys, final Map<String, String> metaDataFilters);

    /**
     * High-level download operation.<br />
     *
     * Downloads the given DataFile from peers.<br/>
     *
     * The given DataFile object must have a valid hash and URI. <br />
     *
     * A lookup operation on the dht for the given hash is performed, and data fetched from peers, if
     * any.<br />
     *
     * The operation will fail if:
     *
     * <ul> <li> no peers are found for the hash</li>
     * <li> All pieces from the file couldn't be fetched</li>
     * <li> Not enough space is remaining on disk</li>
     * <li> If meta can not write to the given destination file</li>
     * </ul>
     *
     * @param destination the URI of the destination file.
     * @return the asynchronous operation of the download
     */
    DownloadOperation download(final DataFile destination);

    /**
     * High-level download operation.<br />
     *
     * Downloads a DataFile from given peers.<br/>
     *
     * The given DataFile object must have a valid hash and URI. <br />
     *
     * The operation will fail if:
     *
     * <ul>
     * <li> All pieces from the file couldn't be fetched</li>
     * <li> Not enough space is remaining on disk</li>
     * <li> If meta can not write to the given destination file</li>
     * </ul>
     *
     * @param destination the URI of the destination file.
     * @param peers the peers to download from
     * @return the asynchronous operation of the download
     */
    DownloadOperation download(final DataFile destination, final Collection<MetaPeer> peers);

    /**
     * Save the searchable in the DB and push his hash to the DHT.
     *
     * @param searchable the searchable to save and push
     * @return true on success, false otherwise
     */
    boolean storePush(final Searchable searchable);

    /**
     * Update a search with the new result.
     *
     * Will check in DB if the Data are already here, in these case, it will not override the data, but just
     * apply changes.
     *
     * After calling this method newSearch will contain newResult in its list of results.
     *
     * @param newSearch the search to update
     * @return the updated search
     */
    Search consolidateSearch(final Search newSearch);

    /**
     * Merges the meta-data of the given data.
     *
     * @param data data to update
     * @return the updated data, or untouched if inexistent in data base
     */
    Data consolidateData(final Data data);

}
