package org.meta.plugin.webservice;

import java.util.HashMap;
import java.util.Iterator;

import org.bson.types.BasicBSONList;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;


public class SingletonWebServiceReader extends Thread {

	private HashMap<String, Class<? extends AbstractWebService>> mapCommand = null;
	private Server 	server 	= null;
	private static 	SingletonWebServiceReader instance 	= null;
	
	private SingletonWebServiceReader() {
		mapCommand = new HashMap<String, Class<? extends AbstractWebService>>();
		this.start();
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
		server = new Server(8080);//TODO

		// serve statics files within 'static' directory
		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setDirectoriesListed(true);
		resource_handler.setResourceBase("static");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler, new WebRequestHandler() });

		server.setHandler(handlers);
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
	
	/**
	 * 
	 * @return the plugin list as a json
	 */
	public String getCommandListAsJson() {
		BasicBSONList list = new BasicBSONList();
		for (Iterator<String> i = mapCommand.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			list.add(key);
		}
		return list.toString();
	}
}
