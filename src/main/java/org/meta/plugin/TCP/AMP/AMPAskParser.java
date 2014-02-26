package org.meta.plugin.TCP.AMP;

import java.text.ParseException;
import java.util.HashMap;


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
	protected void useContent(HashMap<String, String> content) {
		ask 	= content.get("_ask");
		command = content.get("_command");
		hash 	= content.get("_hash");
		//TODO if one of them is null throw an exception
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
