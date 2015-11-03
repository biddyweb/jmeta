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
package org.meta.api.ws;

import java.util.Map;
import org.meta.api.ws.forms.InterfaceDescriptor;
import org.meta.api.ws.forms.organizers.ColumnOrganizer;

/**
 * Defines a web service command.
 *
 * To register a new web service command you must extends this class, and override at least :
 * <ul>
 * <li> execute command</li>
 * <li>apply small update</li>
 * </ul>
 *
 * You should build your interface in the default constructor, which is required
 *
 * @author faquin
 */
public abstract class AbstractWebService {

    /**
     *
     */
    protected ColumnOrganizer rootColumn = null;

    /**
     *
     */
    protected InterfaceDescriptor descriptor = null;

    /**
     *
     */
    protected AbstractPluginWebServiceController controller = null;

    /**
     * You need to build you user interface in here. For that, you shall need the root element, named
     * rootColumn.
     *
     * Fill it with anything you want to build your interface
     *
     * @param wsController the parent web service controller
     */
    public AbstractWebService(final AbstractPluginWebServiceController wsController) {
        this.controller = wsController;
        rootColumn = new ColumnOrganizer("root");
        descriptor = new InterfaceDescriptor(rootColumn);
    }

    /**
     *
     * @param wsController give WebServiceControler
     */
    public final void setWebServiceControler(final AbstractPluginWebServiceController wsController) {
        this.controller = wsController;
    }

    /**
     *
     * @return an interface who will be translate as JSON.
     *
     * Client side will surely build a human interface to allow final user to interact with this webservice
     * commands.
     *
     * Note that you can totally bypass the interface, but you loose interest of webservice commands.
     */
    public final InterfaceDescriptor getInterface() {
        return descriptor;
    }

    /**
     * Execute the command with the given parameters.
     *
     * @param map parameter map from jetty
     * @return the interface descriptor.
     *
     */
    public final InterfaceDescriptor execute(final Map<String, String[]> map) {
        executeCommand(map);
        return descriptor;
    }

    /**
     * When a final user interacts with a webservice command, he will execute it.
     *
     * Executing a command means giving parameters to do a specific action.
     *
     * Parameters are a map of key/values.
     *
     * @param map a simple map, where for each key, you may find or not an array of values. Those are given by
     * Jetty, which is the http server. So, parameters are given by the end user as a get string :
     * ?key1=bar;key2=foo;key2=barfoo
     *
     * you can easily lookup parameters using the following tomcat style methods : - getParameter -
     * getParameters
     *
     * If you want any output, make sure you apply small changes to your interface. Beware that big changes
     * are not tested yet, but you want to give a try, your feedback will be warm welcome.
     *
     * Remember that your operation is blocking the user interface, so, faster you send him a result, better
     * it is.
     *
     */
    protected abstract void executeCommand(final Map<String, String[]> map);

    /**
     * After execution, some client will fetch the interface every X ms.
     *
     * @return the modified interfaceDescripor
     */
    public final InterfaceDescriptor retrieveUpdate() {
        applySmallUpdate();
        return descriptor;
    }

    /**
     *
     * After calling execution, most clients (especially ours) will fetch the interface every X ms, to see if
     * there any changes or any new results.
     *
     * if you want to make small changes in the interface, it's possible here.
     *
     * By small changes, we mean, - make an other DHT search, - take newly arrived results in the callback
     * method and add them into the output text object. - ...
     *
     * Remember that, for now (LSP version) this method will be called every 500ms.
     *
     */
    protected abstract void applySmallUpdate();

    /**
     * TODO ?
     */
    public final void kill() {
        controller = null;
    }

    /**
     *
     * @param name name of the parameter
     * @param map map from jetty
     * @return an array of values for the key or null if not found
     */
    public final String[] getParameters(final String name, final Map<String, String[]> map) {
        return map.get(name);
    }

    /**
     *
     * @param name name of the parameter array
     * @param map map from jetty
     * @return the value for the key or null if not found
     */
    public final String getParameter(final String name, final Map<String, String[]> map) {
        String parameter = null;
        String[] parameters = getParameters(name, map);
        if (parameters != null && parameters.length > 0) {
            parameter = parameters[0];
        }
        return parameter;
    }

}
