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
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("_ask", "23");
		map.put("_command", "toto");
		map.put("_hash", "cacahuete");
		build(map);
	}

}
