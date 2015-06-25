package org.meta.plugins.SimpleHashMap;

import java.util.HashMap;

import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugins.SubtitleSearch.tcp.commands.SearchSubtitleCommand;

public class SimpleHashMapTcpControler extends AbstractPluginTCPControler {

    @Override
    protected void registercommands(HashMap<String, Class<? extends AbstractCommand>> commands) {
        commands.put("getCommand", SearchSubtitleCommand.class);
    }

}
