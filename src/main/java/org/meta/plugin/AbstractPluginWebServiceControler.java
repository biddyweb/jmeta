package org.meta.plugin;

import java.util.HashMap;
import java.util.Iterator;

import org.meta.model.Model;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.SingletonWebServiceReader;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 * @author Thomas LAVOCAT
 *
 */
public abstract class AbstractPluginWebServiceControler {
	protected 	Model 							model 		= null;
	private		SingletonWebServiceReader 		reader 		= null;
	protected  	HashMap<String, Class<? extends AbstractWebService>>	lstCommands		= 	null;
	protected 	AbstractPluginTCPControler 		tcpControler= null;
	
	public AbstractPluginWebServiceControler(){
		reader		= SingletonWebServiceReader.getInstance();
		lstCommands = new HashMap<String, Class<? extends AbstractWebService>>();
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * initialize the plugin
	 */
	public void init() {
		registercommands(lstCommands);
		registerCommandsToTCPReader();
	}

	/**
	 * register the commands to TCPReader
	 */
	private void registerCommandsToTCPReader() {
		for (Iterator<String> i = lstCommands.keySet().iterator(); i.hasNext();) {
			String commandName = i.next();
			reader.registerCommand(commandName, lstCommands.get(commandName));
		}
	}
	
	/**
	 * Fil the lstCommands with all the needed TCP commands.
	 * @param lstCommands2 
	 */
	protected abstract void registercommands(HashMap<String, Class<? extends AbstractWebService>> commands);

	public void setTcpControler(AbstractPluginTCPControler tcpControler) {
		this.tcpControler = tcpControler;
		
	}
	
}
