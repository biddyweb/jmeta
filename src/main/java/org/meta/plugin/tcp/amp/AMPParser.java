package org.meta.plugin.tcp.amp;

import java.util.LinkedHashMap;

import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;

public abstract class AMPParser {
	
	public AMPParser(byte[] bs) throws NotAValidAMPCommand{
			parse(bs);
	}
	
	/**
	 * parse an byte[] as describe in the AMP Protocol
	 * @param bs
	 */
	private void parse(byte[] bs) throws NotAValidAMPCommand{
		LinkedHashMap<String, byte[]> content = new LinkedHashMap<String, byte[]>();
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

	protected abstract void useContent(LinkedHashMap<String, byte[]> content) throws NotAValidAMPCommand;

	private int parseSize(byte a, byte b) {
		return ((int)a<<8)+((int)b);
	}
	
}
