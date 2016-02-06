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
package org.meta.api.p2pp;

import java.util.Map;
import java.util.Set;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.plugin.SearchOperation;

/**
 * Interface providing all peer-to-peer protocol commands to plugins.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public interface PluginP2PPClient {

    /**
     * Sends a keep-alive request to the given server peer.
     *
     * @param peer the peer to send the request to
     * @return the asynchronous operation
     */
    AsyncOperation keepAlive(final MetaPeer peer);

    /**
     * Search results for the given hash.
     *
     * @param peer the peer to send the request to
     * @param metaDataFilters the attendee metadas values
     * @param hash the hash to search results for
     * @return the search operation representing the outcome of the asynchronous search
     */
    SearchOperation search(final MetaPeer peer, final Map<String, String> metaDataFilters, final MetHash hash);

    /**
     * Search results for the given hashes.
     *
     * @param peer the peer to send the request to
     * @param metaDataFilters the attendee metadas values
     * @param hashes the hashes to search results for
     * @return the search operation representing the outcome of the asynchronous search
     */
    SearchOperation search(final MetaPeer peer, final Map<String, String> metaDataFilters,
            final MetHash... hashes);

    /**
     * Search results for the given hash with associated meta-data.
     *
     * @param peer the peer to send the request to
     * @param metaDataFilters the attendee metadas values
     * @param metaDataKeys the meta-data keys to fetch with results. Can be null or empty.
     * @param hash the hash to search results for
     * @return the search operation representing the outcome of the asynchronous search
     */
    SearchOperation searchMeta(final MetaPeer peer, final Map<String, String> metaDataFilters,
            final Set<String> metaDataKeys, final MetHash hash);

    /**
     * Search results for the given hashes with associated meta-data.
     *
     * @param peer the peer to send the request to
     * @param metaDataFilters the attendee metadas values
     * @param metaDataKeys the meta-data keys to fetch with results. Can be null or empty.
     * @param hashes the hashes to search results for
     * @return the search operation representing the outcome of the asynchronous search
     */
    SearchOperation searchMeta(final MetaPeer peer, final Map<String, String> metaDataFilters,
            final Set<String> metaDataKeys,
            final MetHash... hashes);

    /**
     * Search results for the given hash with associated meta-data and data.
     *
     * @param peer the peer to send the request to
     * @param metaDataFilters the attendee metadas values
     * @param metaDataKeys the meta-data keys to fetch with results. Can be null or empty.
     * @param hash the hash to search results for
     * @return the search operation representing the outcome of the asynchronous search
     */
    SearchOperation searchGet(final MetaPeer peer, final Map<String, String> metaDataFilters,
            final Set<String> metaDataKeys, final MetHash hash);

    /**
     * Search results for the given hashes with associated meta-data and data.
     *
     * @param peer the peer to send the request to
     * @param metaDataFilters the attendee metadas values
     * @param metaDataKeys the meta-data keys to fetch with results. Can be null or empty.
     * @param hashes the hashes to search results for
     * @return the search operation representing the outcome of the asynchronous search
     */
    SearchOperation searchGet(final MetaPeer peer, final Map<String, String> metaDataFilters,
            final Set<String> metaDataKeys, final MetHash... hashes);

    /**
     * Get a block of data for the given {@link org.meta.api.model.Data} hash.
     *
     * @param peer the peer to send the request to
     * @param hash the hash of the {@link org.meta.api.model.Data} to get the block from.
     * @param pieceIndex the piece index of the Data to fetch
     * @param byteOffset the byte offset within the piece of the block to fetch
     * @param length the length of the block to fetch.
     * @return The GetOperation representing the outcome of the asynchronous process
     */
    GetOperation get(final MetaPeer peer, final MetHash hash, final int pieceIndex, final int byteOffset,
            final int length);

}
