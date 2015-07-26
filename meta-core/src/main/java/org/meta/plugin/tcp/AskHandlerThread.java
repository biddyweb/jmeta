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
package org.meta.plugin.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.common.MetHash;
import org.meta.plugin.tcp.amp.AMPAskParser;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread only listen to AMP Command.
 *
 * @author faquin
 *
 */
public class AskHandlerThread implements Runnable {

    private Socket client = null;
    private AMPServer reader = null;
    private final Logger logger = LoggerFactory.getLogger(AskHandlerThread.class);

    /**
     * Initiate the handler with given parameters.
     *
     * @param pServer the AMP server that created us.
     * @param pClient socket connection to dicuss with.
     */
    public AskHandlerThread(final AMPServer pServer, final Socket pClient) {
        this.reader = pServer;
        this.client = pClient;
    }

    /**
     *
     */
    @Override
    public final void run() {
        InputStream inputStream = null;
        try {
            //Open the client inputStream
            inputStream = client.getInputStream();
            //Read the stream
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int count = 0;

            while ((count = inputStream.read()) != -1) {
                buffer.write(count);
            }
            //The question as to be a AMP command, if not -> exception
            AMPAskParser parser = new AMPAskParser(buffer.toByteArray());
            buffer.flush();
            buffer.close();

            //Get the _command parameter from the amp command
            //If not null, it means we speak the same langage, if not
            //do nothing
            if (parser.getCommand() != null
                    && parser.getPlugin() != null) {
                PluginAMPCommand command = this.reader.getCommand(
                        parser.getPlugin(),
                        parser.getCommand());
                if (command != null) {
                    //execute it
                    MetHash hash = parser.getHash();
                    String answer = parser.getAsk();
                    byte[] response = command.execute(answer, hash).getMessage();
                    //finally, write the output to the client
                    OutputStream os = client.getOutputStream();
                    for (int i = 0; i < response.length; i++) {
                        os.write((int) response[i]);
                    }
                }
            }
        } catch (IOException | InvalidAMPCommand e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
