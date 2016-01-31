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
import org.meta.api.common.MetHash;
import org.meta.plugin.tcp.amp.exception.InvalidAMPAskCommand;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;

/**
 * Parse an AMP ask.
 *
 * @author faquin
 * @version $Id: $
 */
public class AMPAskParser extends AMPParser {

    //Do not initialize those variables, because it's made by the mumy
    //in her constructor ;) via the implement method "useContent"
    private String ask;
    private String plugin;
    private String command;
    private MetHash hash;

    /**
     * Call parent.
     *
     * @param bs the data to parse
     * @throws org.meta.plugin.tcp.amp.exception.InvalidAMPCommand if invalid values are encountered in the data
     */
    public AMPAskParser(final byte[] bs) throws InvalidAMPCommand {
        super(bs);
    }

    /** {@inheritDoc} */
    @Override
    protected void useContent(final LinkedHashMap<String, byte[]> content) throws InvalidAMPAskCommand {
        if (content.get("_ask") != null) {
            ask = new String(content.get("_ask"));
        } else {
            ask = null;
        }
        if (content.get("_plugin") != null) {
            plugin = new String(content.get("_plugin"));
        } else {
            plugin = null;
        }
        if (content.get("_command") != null) {
            command = new String(content.get("_command"));
        } else {
            command = null;
        }
        if (content.get("_hash") != null) {
            hash = new MetHash(new String(content.get("_hash")));
        } else {
            hash = null;
        }

        //If one of those parameters is empty or null, throw an exception
        if (ask == null || "".equals(ask)
                || plugin == null || "".equals(plugin)
                || command == null || "".equals(command)
                || hash == null || "".equals(hash)) {
            throw new InvalidAMPAskCommand(
                    ask,
                    plugin,
                    command,
                    hash != null ? hash.toString() : null);
        }

    }

    /**
     * <p>Getter for the field <code>ask</code>.</p>
     *
     * @return ask number
     */
    public String getAsk() {
        return ask;
    }

    /**
     * <p>Getter for the field <code>command</code>.</p>
     *
     * @return command name
     */
    public String getCommand() {
        return command;
    }

    /**
     * <p>Getter for the field <code>hash</code>.</p>
     *
     * @return hash value
     */
    public MetHash getHash() {
        return hash;
    }

    /**
     * <p>Getter for the field <code>plugin</code>.</p>
     *
     * @return plugin name
     */
    public String getPlugin() {
        return plugin;
    }
}
