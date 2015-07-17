package org.meta.plugins.SimpleHashMap;

import java.util.HashMap;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.amp.PluginAMPController;
import org.meta.plugins.SimpleHashMap.tcp.commands.Get;

public class SimpleHashMapTcpControler extends PluginAMPController {

    @Override
    protected void registercommands(HashMap<String, Class<? extends PluginAMPCommand>> commands) {
        commands.put("getCommand", Get.class);
    }

}
