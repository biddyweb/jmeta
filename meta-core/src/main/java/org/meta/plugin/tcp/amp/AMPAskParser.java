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
 * parse an AMP ask
 * @author faquin
 *
 */
public class AMPAskParser extends AMPParser{

    //Do not initialize those variables, because it's made by the mumy
    //in her constructor ;) via the implement method "useContent"
    private String    ask       ;
    private String    plugin    ;
    private String    command   ;
    private MetHash   hash      ;

    /**
     * Call parent
     * @param bs
     * @throws InvalidAMPCommand
     */
    public AMPAskParser(byte[] bs) throws InvalidAMPCommand{
        super(bs);
    }

    @Override
    protected void useContent(LinkedHashMap<String, byte[]> content) throws InvalidAMPAskCommand {
        ask     = content.get("_ask")    != null ? new String(content.get("_ask"))     : null;
        plugin  = content.get("_plugin") != null ? new String(content.get("_plugin"))  : null;
        command = content.get("_command")!= null ? new String(content.get("_command")) : null;
        hash    = content.get("_hash")   != null ? new MetHash(new String(content.get("_hash"))) : null;
          
        //If one of those parameters is empty or null, throw an exception
        if( ask     == null || "".equals(ask)        ||
            plugin  == null || "".equals(plugin)     ||
            command == null || "".equals(command)    ||
            hash    == null || "".equals(hash)
        ){
            throw new InvalidAMPAskCommand(
                    ask, 
                    plugin, 
                    command, 
                    hash != null ? hash.toString() : null);
        }

    }
    /**
     * 
     * @return ask number
     */
    public String getAsk() {
        return ask;
    }

    /**
     * 
     * @return command name
     */
    public String getCommand() {
        return command;
    }

    /**
     * 
     * @return hash value
     */
    public MetHash getHash() {
        return hash;
    }

    /**
     * 
     * @return plugin name
     */
    public String getPlugin() {
        return plugin;
    }
}
