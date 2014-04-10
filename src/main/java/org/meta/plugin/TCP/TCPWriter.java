package org.meta.plugin.TCP;

import java.net.InetAddress;

import org.meta.plugin.TCP.AMP.AMPAskFactory;


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
						TCPResponseCallback listenner){
		lastAsk++;
		AMPAskFactory ask = new AMPAskFactory(lastAsk+"", command, hash);
		AnswerSenderThread sender = new AnswerSenderThread(	ask, 
															adress, 
															port, 
															listenner);
		sender.start();
	}
}
