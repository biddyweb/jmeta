package org.meta.plugin.TCP.AMP;

import java.text.ParseException;
import java.util.HashMap;

public abstract class AMPParser {
	public AMPParser(byte[] bs) throws ParseException{
		try {
			parse(bs);
		} catch (Exception e) {
			throw new ParseException(bs + "is not a valide AMP command", -1);
		}
	}
	
	/**
	 * parse an byte[] as describe in the AMP Protocol
	 * @param bs
	 */
	private void parse(byte[] bs) throws Exception{
		HashMap<String, String> content = new HashMap<String, String>();
		int readIndex = 0;
		
		while(bs[readIndex] != 0x00 || bs[readIndex+1] != 0x00){
			int 	size 	= parseSize(bs[readIndex], bs[readIndex+1]);
			String 	name	= null;
			String 	value	= null;
			
			readIndex += 2;
			
			StringBuilder builder = new StringBuilder();
			for(int i=readIndex; (i-readIndex)<size; i++){
				builder.append((char)bs[i]);  
			}
			readIndex = readIndex + size;	
			name = builder.toString();
	
			size = parseSize(bs[readIndex], bs[readIndex+1]);	
			readIndex += 2;
			
			builder = new StringBuilder();	
			for(int i=readIndex; (i-readIndex)<size; i++){
				builder.append((char)bs[i]);
			}
			readIndex = readIndex + size;	
			value = builder.toString();
			
			content.put(name, value);
		}
		useContent(content);
	}

	protected abstract void useContent(HashMap<String, String> content);

	private int parseSize(byte a, byte b) {
		return ((int)a<<8)+((int)b);
	}
	
}
