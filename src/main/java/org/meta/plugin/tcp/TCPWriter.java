package org.meta.plugin.tcp;

import java.net.InetAddress;

import org.meta.plugin.tcp.amp.AMPAskFactory;


public class TCPWriter {
	private static 		TCPWriter 	instance 	= new TCPWriter();
	private 			int			port		= 4001;//TODO
	private 			int			lastAsk 	= 0;
	
	private TCPWriter() {}
	
	public static TCPWriter getInstance() {
		return instance;
	}
	
	public void askTo(	InetAddress adress, 
						String command, 
						String hash, 
						TCPResponseCallbackInteface listenner){
		lastAsk++;
		AMPAskFactory ask = new AMPAskFactory(lastAsk+"", command, hash);
		AnswerSenderThread sender = new AnswerSenderThread(	ask, 
															adress, 
															port, 
															listenner);
		sender.start();
	}
}
