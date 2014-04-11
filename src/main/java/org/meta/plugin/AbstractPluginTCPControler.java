package org.meta.plugin;

import java.util.HashMap;
import java.util.Iterator;

import org.meta.controler.P2P.P2PControler;
import org.meta.controler.P2P.P2PListener;
import org.meta.modele.Model;
import org.meta.plugin.tcp.Command;
import org.meta.plugin.tcp.TCPReader;

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
public abstract class AbstractPluginTCPControler implements P2PListener{

	protected 	P2PControler p2pControler 	= 	null;
	protected  	Model 		 model 			=	null;
	private 	TCPReader	reader			= 	null;
	protected  	HashMap<String, Class<? extends Command>>	lstCommands		= 	null;
	
	public AbstractPluginTCPControler(){
		reader = TCPReader.getInstance();
		lstCommands = new HashMap<String, Class<? extends Command>>();
	}

	/**
	 * Fil the lstCommands with all the needed TCP commands.
	 * @param lstCommands2 
	 */
	protected abstract void registercommands(HashMap<String, Class<? extends Command>> commands);

	/**
	 * @param p2pControler the p2pControler to set
	 */
	public void setP2pControler(P2PControler p2pControler) {
		this.p2pControler = p2pControler;
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
	 * Give the model
	 * @param model
	 */
	public void setModel(Model model) {
		this.model = model;
	}
	
	
}
