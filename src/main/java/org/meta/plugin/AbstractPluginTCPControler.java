/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 JMeta
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.plugin;

import java.util.HashMap;

import org.meta.common.MetHash;
import org.meta.model.Model;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.SingletonTCPReader;

/**
 *
 * @author Thomas LAVOCAT
 *
 */
public abstract class AbstractPluginTCPControler {

    protected Model model = null;
    private SingletonTCPReader reader = null;
    protected HashMap<String, Class<? extends AbstractCommand>> lstCommands = null;
    private String pluginName = null;

    public AbstractPluginTCPControler() {
        reader = SingletonTCPReader.getInstance();
        lstCommands = new HashMap<String, Class<? extends AbstractCommand>>();
    }

    /**
     * Fill the lstCommands with all the needed TCP commands.
     *
     * @param lstCommands2
     */
    protected abstract void registercommands(HashMap<String, Class<? extends AbstractCommand>> commands);

    /**
     * initialize the plugin
     *
     * @param pluginName
     */
    public void init(String pluginName) {
        this.pluginName = pluginName;
        registercommands(lstCommands);
    }

    /**
     * Give the model
     *
     * @param model
     */
    public void setModel(Model model) {
        this.model = model;
    }

    public AbstractCommand getCommand(String commandName) {
        AbstractCommand command = null;

        Class<? extends AbstractCommand> clazz = lstCommands.get(commandName);
        if (clazz != null) {
            try {
                command = (AbstractCommand) clazz.newInstance();
                command.setPluginTCPControler(this);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return command;
    }

    public Searchable getInTheModel(MetHash hash) {
        return model.getSearchable(hash);
    }
}
