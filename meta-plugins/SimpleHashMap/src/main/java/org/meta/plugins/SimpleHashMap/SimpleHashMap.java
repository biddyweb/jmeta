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
package org.meta.plugins.SimpleHashMap;

import org.meta.api.amp.PluginAMPController;
import org.meta.api.plugin.MetaPlugin;
import org.meta.api.plugin.MetAPI;
import org.meta.api.ws.AbstractPluginWebServiceController;

/**
 * <p>SimpleHashMap class.</p>
 *
 * @author nico
 * @version $Id: $Id
 */
public class SimpleHashMap implements MetaPlugin {

    private static final String NAME = "SimpleHashMap";

    private MetAPI pluginAPI;

    private PluginAMPController ampController;
    private AbstractPluginWebServiceController wsController;

    /**
     * <p>Constructor for SimpleHashMap.</p>
     */
    public SimpleHashMap() {
        //ampController = new SimpleHashMapTcpControler();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return NAME;
    }

    /** {@inheritDoc} */
    @Override
    public void setPluginAPI(final MetAPI api) {
        this.pluginAPI = api;
        this.wsController = new SimpleHashMapWebServiceControler(pluginAPI);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractPluginWebServiceController getWebServiceController() {
        return wsController;
    }

//    @Override
//    public PluginAMPController getAMPController() {
//        return ampController;
//    }
}
