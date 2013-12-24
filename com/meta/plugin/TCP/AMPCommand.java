package com.meta.plugin.TCP;


public abstract class AMPCommand {
	protected String							commandName =	null;
	protected SerializableCommandParameters	command		= 	null;
	protected String							callingIp   =	null;
	protected TCPWriter 						tcpWriter   = 	null;
	
	public AMPCommand(){}
	
	public abstract void execute();
	
	/**
	 * 
	 * @param command
	 */
	public void setParameters(SerializableCommandParameters command){
		this.command = command;
	}
	
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

	/**
	 * 
	 * @param writer
	 */
	public  void setTCPWriter(TCPWriter writer) {
		this.tcpWriter = writer;
	}
	
}
