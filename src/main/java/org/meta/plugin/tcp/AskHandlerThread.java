package org.meta.plugin.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.meta.common.MetHash;
import org.meta.plugin.tcp.amp.AMPAskParser;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This thread only listen to AMP Command
 * @author faquin
 *
 */
public class AskHandlerThread implements Runnable{

    private Socket client = null;
    private SingletonTCPReader reader = SingletonTCPReader.getInstance();
    private Logger logger = LoggerFactory.getLogger(AskHandlerThread.class);

    /**
     * Initiate the handler with given parameters
     * @param client socket connection to dicuss with
     */
    public AskHandlerThread(Socket client){
        this.client = client;
    }

    @Override
    public void run() {
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
            if(parser.getCommand() != null &&
                  parser.getPlugin() != null
            ) {
                AbstractCommand command = this.reader.getCommand(
                        parser.getPlugin(),
                        parser.getCommand());
                if(command != null){
                    //execute it
                    MetHash hash = parser.getHash();
                    String  answer = parser.getAsk();
                    byte[] response = command.execute(answer, hash).getMessage();
                    //finally, write the output to the client
                    OutputStream os = client.getOutputStream();
                    for (int i = 0; i < response.length; i++) {
                        os.write((int)response[i]);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (NotAValidAMPCommand e) {
            logger.error(e.getMessage(), e);
        } finally {
            if(inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }
    }
}
