package org.meta.plugin.TCP.AMP;

import java.text.ParseException;
import java.util.HashMap;

import org.meta.plugin.TCP.AMP.exception.NotAValidAMPAskCommand;


public class AMPAskParser extends AMPParser{
	
	//Do not initialize those variables, because it's made by the mumy
	//in her constructor ;) via the implement method "useContent"
	private String 	ask			;
	private String 	command		;
	private String	hash		;

	public AMPAskParser(byte[] bs) throws ParseException{
		super(bs);
	}
	
	@Override
	protected void useContent(HashMap<String, byte[]> content) throws NotAValidAMPAskCommand {
		ask 	= new String(content.get("_ask"));
		command = new String(content.get("_command"));
		hash 	= new String(content.get("_hash"));
		
		//If one of those parameters is empty or null, throw an exception
		if(	ask 	== null || "".equals(ask) 		||
			command	== null || "".equals(command)	||
			hash	== null || "".equals(hash)
		)
			throw new NotAValidAMPAskCommand(ask, command, hash);
		
	}

	public String getAsk() {
		return ask;
	}

	public String getCommand() {
		return command;
	}

	public String getHash() {
		return hash;
	}
}
