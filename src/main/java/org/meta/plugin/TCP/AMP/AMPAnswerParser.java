package org.meta.plugin.TCP.AMP;

import java.text.ParseException;
import java.util.HashMap;

public class AMPAnswerParser extends AMPParser{

	//Do not initialize those variables, because it's made by the mumy
	//in her constructor ;) via the implement method "useContent"
	private String 					answer		;
	private HashMap<String, byte[]>	parameters	;
	
	public AMPAnswerParser(byte[] bs) throws ParseException {
		super(bs);
	}

	@Override
	protected void useContent(HashMap<String, byte[]> content) {
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
