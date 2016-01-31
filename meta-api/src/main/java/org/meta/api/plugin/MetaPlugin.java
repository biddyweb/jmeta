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
package org.meta.api.plugin;

import org.meta.api.ws.AbstractPluginWebServiceController;

/**
 * Plugin interface to be implemented by plugins for META.
 *
 * @author nico
 * @version $Id: $
 */
public interface MetaPlugin {

    /**
     * <p>getName</p>
     *
     * @return The name of the plugin.
     */
    String getName();

    /**
     * Give an instance of the meta API to the plugin.
     *
     * @param api the api
     */
    void setPluginAPI(final MetAPI api);

    /**
     * Return the instance of {@link AbstractPluginWebServiceController} defined by this plugin.
     *
     * @return the web service controller.
     */
    AbstractPluginWebServiceController getWebServiceController();

    /**
     * Return the instance of {@link PluginAMPController} defined by this plugin.
     *
     * @return the AMP controller.
     */
    //PluginAMPController getAMPController();
}
