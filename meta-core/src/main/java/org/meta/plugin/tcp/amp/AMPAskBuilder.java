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
