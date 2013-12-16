package com.meta.plugin;

import com.meta.controler.P2P.P2PControler;
import com.meta.modele.Model;

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
public abstract class PluginTCPControler {

	protected 	P2PControler p2pControler 	= 	null;
	protected 	Model 		 model 			=	null;
	
	public PluginTCPControler(){}

	/**
	 * @param p2pControler the p2pControler to set
	 */
	public void setP2pControler(P2PControler p2pControler) {
		this.p2pControler = p2pControler;
	}

	public void init() {
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
	
}
