package org.meta.plugin.tcp;

import org.meta.plugin.tcp.amp.AMPAnswerFactory;



public abstract class AbstractCommand {
	
	protected String workOn = null;
	
	public AbstractCommand(){}
	
	public abstract AMPAnswerFactory execute(String answer, String hash);	
}
