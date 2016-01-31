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
package org.meta.p2pp.server;

/**
 * Base class to be implemented for each server-side command handler in the peer-to-peer protocol.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public abstract class P2PPCommandHandler {

    /**
     * The P2PP server.
     */
    protected final P2PPServer server;

    /**
     * <p>Constructor for P2PPCommandHandler.</p>
     *
     * @param p2ppServer the server
     */
    public P2PPCommandHandler(final P2PPServer p2ppServer) {
        this.server = p2ppServer;
    }

    /**
     * Handles a request.
     *
     * @param request the request to handle
     */
    public abstract void handle(final P2PPServerRequestContext request);

}
