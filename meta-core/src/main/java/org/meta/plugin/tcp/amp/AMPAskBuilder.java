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
package org.meta.plugin.tcp.amp;

import java.util.LinkedHashMap;
import org.meta.api.amp.AMPBuilder;
import org.meta.api.common.MetHash;

/**
 * Build a new Ask
 * @author faquin
 *
 */
public class AMPAskBuilder extends AMPBuilder{

    /**
     *
     * Create a new AMP Ask command with the given parameters
     *
     * @param ask       the number for the question
     * @param plugin
     * @param command   the command to execute to the other pair
     * @param hash      the hash of the request
     */
    public AMPAskBuilder(String ask, String plugin, String command, MetHash hash) {
        LinkedHashMap<String, byte[]> map = new LinkedHashMap<String, byte[]>();
        map.put("_ask", ask.getBytes());
        map.put("_plugin", plugin.getBytes());
        map.put("_command", command.getBytes());
        map.put("_hash", hash.toString().getBytes());
        //build the message
        super.build(map);
    }

}
