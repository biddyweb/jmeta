package org.meta.plugin.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import org.meta.modele.Searchable;
import org.meta.plugin.tcp.amp.AMPAnswerParser;
import org.meta.plugin.tcp.amp.AMPAskFactory;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;


public class AnswerSenderThread extends Thread {

	private InetAddress 			adress 		= null;
	private AMPAskFactory 			ask 		= null;
	private ArrayList<Searchable> 	results 	= null;
	private int 					port 		= 0;
	private TCPResponseCallbackInteface 	listenner 	= null;
	/**
	 * 
	 * @param listenner 
	 * @param answer
	 */
	public AnswerSenderThread(	AMPAskFactory ask,
								InetAddress adress, 
								int port,
								TCPResponseCallbackInteface listenner)
	{
		this.ask 		= ask;
		this.adress 	= adress;
		this.port 		= port;
		this.listenner 	= listenner;
	}

	public void run() {
		try {
			// Open a connection to the pair
			Socket client = new Socket(adress, port);
			// write the message
			OutputStream os = client.getOutputStream();
			os.write(ask.getMessage());
			client.shutdownOutput();

			// wait for an answer
			InputStream is = client.getInputStream();
			//Open the input stream			
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int count = 0;
			while ((count = is.read()) != -1) {
				buffer.write(count);	
			}
			//parse it into an answer
			AMPAnswerParser parser = new AMPAnswerParser(buffer.toByteArray());
			this.results = parser.getDatas();
			//close everything that use memory
			buffer.flush();
			buffer.close();
			client.close();
			is.close();
		} catch (IOException | NotAValidAMPCommand e) {
			e.printStackTrace();// TODO
		}
		
		listenner.callback(results);
	}
	
	public ArrayList<Searchable> getResults() {
		return results;
	}
}
