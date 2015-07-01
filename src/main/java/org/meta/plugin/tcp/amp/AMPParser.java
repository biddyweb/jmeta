package org.meta.plugin.tcp.amp;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import org.meta.model.Model;
import org.meta.model.ModelFactory;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;

/**
 * 
 * Parse an AMP message 
 * @author faquin
 */
public abstract class AMPParser {
    protected ModelFactory factory = null;
    public AMPParser(byte[] bs) throws NotAValidAMPCommand{
        try {
            factory = Model.getInstance().getFactory();
        } catch (ModelException e) {
            e.printStackTrace();
        }
        parse(bs);
    }

    /**
     * parse an byte[] as describe in the AMP Protocol
     * Once the hashMap containing keyMap is rebuild, it call useContent
     * wich has to be override in an extending class
     * 
     * @param bs the byte array to parse
     */
    private void parse(byte[] bs) throws NotAValidAMPCommand{
        LinkedHashMap<String, byte[]> content = new LinkedHashMap<String, byte[]>();
        int readIndex = 0;

        if(bs.length > 0){
            //for each elements in the byte array
            while(bs[readIndex] != 0x00 || bs[readIndex+1] != 0x00){
                //recompose the size of the following bloc
                int     size     = parseSize(bs[readIndex], bs[readIndex+1]);

                String     name    = null;
                byte[]     value    = null;

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
        }else{
            throw new NotAValidAMPCommand("content cannot be null");
        }
        useContent(content);
    }

    /**
     * use this content to rebuild what you want
     * @param content an LinkedhashMap containing key value
     * where key si the name of what the value represent
     * 
     * @throws NotAValidAMPCommand
     */
    protected abstract void useContent(LinkedHashMap<String, byte[]> content) throws NotAValidAMPCommand;

    /**
     * rebuild a short from a byte array
     * @param bytes byte array containing two index
     * @return a short
     */
    private int parseSize(byte ... bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

}
