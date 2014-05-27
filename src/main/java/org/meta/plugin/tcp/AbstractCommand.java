package org.meta.plugin.tcp;

import org.meta.plugin.tcp.amp.AMPAnswerFactory;


//TODO transform as an interface
public abstract class AbstractCommand {
	
	public AbstractCommand(){}
	
	public abstract AMPAnswerFactory execute(String answer, String hash);	
}
