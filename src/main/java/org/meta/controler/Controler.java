package org.meta.controler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.meta.controler.P2P.P2PControler;
import org.meta.modele.Model;
import org.meta.plugin.PluginTCPControler;
import org.meta.plugin.PluginWebServiceControler;
import org.meta.plugin.tcp.TCPReader;

import djondb.LibraryException;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 * @author Thomas LAVOCAT
 * 
 */
public class Controler {
	
	private Properties 		pluginsProperties		= 	null;
	private Properties		configuration			=	null;
	private Model 			model 					=	null;
	private String 			pluginsropertiesFile	=	"conf/plugins.prop";
	private String 			confPropertiesFile		=	"conf/jmeta.prop";
	private P2PControler 	p2pControler 			= 	null;
	private ArrayList<String>							lstPluginsNames			=	null;
	private HashMap<String, PluginTCPControler> 		mapTCPControler			=	null;
	private HashMap<String, PluginWebServiceControler>	mapWebServiceControler	=	null;
	
	/**
	 * 
	 * @throws LibraryException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	 public Controler() 
		 throws 	LibraryException, 
		 			IOException, 
		 			URISyntaxException
	{
		 //TODO initialize configuration
		 this.model = new Model();
		 FileInputStream pluginInput = new FileInputStream(
				 					new File(pluginsropertiesFile));
		 FileInputStream confInput = new FileInputStream(
				 					new File(confPropertiesFile));
		 this.pluginsProperties = new Properties();
		 this.configuration 	= new Properties();
		 pluginsProperties.load(pluginInput);
		 configuration.load(confInput);
		 this.p2pControler = new P2PControler(Integer.parseInt(configuration.getProperty("port")));
		 TCPReader.getInstance().initializePortAndRun(Integer.parseInt(configuration.getProperty("port")));
		 pluginInitialisation();
	 }
	 
	 /**
	  * TODO
	  */
	private void pluginInitialisation() {
		lstPluginsNames			= new ArrayList<String>();
		mapTCPControler 		= new HashMap<String, PluginTCPControler>();
		mapWebServiceControler	= new HashMap<String, PluginWebServiceControler>();
		
		Enumeration<Object> keys = pluginsProperties.keys();
		while(keys.hasMoreElements()){
			String key = ((String) keys.nextElement());
			//TODO split sur les points et prendre le dernier élément
			if(key.contains(".name")){
				//plugin founded
				lstPluginsNames.add(pluginsProperties.getProperty(key));
				//load TCP class
				String strTCPClass = pluginsProperties.getProperty(key.replaceAll(".name", "")+".TCPClass");
				//load web service class
				String strWSClass  = pluginsProperties.getProperty(key.replaceAll(".name", "")+".WSClass");
				
				try {
					Class<?> clazzTCP 	= Class.forName(strTCPClass);
					Class<?> clazzWS	= Class.forName(strWSClass);
					
					//load TCPControler
					PluginTCPControler tcpControler = 
							(PluginTCPControler)   clazzTCP.newInstance();
					//load webServiceControler
					PluginWebServiceControler webServiceControler = 
							(PluginWebServiceControler) clazzWS.newInstance();
					
					//Set parameters
					tcpControler.setP2pControler(p2pControler);
					tcpControler.setModel(model);
					//Set parameters
					webServiceControler.setModel(model);
					webServiceControler.setTcpControler(tcpControler);
					//init TCP and WS parts
					tcpControler.init();
					webServiceControler.init();
					
				} catch (ClassNotFoundException e) {
					System.out.println("The plugin "+key+" is not available");
					mapTCPControler.remove(key);
					mapWebServiceControler.remove(key);
					lstPluginsNames.remove(key);
				} catch (InstantiationException e) {
					System.out.println(
							"Error during instanciation of th plugin : "+key);
					mapTCPControler.remove(key);
					mapWebServiceControler.remove(key);
					lstPluginsNames.remove(key);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}
}
