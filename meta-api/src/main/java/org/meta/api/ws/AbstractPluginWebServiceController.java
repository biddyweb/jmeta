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

import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bson.types.BasicBSONList;
import org.meta.api.plugin.MetAPI;

/**
 * This class is to be extended to create the WS part of a plugin.
 *
 * It allows you to register commands to the web service, that can be executed by the user on his interface.
 *
 * Basically, this class offer generic treatment to serve interfaces and handle specific plugin operations.
 *
 * You may extends registerCommands which allows you to tell the {@link WebServiceReader} that you may have
 * something to execute.
 *
 * @author Thomas LAVOCAT
 * @version $Id: $
 */
public abstract class AbstractPluginWebServiceController {

    /**
     * The Meta api object.
     */
    protected final MetAPI api;

    /**
     *
     */
    protected LinkedHashMap<String, Class<? extends AbstractWebService>> lstCommands = null;

    /**
     * Just a TEST!.
     */
    protected Map<String, Object> context;

    /**
     *
     */
    protected String pluginName = null;

    /**
     * <p>Constructor for AbstractPluginWebServiceController.</p>
     *
     * @param pluginAPI the meta plugin api
     */
    public AbstractPluginWebServiceController(final MetAPI pluginAPI) {
        this.api = pluginAPI;
        lstCommands = new LinkedHashMap<>();
        this.context = new HashMap<>();
    }

    /**
     * Initialize the plugin.
     *
     * @param name the plugin name to initialize.
     */
    public final void init(final String name) {
        this.pluginName = name;
        registercommands(lstCommands);
    }

    /**
     * Fill the lstCommands with all the needed web service commands.
     *
     * @param commands is a HashMap containing a key which is the command name and a Class which is the
     * handler of the command.
     */
    protected abstract void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands);

    /**
     * <p>getCommand</p>
     *
     * @param command name of the command
     * @return the className of the command pointed by the given param
     *
     * TODO return an instance here instead of a Class!
     */
    public final Class<? extends AbstractWebService> getCommand(final String command) {
        return lstCommands.get(command);
    }

    /**
     * Serialize as JSON the list of commands.
     *
     * @return a list of commands as JSON
     */
    public final String getJsonCommandList() {
        BasicBSONList list = new BasicBSONList();
        for (Iterator<String> i = lstCommands.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            list.add(key);
        }

        // Serialize BasicBSONList in JSON
        ObjectSerializer jsonSerializer = JSONSerializers.getStrict();
        return jsonSerializer.serialize(list);
    }

    /**
     * <p>getAPI</p>
     *
     * @return the plugin api
     */
    public MetAPI getAPI() {
        return this.api;
    }

    /**
     * <p>Getter for the field <code>context</code>.</p>
     *
     * @return The WS controller context. FOR TESTS!.
     */
    public Map<String, Object> getContext() {
        return context;
    }
}
