/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.plugin.tcp.amp;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import org.meta.model.MetaObjectModelFactory;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Parse an AMP message.
 *
 * @author faquin
 */
public abstract class AMPParser {

    private final Logger logger = LoggerFactory.getLogger(AMPParser.class);

    /**
     *
     */
    protected MetaObjectModelFactory factory = null;

    /**
     *
     * @param bs the data
     * @param modelFactory the model factory
     * @throws InvalidAMPCommand if the data contains invalid values
     */
    public AMPParser(final byte[] bs, final MetaObjectModelFactory modelFactory) throws InvalidAMPCommand {
        this.factory = modelFactory;
        parse(bs);
    }

    /**
     *
     * @param bs the data
     * @throws InvalidAMPCommand if the data contains invalid values
     */
    public AMPParser(final byte[] bs) throws InvalidAMPCommand {
        parse(bs);
    }

    /**
     * Parses a byte array as described in the AMP Protocol. Once the hashMap containing keyMap is rebuild, it
     * call useContent which has to be overridden in the extending class
     *
     * @param bs the byte array to parse
     */
    private void parse(final byte[] bs) throws InvalidAMPCommand {
        LinkedHashMap<String, byte[]> content = new LinkedHashMap<>();
        int readIndex = 0;

        if (bs.length > 0) {
            //for each elements in the byte array
            while (bs[readIndex] != 0x00 || bs[readIndex + 1] != 0x00) {
                //recompose the size of the following bloc
                int size = parseSize(bs[readIndex], bs[readIndex + 1]);

                String name = null;
                byte[] value = null;

                //increase offset
                readIndex += 2;

                //user a stringBuilder as a buffer to rebuild the name value
                StringBuilder builder = new StringBuilder();
                for (int i = readIndex; (i - readIndex) < size; i++) {
                    builder.append((char) bs[i]);
                }
                //increase the offset by the readed size
                readIndex = readIndex + size;
                name = builder.toString();

                //size of the value
                size = parseSize(bs[readIndex], bs[readIndex + 1]);
                readIndex += 2;

                //Stack the value directly in a byte[]
                value = new byte[size];
                for (int i = readIndex; (i - readIndex) < size; i++) {
                    value[i - readIndex] = bs[i];
                }
                //increase the offset
                readIndex = readIndex + size;
                content.put(name, value);
            }
        } else {
            throw new InvalidAMPCommand("content cannot be null");
        }
        useContent(content);
    }

    /**
     * use this content to rebuild what you want.
     *
     * @param content an LinkedhashMap containing key value where key is the name of what the value represent
     *
     * @throws InvalidAMPCommand if something invalid was encountered
     */
    protected abstract void useContent(LinkedHashMap<String, byte[]> content) throws InvalidAMPCommand;

    /**
     * Rebuild a short from a byte array.
     *
     * @param bytes byte array containing two index
     * @return a short
     */
    private int parseSize(final byte... bytes) {
        return ByteBuffer.wrap(bytes).getShort();
    }

}
