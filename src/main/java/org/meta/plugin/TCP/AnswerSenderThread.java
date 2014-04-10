package org.meta.plugin.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import org.meta.modele.Searchable;
import org.meta.plugin.TCP.AMP.AMPAnswerParser;
import org.meta.plugin.TCP.AMP.AMPAskFactory;
import org.meta.plugin.TCP.AMP.exception.NotAValidAMPCommand;

import com.sun.xml.internal.ws.util.ByteArrayBuffer;

public class AnswerSenderThread extends Thread {

	private InetAddress adress = null;
	private AMPAskFactory ask = null;
	private ArrayList<Searchable> results = null;
	private int port = 0;

	/**
	 * 
	 * @param answer
	 */
	public AnswerSenderThread(AMPAskFactory ask, InetAddress adress, int port) {
		this.ask = ask;
		this.adress = adress;
		this.port = port;
	}

	public void run() {
		try {
			// Open a connection to the pair
			Socket client = new Socket(adress, port);
			// write the message
			client.getOutputStream().write(ask.getMessage());
			// wait for an answer
			InputStream is = client.getInputStream();
			//Open the input stream			
			ByteArrayBuffer buffer = new ByteArrayBuffer();
			int count = 0;
			while ((count = is.read()) != -1) {
				buffer.write(count);	
			}
			//parse it into an answer
			AMPAnswerParser parser = new AMPAnswerParser(buffer.getRawData());
			this.results = parser.getDatas();
			//close everything that use memory
			buffer.flush();
			buffer.close();
			client.close();
			is.close();
		} catch (IOException | NotAValidAMPCommand e) {
			e.printStackTrace();// TODO
		}
	}
	
	
	public ArrayList<Searchable> getResults() {
		return results;
	}
}
