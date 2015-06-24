package org.meta.plugins.PluginExemple;

import java.util.LinkedHashMap;

import org.meta.plugins.PluginExemple.webservice.commands.ExempleWsCommand;
import org.meta.plugins.PluginExemple.webservice.commands.SecondStateExempleWsCommand;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;

public class PluginExempleWebServiceControler extends AbstractPluginWebServiceControler{

    @Override
    protected void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands) {
        commands.put("example", ExempleWsCommand.class);
        commands.put("secondExample", SecondStateExempleWsCommand.class);
    }

}
