package org.meta.plugins.PluginExemple;

import java.util.HashMap;

import org.meta.plugins.PluginExemple.tcp.commands.ExempleCommand;
import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.tcp.AbstractCommand;

public class PluginExempleTcpControler extends AbstractPluginTCPControler {

    @Override
    protected void registercommands(HashMap<String, Class<? extends AbstractCommand>> commands) {
        commands.put("example", ExempleCommand.class);
    }


}
