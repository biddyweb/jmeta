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
import java.util.HashSet;
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
public class SearchOperation extends AsyncOperation {

    /**
     * The results.
     */
    protected Set<Data> results;

    /**
     * The peers we searched from.
     */
    protected Collection<MetaPeer> peers;

    /**
     *
     * @return the results found once the search completed.
     */
    public Set<Data> getResults() {
        return this.results;
    }

    /**
     *
     * @param res the results
     */
    public void setResults(final Set<Data> res) {
        this.results = res;
    }

    /**
     *
     * @return the peers we searched from
     */
    public Collection<MetaPeer> getPeers() {
        return this.peers;
    }

    /**
     *
     * @param peer the peer we search from
     */
    public void setPeer(final MetaPeer peer) {
        if (this.peers == null) {
            this.peers = new HashSet<>();
        }
        this.peers.add(peer);
    }

    /**
     *
     * @param searchPeers the peers we search from
     */
    public void setPeers(final Collection<MetaPeer> searchPeers) {
        this.peers = searchPeers;
    }

}
