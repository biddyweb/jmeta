package org.meta.plugins.PluginExemple;

import java.util.HashMap;

import org.meta.plugins.PluginExemple.webservice.commands.ExempleWsCommand;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;

public class PluginExempleWebServiceControler extends AbstractPluginWebServiceControler{

	@Override
	protected void registercommands(
			HashMap<String, Class<? extends AbstractWebService>> commands) {
		commands.put("example", ExempleWsCommand.class);
	}

}
