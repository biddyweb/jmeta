package org.meta.plugins.PluginExemple;

import java.util.HashMap;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.amp.PluginAMPController;
import org.meta.plugins.PluginExemple.tcp.commands.ExempleCommand;

public class PluginExempleTcpControler extends PluginAMPController {

    @Override
    protected void registercommands(HashMap<String, Class<? extends PluginAMPCommand>> commands) {
        commands.put("example", ExempleCommand.class);
    }


}
