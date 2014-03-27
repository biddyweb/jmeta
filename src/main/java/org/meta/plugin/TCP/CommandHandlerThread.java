package org.meta.plugin.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;

import org.meta.plugin.TCP.AMP.AMPAskParser;
import org.meta.plugin.TCP.AMP.exception.NotAValidAMPCommand;

/**
 * This thread only listen to AMP Command
 * @author faquin
 *
 */
public class CommandHandlerThread extends Thread{
	
	private Socket 		client	= null;
	private TCPReader 	reader = TCPReader.getInstance();

	public CommandHandlerThread(Socket client){
		this.client = client;
	}
	
	@Override
	public void run() {
		InputStream inputStream = null;
		try {
			//Use a Stringbuilder to rebuilt the stream
			StringBuilder input = new StringBuilder();
			//Open the client inputStream
			inputStream = client.getInputStream();
			//Read the stream
			int octet = inputStream.read();
			while(octet != -1){
				input.append((char) octet);
				octet = inputStream.read();
			}
			//The question as to be a AMP command, if not -> exception
			AMPAskParser parser = new AMPAskParser(input.toString().getBytes());
			//Get the _command parameter from the amp command
			//If not null, it means we speak the same langage, if not
			//do nothing
			if(parser.getCommand() != null){//TODO handle this with an exception
				//get the AMPCommand from the TCPReader singleton
				//who know every plugins
				Class<AMPCommand> classCommand = 
										this.reader.getCommand(parser.getCommand());
				//if the classCommand is null, we d'ont have the requeried
				//command to execute
				if(classCommand != null){
					//Make a new instance, is an exception is thrown here
					//it means that maybe here's an error in the plugin
					//config file;
					AMPCommand command = (AMPCommand) classCommand.newInstance();
					//Set the parameters as String[]
					command.setParameters(parser.getHash());
					//and execute it
					Byte[] response = command.execute();
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
