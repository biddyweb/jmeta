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

import org.meta.api.common.MetHash;

/**
 * Define how need to work a tcp command in a plugin.
 *
 * @author faquin
 * @version $Id: $
 */
public abstract class PluginAMPCommand {

    /**
     *
     */
    protected PluginAMPController myTCPControler = null;

    /**
     * <p>Constructor for PluginAMPCommand.</p>
     */
    public PluginAMPCommand() {
    }

    /**
     * Execute the command with the given parameters. This command is called via {@link AskHandlerThread} by
     * the request of an othe machine.
     *
     * @param answer the unique code defining the question
     * @param hash the MetHash object of the question
     * @return The builder for the query
     */
    public abstract AMPBuilder execute(String answer, MetHash hash);

    /**
     * Who's my tcpControler ?
     *
     * @param abstractPluginTCPControler it's it
     */
    public final void setPluginTCPControler(final PluginAMPController abstractPluginTCPControler) {
        this.myTCPControler = abstractPluginTCPControler;
    }
}
