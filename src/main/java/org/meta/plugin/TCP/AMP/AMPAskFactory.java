package org.meta.plugin.TCP.AMP;

import java.util.LinkedHashMap;

/**
 * 
 * @author faquin
 *
 */
public class AMPAskFactory extends AMPFactory{
	
	/**
	 * 
	 * Create a new AMP Ask command
	 * 
	 * @param ask the number for the question
	 * @param command the command to execute to the other pair
	 * @param hash the hash of the request
	 */
	public AMPAskFactory(String ask, String command, String hash) {
		LinkedHashMap<String, byte[]> map = new LinkedHashMap<String, byte[]>();
		map.put("_ask", ask.getBytes());
		map.put("_command", command.getBytes());
		map.put("_hash", hash.getBytes());
		//build the message
		super.build(map);
	}

}
