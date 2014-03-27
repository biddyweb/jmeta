package org.meta.plugin.TCP.AMP;

import java.text.ParseException;
import java.util.HashMap;

import org.meta.plugin.TCP.AMP.exception.NotAValidAMPAskCommand;
import org.meta.plugin.TCP.AMP.exception.NotAValidAMPCommand;


public class AMPAskParser extends AMPParser{
	
	//Do not initialize those variables, because it's made by the mumy
	//in her constructor ;) via the implement method "useContent"
	private String 	ask			;
	private String 	command		;
	private String	hash		;

	public AMPAskParser(byte[] bs) throws NotAValidAMPCommand{
		super(bs);
	}
	
	@Override
	protected void useContent(HashMap<String, byte[]> content) throws NotAValidAMPAskCommand {
		ask 	= content.get("_ask") != null ? new String(content.get("_ask")) : null;
		command = content.get("_command") != null ?new String(content.get("_command")) : null;
		hash 	= content.get("_hash") != null ? new String(content.get("_hash")) : null;
		
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
