package org.meta.plugin.tcp.amp;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Thomas LAVOCAT
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Build an AMP message
 * @author faquin
 */
public abstract class AMPBuilder {
    protected byte[]    message     =     null;

    /**
     * build from a LinkedhashMap containing a key value
     * where a key is well linked to a value as byte array
     * 
     * example
     * 
     * key   : _hash
     * value : 0x0000à0000000000000 (as byte array)
     * 
     * @param ask
     * @param command
     * @param map
     * @return
     */
    protected void build(LinkedHashMap<String, byte[]> map)
    {
        message = new byte[0];
        /*
         * for each entry in the map, add information in the byte array
         */
        for(String name : map.keySet())
            addPair(name, map.get(name));
        //close message
        closeMessage();
    }

    /**
     * Close the amp message with standard 0x00 0x00 ending
     */
    public void closeMessage() {
        byte[] tempMessage = Arrays.copyOf(this.message, this.message.length+2);
        tempMessage[tempMessage.length-2] = 0x00;
        tempMessage[tempMessage.length-1] = 0x00;
        this.message = tempMessage;
    }

    /**
     * Add the pair key value to the current message
     * @param key
     * @param bs
     */
    private void addPair(String key, byte[] bs) {
        int     offSet            = 0;
        //lenght of the key
        int     keyLength         = key.length();// TODO Auto-generated method stub

        //length of the message
        int     messageLength     = bs.length;

        //Size of the message all message Key length + message length + 4 bytes
        byte[]     subMessage         = new byte[4+keyLength+messageLength];

        //Write the key length on two bytes
        byte[] size = ByteBuffer.allocate(2).putShort((short)keyLength).array();
        subMessage[offSet]      =  size[0];
        subMessage[offSet+1]    =  size[1];

        //increase offset
        offSet += 2;

        //for each char of the key, add in the message
        for(int i=offSet; (i-offSet)< key.length(); i++){
            subMessage[i] = key.getBytes()[i-offSet];
        }

        //increase offset by the key length
        offSet += key.length();

        //write the message length on two bytes
        size = ByteBuffer.allocate(2).putShort((short)messageLength).array();
        subMessage[offSet]      =  size[0];
        subMessage[offSet+1]    =  size[1]; 

        offSet += 2;

        //for each char of the message, add in the message
        for(int i=offSet; (i-offSet)< bs.length; i++){
            subMessage[i] = bs[i-offSet];
        }

        //add the message length to the offset counter
        offSet += bs.length;

        //add the submessage to the global Amp message
        //original size
        int originalLength = this.message.length;
        //new empty temp message -> old message size + sub message size
        byte[] tempMessage = new byte[originalLength+subMessage.length];
        //copy the old message into the temp buffer
        for(int i=0; i<originalLength; i++)
            tempMessage[i] = this.message[i];
        //copy the sub message into the sub buffer
        for(int i=originalLength; (i-originalLength)<subMessage.length; i++)
            tempMessage[i] = subMessage[i-originalLength];
        this.message = tempMessage;
    }

    /**
     *
     * @return an AMPMessage (ask or answer)
     */
    public byte[] getMessage(){
        return message;
    }

}
