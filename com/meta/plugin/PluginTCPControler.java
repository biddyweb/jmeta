package com.meta.plugin;

import java.util.HashMap;
import java.util.Iterator;

import com.meta.controler.P2P.P2PControler;
import com.meta.controler.P2P.P2PListener;
import com.meta.modele.Model;
import com.meta.plugin.TCP.AMPCommand;
import com.meta.plugin.TCP.TCPReader;

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
public abstract class PluginTCPControler implements P2PListener{

	protected 	P2PControler 						p2pControler 	= 	null;
	protected  Model 		 						model 			=	null;
	protected  HashMap<String, Class<AMPCommand>>	lstCommands		= 	null;
	private 	TCPReader							reader			= 	null;
	
	public PluginTCPControler(){
		reader = TCPReader.getInstance();
	}

	/**
	 * Fil the lstCommands with all the needed TCP commands.
	 */
	protected abstract void initialiseCommands();

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
		initialiseCommands();
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
