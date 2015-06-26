package org.meta.plugins.SimpleChat;

import java.util.LinkedHashMap;

import org.meta.plugins.SimpleChat.webservice.commands.Chat;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;

public class SimpleChatWebServiceControler extends AbstractPluginWebServiceControler{

    @Override
    protected void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands) {
        commands.put("Chat",     Chat.class);
    }

}
