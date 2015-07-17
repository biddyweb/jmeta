package org.meta.plugins.SimpleChat;

import java.util.LinkedHashMap;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.api.ws.AbstractWebService;
import org.meta.plugins.SimpleChat.webservice.commands.Chat;

public class SimpleChatWebServiceControler extends AbstractPluginWebServiceControler{

    @Override
    protected void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands) {
        commands.put("Chat",     Chat.class);
    }

}
