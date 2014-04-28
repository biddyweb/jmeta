package org.meta.plugin.webservice;

import java.util.HashMap;

import org.meta.plugin.tcp.AbstractCommand;

public class SingletonWebServiceReader extends Thread{

	private HashMap<String, Class<?>> mapCommand 	= null;
	private 			boolean 			work		= true;
	private static 		SingletonWebServiceReader 	instance 	= new SingletonWebServiceReader();
	
	private SingletonWebServiceReader() {
		mapCommand = new HashMap<String, Class<?>>();
	}
	
	public static SingletonWebServiceReader getInstance() {
		return instance;
	}
	
	public void registerCommand(String commandName, Class<? extends AbstractCommand> clazz){
		mapCommand.put(commandName, clazz);
	}
	
	public Class<?> getCommand(String commandName){
		return mapCommand.get(commandName);
	}
	
	@Override
	public void run() {
		while(work){
		}
	}	
	
	public void kill(){
		work = false;
	}

	public void initialiseAndRun(int port) {
		this.start();
	}
}
