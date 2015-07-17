package org.meta.plugins.SubtitleSearch;

import java.util.HashMap;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.amp.PluginAMPController;
import org.meta.plugins.SubtitleSearch.tcp.commands.GetSubtitleCommand;
import org.meta.plugins.SubtitleSearch.tcp.commands.SearchSubtitleCommand;

/**
 *
 * @author nico
 */
public class PluginSubtitleSearchTcpControler extends PluginAMPController {

    @Override
    protected void registercommands(HashMap<String, Class<? extends PluginAMPCommand>> commands) {
        commands.put("SearchSubtitleCommand",   SearchSubtitleCommand.class);
        commands.put("GetSubtitleCommand",      GetSubtitleCommand.class);
    }

}
