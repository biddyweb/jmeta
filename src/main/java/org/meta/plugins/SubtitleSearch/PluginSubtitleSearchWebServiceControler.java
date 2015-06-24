package org.meta.plugins.SubtitleSearch;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.meta.plugins.SubtitleSearch.webservice.commands.AddSubtitle;
import org.meta.plugins.SubtitleSearch.webservice.commands.GetSubtitles;
import org.meta.plugins.SubtitleSearch.webservice.commands.SearchSubtitles;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;

public class PluginSubtitleSearchWebServiceControler extends AbstractPluginWebServiceControler{

    @Override
    protected void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands) {
        commands.put("searchSubtitles",     SearchSubtitles.class);
        commands.put("getSubtitles",         GetSubtitles.class);
        commands.put("addSubtitle",         AddSubtitle.class);
    }

}
