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
package org.meta.plugin.tcp.amp.exception;

/**
 * <p>InvalidAMPAskCommand class.</p>
 *
 * @author nico
 * @version $Id: $
 */
public class InvalidAMPAskCommand extends InvalidAMPCommand {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * <p>Constructor for InvalidAMPAskCommand.</p>
     *
     * @param ask the amp ask
     * @param plugin the plugin name
     * @param command the command
     * @param hash the hash
     */
    public InvalidAMPAskCommand(final String ask,
            final String plugin,
            final String command,
            final String hash) {
        super("The received message is not valid because"
                + "one of those parameters are empty or null : "
                + "plugin : " + plugin
                + "ask : " + ask
                + " ; command : " + command
                + " ; hash : " + hash);

    }

}
