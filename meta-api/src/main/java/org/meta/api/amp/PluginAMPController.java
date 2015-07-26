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
package org.meta.api.amp;

import java.util.HashMap;
import org.meta.api.common.MetHash;
import org.meta.api.model.Model;
import org.meta.api.model.Searchable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * You may extends {@link PluginAMPController} to create the TCP part of a plugin. It's allow you to register
 * command to the tcp reader, that can be executed by other peers
 *
 * Basically, this class offer generic treatment to serve Data over P2P
 *
 * You may extends registerCommands wich allow you to tel the {@link AMPServer} that you may have something to
 * execute.
 *
 * You may use getInTheModel to retrieve information from the DB
 *
 * @author Thomas LAVOCAT
 *
 */
public abstract class PluginAMPController {

    /**
     *
     */
    protected Model model = null;

    /**
     *
     */
    protected HashMap<String, Class<? extends PluginAMPCommand>> lstCommands = null;

    /**
     *
     */
    protected String pluginName = null;
    private final Logger logger = LoggerFactory.getLogger(PluginAMPController.class);

    /**
     * toto.
     */
    public PluginAMPController() {
        lstCommands = new HashMap<>();
    }

    /**
     * Fill the lstCommands with all the needed TCP commands.
     *
     * @param commands is a HashMap containing a key wich is the command name and a Clas wich is the Class of
     * the command.
     */
    protected abstract void registercommands(HashMap<String, Class<? extends PluginAMPCommand>> commands);

    /**
     * Initialize the plugin.
     *
     * @param plugin the name of the plugin.
     */
    public final void init(final String plugin) {
        this.pluginName = plugin;
        registercommands(lstCommands);
    }

    /**
     * Give an instance of the model.
     *
     * @param metaModel the model instance
     */
    public final void setModel(final Model metaModel) {
        this.model = metaModel;
    }

    /**
     * Instantiate (if found) a new Command.
     *
     * @param commandName name of the command
     *
     * @return A command to execute
     */
    public final PluginAMPCommand getCommand(final String commandName) {
        PluginAMPCommand command = null;

        Class<? extends PluginAMPCommand> clazz = lstCommands.get(commandName);
        if (clazz != null) {
            try {
                command = (PluginAMPCommand) clazz.newInstance();
                command.setPluginTCPControler(this);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return command;
    }

    /**
     * Search the given hash in the DB.
     *
     * @param hash hash to search
     * @return the searchable if found, nothing otherwise
     */
    public final Searchable getInTheModel(final MetHash hash) {
        return model.getSearchable(hash);
    }
}
