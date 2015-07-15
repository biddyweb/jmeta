/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.plugins.SubtitleSearch;

import org.meta.api.amp.PluginAMPController;
import org.meta.api.plugin.MetaPlugin;
import org.meta.api.ws.AbstractPluginWebServiceControler;

/**
 *
 * @author nico
 */
public class SubtitleSearch implements MetaPlugin {

    public static String NAME = "SubtitleSearch";

    private PluginAMPController ampController;
    private AbstractPluginWebServiceControler wsController;

    /**
     *
     */
    public SubtitleSearch() {
        ampController = new PluginSubtitleSearchTcpControler();
        wsController = new PluginSubtitleSearchWebServiceControler();
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
