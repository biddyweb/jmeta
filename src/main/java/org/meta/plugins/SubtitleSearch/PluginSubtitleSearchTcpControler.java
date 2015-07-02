package org.meta.plugins.SubtitleSearch;

import java.util.HashMap;

import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugins.SubtitleSearch.tcp.commands.GetSubtitleCommand;
import org.meta.plugins.SubtitleSearch.tcp.commands.SearchSubtitleCommand;

public class PluginSubtitleSearchTcpControler extends AbstractPluginTCPControler {

    @Override
    protected void registercommands(HashMap<String, Class<? extends AbstractCommand>> commands) {
        commands.put("SearchSubtitleCommand",   SearchSubtitleCommand.class);
        commands.put("GetSubtitleCommand",      GetSubtitleCommand.class);
    }

}
