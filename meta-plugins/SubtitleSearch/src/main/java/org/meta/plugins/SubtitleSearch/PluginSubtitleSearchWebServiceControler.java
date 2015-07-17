package org.meta.plugins.SubtitleSearch;

import java.util.LinkedHashMap;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.api.ws.AbstractWebService;
import org.meta.plugins.SubtitleSearch.webservice.commands.AddSubtitle;
import org.meta.plugins.SubtitleSearch.webservice.commands.GetSubtitles;
import org.meta.plugins.SubtitleSearch.webservice.commands.SearchSubtitles;

/**
 *
 * @author nico
 */
public class PluginSubtitleSearchWebServiceControler extends AbstractPluginWebServiceControler{

    @Override
    protected void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands) {
        commands.put("searchSubtitles",     SearchSubtitles.class);
        commands.put("getSubtitles",         GetSubtitles.class);
        commands.put("addSubtitle",         AddSubtitle.class);
    }

}
