package org.meta.plugin.tcp;



public abstract class Command {
	protected String		commandName =	null;
	protected String		callingIp   =	null;
	
	public Command(){}
	
	public abstract byte[] execute();
	
	
	
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
