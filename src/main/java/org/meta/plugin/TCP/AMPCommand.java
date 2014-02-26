package org.meta.plugin.TCP;


import java.util.HashMap;

import com.google.gson.JsonArray;

public abstract class AMPCommand {
	protected String		commandName =	null;
	protected String		callingIp   =	null;
	
	public AMPCommand(){}
	
	public abstract Byte[] execute();
	
	
	
	/**
	 * 
	 * @param jsonArray 
	 * @param command
	 */
	public abstract void setParameters(String hash);
	
	/**
	 * 
	 * @return
	 */
	public String getCommandName(){
		return commandName;
	}

	/**
	 * @param callingNode the callingNode to set
	 */
	public void setCallingNode(String ip) {
		this.callingIp = ip;
	}

}
