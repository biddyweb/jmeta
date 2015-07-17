/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.plugins.SimpleChat;

import org.meta.api.amp.PluginAMPController;
import org.meta.api.plugin.MetaPlugin;
import org.meta.api.ws.AbstractPluginWebServiceControler;

/**
 *
 * @author nico
 */
public class SimpleChat implements MetaPlugin {

    /**
     *
     */
    public static String NAME = "SimpleChat";

    private PluginAMPController ampController;
    private AbstractPluginWebServiceControler wsController;

    /**
     *
     */
    public SimpleChat() {
        ampController = new SimpleChatTcpControler();
        wsController = new SimpleChatWebServiceControler();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public AbstractPluginWebServiceControler getWebServiceController() {
        return wsController;
    }

    @Override
    public PluginAMPController getAMPController() {
        return ampController;
    }

}
