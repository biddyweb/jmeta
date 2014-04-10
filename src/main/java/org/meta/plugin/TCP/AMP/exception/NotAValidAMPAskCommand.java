package org.meta.plugin.TCP.AMP.exception;

public class NotAValidAMPAskCommand extends NotAValidAMPCommand {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotAValidAMPAskCommand(String ask, String command, String hash){
		super(	"The received message is not valid because"+
				"one of those parameters are empty or null : "+
				"ask : "+ask+" ; command : "+command+" ; hash : "+hash);
		
	}
	
}