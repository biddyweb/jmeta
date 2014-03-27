package org.meta.plugin.TCP.AMP;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.meta.plugin.TCP.AMP.exception.NotAValidAMPCommand;

public class AMPAnswerParser extends AMPParser{

	//Do not initialize those variables, because it's made by the mumy
	//in her constructor ;) via the implement method "useContent"
	private String 					answer		;
	private HashMap<String, byte[]>	parameters	;
	
	public AMPAnswerParser(byte[] bs) throws NotAValidAMPCommand {
		super(bs);
	}

	@Override
	protected void useContent(LinkedHashMap<String, byte[]> content) {
		answer = new String(content.get("_answer"));
		content.remove("_answer");
		parameters = content;
	}

	public String getAnswer() {
		return answer;
	}

	public HashMap<String, byte[]> getParameters() {
		return parameters;
	}
}
