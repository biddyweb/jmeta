package org.meta.plugin.webservice;

import java.util.HashMap;
import java.util.Iterator;

import org.bson.types.BasicBSONList;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.meta.plugin.AbstractPluginWebServiceControler;

import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;


public class SingletonWebServiceReader extends Thread {

	private HashMap<String, AbstractPluginWebServiceControler> mapPlugins = null;
	private Server 	server 	= null;
	private static 	SingletonWebServiceReader instance 	= null;
	
	private SingletonWebServiceReader() {
		mapPlugins = new HashMap<String, AbstractPluginWebServiceControler>();
		this.start();
	}
	
	public static SingletonWebServiceReader getInstance() {
		if(instance == null)
			instance = new SingletonWebServiceReader();
		return instance;
	}
	
	public AbstractPluginWebServiceControler getPlugin(String commandName){
		return mapPlugins.get(commandName);
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

	public void registerPlugin(String pluginName,
			AbstractPluginWebServiceControler abstractPluginWebServiceControler) {
		mapPlugins.put(pluginName, abstractPluginWebServiceControler);
	}

	public String getPluginListAsJson() {
		BasicBSONList list = new BasicBSONList();
		for (Iterator<String> i = mapPlugins.keySet().iterator(); i.hasNext();){	
			String key = (String) i.next();
			list.add(key);
		}
		
		// Serialize BasicBSONList in JSON
		ObjectSerializer json_serializer = JSONSerializers.getStrict();
		return json_serializer.serialize(list);	
	}
	
}
