/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.plugins.PluginExemple;

import org.meta.api.plugin.MetAPI;
import org.meta.api.plugin.MetaPlugin;
import org.meta.api.ws.AbstractPluginWebServiceController;

/**
 * <p>PluginExemple class.</p>
 *
 * @author nico
 * @version $Id: $Id
 */
public class PluginExemple implements MetaPlugin {

    /**
     *
     */
    public static String NAME = "PluginExample";

    private MetAPI pluginAPI;

    private AbstractPluginWebServiceController wsController;

    //private PluginAMPController ampController;
    /**
     * <p>Constructor for PluginExemple.</p>
     */
    public PluginExemple() {
        //ampController = new PluginExempleTcpControler();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return NAME;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractPluginWebServiceController getWebServiceController() {
        return wsController;
    }

    /** {@inheritDoc} */
    @Override
    public void setPluginAPI(final MetAPI api) {
        this.pluginAPI = api;
        wsController = new PluginExempleWebServiceControler(api);
    }

    //    @Override
//    public PluginAMPController getAMPController() {
//        return ampController;
//    }
}
