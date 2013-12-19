package com.meta.plugin.TCP;

import il.technion.ewolf.kbr.Node;

public abstract class AMPCommand {
	protected String							commandName =	null;
	protected SerializableCommandParameters	command		= 	null;
	protected Node								callingNode	=	null;
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
	public void setCallingNode(Node callingNode) {
		this.callingNode = callingNode;
	}

	/**
	 * 
	 * @param writer
	 */
	public  void setTCPWriter(TCPWriter writer) {
		this.tcpWriter = writer;
	}
	
}
