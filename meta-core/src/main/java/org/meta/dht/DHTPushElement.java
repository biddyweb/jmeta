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
package org.meta.dht;

import org.meta.api.common.MetHash;

/**
 * An element holding simple information about a hash pushed over the DHT.
 *
 */
public class DHTPushElement {

    private MetHash search = null;
    private long nextPushTime = 0;
    private long expiration = 0;

    /**
     *
     * @param hash the hash to push
     * @param nextPush the next time to push
     * @param expire expiration timestamp of this element
     */
    public DHTPushElement(final MetHash hash, final long nextPush,
            final long expire) {
        this.search = hash;
        this.nextPushTime = nextPush;
        this.expiration = expire;
    }

    /**
     * @return the next time the hash needs to be pushed
     */
    public long getNextPushTime() {
        return nextPushTime;
    }

    /**
     * @return the expiration timestamp of this element
     */
    public long getExpiration() {
        return expiration;
    }

    /**
     * @return the hash to push
     */
    public MetHash getHash() {
        return search;
    }

    /**
     *
     * @param nextPush the next time the hash needs to be pushed
     */
    public void setNextPushTime(final long nextPush) {
        this.nextPushTime = nextPush;
    }

    /**
     *
     * @param exp expiration timestamp
     */
    public void setExpiration(final long exp) {
        this.expiration = exp;
    }

}
