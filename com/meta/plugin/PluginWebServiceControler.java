package com.meta.plugin;

import com.meta.modele.Model;

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
