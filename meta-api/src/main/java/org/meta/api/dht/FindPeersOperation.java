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
package org.meta.api.dht;

import org.meta.api.common.MetaPeer;
import org.meta.api.common.AsyncOperation;
import java.util.Collection;

/**
 *
 * An asynchronous DHT operation representing the process of finding peers for a
 * given hash.
 *
 * The implementation details is let to the extending class.
 *
 * @author nico
 * @version $Id: $
 */
public abstract class FindPeersOperation extends AsyncOperation {

    /**
     * The peers found.
     */
    protected Collection<MetaPeer> peers;

    /**
     * <p>Getter for the field <code>peers</code>.</p>
     *
     * @return The peers found.
     */
    public Collection<MetaPeer> getPeers() {
        return peers;
    }

}
