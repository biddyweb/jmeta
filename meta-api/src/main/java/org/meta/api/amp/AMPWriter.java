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
package org.meta.api.amp;

import java.net.InetAddress;
import java.util.concurrent.Future;
import org.meta.api.common.MetHash;

/**
 *
 * @author nico
 */
public interface AMPWriter {

    /**
     * Ask a question to the given address, with the given parameters.
     *
     * @param adress who do we call ?
     * @param port on wich port do we call ?
     * @param plugin which plugin is concerned ?
     * @param command what command to we ask him to execute ?
     * @param hash on wich hash ?
     * @param listenner after getting result, who is getting the callback ?
     * @return the sender thread for join purposes
     */
    Future<?> askTo(InetAddress adress, String plugin, String command, MetHash hash,
            AMPResponseCallback listenner, int port);

}
