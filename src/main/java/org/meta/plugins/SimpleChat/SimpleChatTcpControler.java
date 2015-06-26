package org.meta.plugins.SimpleChat;

import java.util.HashMap;

import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugins.SimpleChat.tcp.commands.GetLastMessage;

public class SimpleChatTcpControler extends AbstractPluginTCPControler {

    @Override
    protected void registercommands(HashMap<String, Class<? extends AbstractCommand>> commands) {
        commands.put("getLastMessages", GetLastMessage.class);
    }

}
