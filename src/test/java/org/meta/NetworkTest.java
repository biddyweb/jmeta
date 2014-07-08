package org.meta;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.meta.model.DataFile;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.SingletonTCPReader;
import org.meta.plugin.tcp.TCPResponseCallbackInteface;
import org.meta.plugin.tcp.SingletonTCPWriter;

public class NetworkTest {
	public static void main(String[] args) {
		SingletonTCPReader reader = SingletonTCPReader.getInstance();
		
		AbstractPluginTCPControler pluginTest = new AbstractPluginTCPControler(){

			@Override
			protected void registercommands(
					HashMap<String, Class<? extends AbstractCommand>> commands) {
				commands.put("toto", CommanndTest.class);
			}
			
		};
		pluginTest.init("truc");
		reader.registerPlugin("truc", pluginTest);
		
		reader.initializePortAndRun(4001);
		SingletonTCPWriter writer = SingletonTCPWriter.getInstance();
		InetAddress adress;
		try {
			adress = InetAddress.getLocalHost();
			writer.askTo(adress, "truc", "toto", "youpi", new TCPResponseCallbackInteface() {
				
				@Override
				public void callback(ArrayList<Searchable> results) {
					for (int i=0; i<results.size(); i++) {
						Searchable searchable = results.get(i);
						
						if(searchable instanceof Search){
							Search search3 = (Search) searchable;
							System.out.println(search3.getTmpResultsHashes());
						}else if(searchable instanceof MetaData){
							MetaData metaData3 = (MetaData) searchable;
							System.out.println(metaData3.getTmpLinkedData());
						}else if(searchable instanceof DataFile){
							DataFile dataFile3 = (DataFile) searchable;
							System.out.println(dataFile3.getFile().getName());
						}else if(searchable instanceof DataString){
							DataString dataString = (DataString) searchable;
							System.out.println(dataString.getString());
						}
					}
				}
			});
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		reader.kill();
	}
}
