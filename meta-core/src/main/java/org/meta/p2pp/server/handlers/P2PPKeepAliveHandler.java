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
package org.meta.p2pp.server.handlers;

import org.meta.p2pp.server.P2PPCommandHandler;
import org.meta.p2pp.server.P2PPServer;
import org.meta.p2pp.server.P2PPServerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class P2PPKeepAliveHandler extends P2PPCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPKeepAliveHandler.class);

    /**
     *
     * @param p2ppServer the p2pp server
     */
    public P2PPKeepAliveHandler(final P2PPServer p2ppServer) {
        super(p2ppServer);
    }

    @Override
    public void handle(final P2PPServerRequestContext request) {
        logger.debug("handle keep alive");
        //Nothing to do for keep-alive request
        //request.setStatus(P2PPConstants.ServerRequestStatus.FINISHED);
    }

}
