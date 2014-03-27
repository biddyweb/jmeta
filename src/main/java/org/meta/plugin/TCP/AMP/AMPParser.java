package org.meta.plugin.TCP.AMP;

import java.text.ParseException;
import java.util.HashMap;

import org.meta.plugin.TCP.AMP.exception.NotAValidAMPCommand;

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
		HashMap<String, byte[]> content = new HashMap<String, byte[]>();
		int readIndex = 0;
		
		//for each elements in the byte array
		while(bs[readIndex] != 0x00 || bs[readIndex+1] != 0x00){
			//recompose the size of the following bloc
			int 	size 	= parseSize(bs[readIndex], bs[readIndex+1]);
			
			String 	name	= null;
			byte[] 	value	= null;
			
			//increase offset
			readIndex += 2;
			
			//user a stringBuilder as a buffer to rebuild the name value
			StringBuilder builder = new StringBuilder();
			for(int i=readIndex; (i-readIndex)<size; i++){
				builder.append((char)bs[i]);  
			}
			//increase the offset by the readed size
			readIndex = readIndex + size;	
			name = builder.toString();
			
			//size of the value
			size = parseSize(bs[readIndex], bs[readIndex+1]);	
			readIndex += 2;
			
			//Stack the value directly in a byte[]
			value = new byte[size];
			for(int i=readIndex; (i-readIndex)<size; i++){
				value[i-readIndex] = bs[i];
			}
			//increase the offset
			readIndex = readIndex + size;	
			content.put(name, value);
		}
		useContent(content);
	}

	protected abstract void useContent(HashMap<String, byte[]> content) throws NotAValidAMPCommand;

	private int parseSize(byte a, byte b) {
		return ((int)a<<8)+((int)b);
	}
	
}
