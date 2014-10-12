package org.meta.plugin.tcp;

import java.net.InetAddress;

import org.meta.plugin.tcp.amp.AMPAskFactory;


public class SingletonTCPWriter {
	private static 		SingletonTCPWriter 	instance 	= new SingletonTCPWriter();
	private 			int			port		= 4001;//TODO
	private 			int			lastAsk 	= 0;
	
	private SingletonTCPWriter() {}
	
	public static SingletonTCPWriter getInstance() {
		return instance;
	}
	
	public void askTo(	InetAddress adress, 
						String plugin,
						String command, 
						String hash, 
						TCPResponseCallbackInteface listenner){
		lastAsk++;
		AMPAskFactory ask = new AMPAskFactory(lastAsk+"", plugin, command, hash);
		AnswerSenderThread sender = new AnswerSenderThread(	ask, 
															adress, 
															port, 
															listenner);
		sender.start();
	}
}
