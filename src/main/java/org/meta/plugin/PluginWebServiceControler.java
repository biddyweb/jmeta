package org.meta.plugin;

import org.meta.modele.Model;

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
public abstract class PluginWebServiceControler {
	protected Model 				model 		= null;
	protected PluginTCPControler tcpControler 	= null;
	
	public PluginWebServiceControler(){}

	/**
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * @param tcpControler the tcpControler to set
	 */
	public void setTcpControler(PluginTCPControler tcpControler) {
		this.tcpControler = tcpControler;
	}

	public void init() {
		// TODO Auto-generated method stub
	}
	
	
}