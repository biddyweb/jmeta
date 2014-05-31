package org.meta.plugins.SubtitleSearch;

import java.util.HashMap;

import org.meta.plugins.PluginExemple.webservice.commands.ExempleWsCommand;
import org.meta.plugins.SubtitleSearch.webservice.commands.SearchSubtitles;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;

public class PluginSubtitleSearchWebServiceControler extends AbstractPluginWebServiceControler{

	@Override
	protected void registercommands(
			HashMap<String, Class<? extends AbstractWebService>> commands) {
		commands.put("SearchSubtitles", SearchSubtitles.class);
	}

}
