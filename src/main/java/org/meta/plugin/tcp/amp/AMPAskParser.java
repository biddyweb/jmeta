package org.meta.plugin.tcp.amp;

import java.util.LinkedHashMap;

import org.meta.common.MetHash;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPAskCommand;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;

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
     * @throws NotAValidAMPCommand
     */
    public AMPAskParser(byte[] bs) throws NotAValidAMPCommand{
        super(bs);
    }

    @Override
    protected void useContent(LinkedHashMap<String, byte[]> content) throws NotAValidAMPAskCommand {
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
            throw new NotAValidAMPAskCommand(
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
