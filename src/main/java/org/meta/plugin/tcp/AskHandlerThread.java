package org.meta.plugin.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.meta.plugin.tcp.amp.AMPAskParser;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

/**
 * This thread only listen to AMP Command
 * @author faquin
 *
 */
public class AskHandlerThread extends Thread{
	
	private Socket 		client	= null;
	private TCPReader 	reader = TCPReader.getInstance();

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
			ByteArrayBuffer buffer = new ByteArrayBuffer();
			int count = 0;
			
			while ((count = inputStream.read()) != -1) {
				buffer.write(count);	
			}
			//The question as to be a AMP command, if not -> exception
			AMPAskParser parser = new AMPAskParser(buffer.getRawData());
			buffer.flush();
			buffer.close();
			
			//Get the _command parameter from the amp command
			//If not null, it means we speak the same langage, if not
			//do nothing
			if(parser.getCommand() != null){//TODO handle this with an exception
				//get the AMPCommand from the TCPReader singleton
				//who know every plugins
				Class<? extends AbstractCommand> classCommand = 
										this.reader.getCommand(parser.getCommand());
				//if the classCommand is null, we d'ont have the requeried
				//command to execute
				if(classCommand != null){
					//Make a new instance, is an exception is thrown here
					//it means that maybe here's an error in the plugin
					//config file;
					AbstractCommand command = (AbstractCommand) classCommand.newInstance();
					//Set the parameters as String[]
					command.setParameters(parser.getHash());
					//and execute it
					byte[] response = command.execute();
					//finally, write the output to the client
					OutputStream os = client.getOutputStream();
					for (int i = 0; i < response.length; i++) {
						os.write((int)response[i]); 
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();//TODO gérer cette exception
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//TODO if an exception is throw here it means you cannot excecute 
			//the command
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotAValidAMPCommand e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
}
