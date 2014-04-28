package org.meta.plugin.webservice;

import java.util.HashMap;

import org.eclipse.jetty.server.Server;
import org.meta.plugin.tcp.AbstractCommand;

public class SingletonWebServiceReader extends Thread {

	private HashMap<String, Class<? extends AbstractWebService>> mapCommand = null;
	private Server 	server 	= null;
	private static 	SingletonWebServiceReader instance 	= null;
	
	private SingletonWebServiceReader() {
		mapCommand = new HashMap<String, Class<? extends AbstractWebService>>();
		server = new Server(8080);
		server.setHandler(new WebRequestHandler());
	}
	
	public static SingletonWebServiceReader getInstance() {
		if(instance == null)
			instance = new SingletonWebServiceReader();
		return instance;
	}
	
	public void registerCommand(String commandName, Class<? extends AbstractWebService> clazz){
		mapCommand.put(commandName, clazz);
	}
	
	public Class<? extends AbstractWebService> getCommand(String commandName){
		return mapCommand.get(commandName);
	}
	
	@Override
	public void run() {
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void initialiseAndRun() {
		this.start();
	}
}
