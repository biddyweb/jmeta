package com.meta.plugin;

import com.meta.controler.P2P.P2PControler;
import com.meta.modele.Model;

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
