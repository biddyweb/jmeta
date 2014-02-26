package org.meta.plugin.TCP.AMP;

import java.text.ParseException;
import java.util.HashMap;

public class AMPAnswerParser extends AMPParser{

	//Do not initialize those variables, because it's made by the mumy
	//in her constructor ;) via the implement method "useContent"
	private String 					answer		;
	private HashMap<String, String>	parameters	;
	
	public AMPAnswerParser(byte[] bs) throws ParseException {
		super(bs);
	}

	@Override
	protected void useContent(HashMap<String, String> content) {
		answer = content.get("_answer");
		content.remove("_answer");
		parameters = content;
	}

	public String getAnswer() {
		return answer;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	
}
