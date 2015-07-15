/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.plugins.PluginExemple;

import org.meta.api.amp.PluginAMPController;
import org.meta.api.plugin.MetaPlugin;
import org.meta.api.ws.AbstractPluginWebServiceControler;

/**
 *
 * @author nico
 */
public class PluginExemple implements MetaPlugin {

    public static String NAME = "PluginExample";

    private AbstractPluginWebServiceControler wsController;
    private PluginAMPController ampController;

    public PluginExemple() {
        wsController = new PluginExempleWebServiceControler();
        ampController = new PluginExempleTcpControler();
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
