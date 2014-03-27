package org.meta.plugin.TCP.AMP;

import java.util.HashMap;

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
	 * @param ask
	 * @param command
	 * @param hash
	 */
	public AMPAskFactory(String ask, String command, String hash) {
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		map.put("_ask", ask.getBytes());
		map.put("_command", command.getBytes());
		map.put("_hash", hash.getBytes());
		build(map);
	}

}
