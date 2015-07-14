package org.meta.plugins.SimpleChat;

import java.util.HashMap;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.amp.PluginAMPController;

import org.meta.plugins.SimpleChat.tcp.commands.GetLastMessage;

public class SimpleChatTcpControler extends PluginAMPController {

    @Override
    protected void registercommands(HashMap<String, Class<? extends PluginAMPCommand>> commands) {
        commands.put("getLastMessages", GetLastMessage.class);
    }

}
