package org.meta.plugin.TCP.AMP;

import java.util.Arrays;
import java.util.HashMap;

public abstract class AMPFactory {
	protected byte[]	message     = 	null;
	
	/**
	 * 
	 * @param ask
	 * @param command
	 * @param parameters
	 * @return
	 */
	protected void build(HashMap<String, String> parameters) 
	{
		for(String name : parameters.keySet())
			addPair(name, parameters.get(name));
		
		closeMessage();
	}

	private void closeMessage() {
		byte[] tempMessage = Arrays.copyOf(this.message, this.message.length+2);
		tempMessage[tempMessage.length-2] = 0x00;
		tempMessage[tempMessage.length-1] = 0x00;
		this.message = tempMessage;
	}

	/**
	 * Add the pair key value to the current message
	 * @param key
	 * @param message
	 */
	private void addPair(String key, String message) {
		int 	offSet			= 0;
		int 	keyLength 		= key.length();
		int 	messageLength 	= message.length();
		byte[] 	subMessage 		= new byte[4+keyLength+messageLength];
		
		subMessage[offSet] =  (byte) (keyLength >> 8);
		subMessage[offSet+1] =  (byte) keyLength;
		
		offSet += 2;
		
		for(int i=offSet; (i-offSet)< key.length(); i++){
			subMessage[i] = key.getBytes()[i-offSet];
		}
		
		offSet += key.length();
		
		
		subMessage[offSet] 	 =  (byte) (messageLength >> 8);
		subMessage[offSet+1] =  (byte) messageLength;
		
		offSet += 2;
		
		for(int i=offSet; (i-offSet)< message.length(); i++){
			subMessage[i] = message.getBytes()[i-offSet];
		}
		
		offSet += message.length();
		
		int originalLength = this.message.length;
		byte[] tempMessage = new byte[originalLength+subMessage.length];
		for(int i=0; i<originalLength; i++)
			tempMessage[i] = this.message[i];
		for(int i=originalLength; (i-originalLength)<subMessage.length; i++)
			tempMessage[i] = subMessage[i-originalLength];
		this.message = tempMessage;
	}
	
	public byte[] getMessage(){
		return message;
	}
	
}
