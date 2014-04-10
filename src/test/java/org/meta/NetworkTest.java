package org.meta;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.meta.modele.DataFile;
import org.meta.modele.DataString;
import org.meta.modele.MetaData;
import org.meta.modele.Search;
import org.meta.modele.Searchable;
import org.meta.plugin.TCP.TCPReader;
import org.meta.plugin.TCP.TCPResponseCallback;
import org.meta.plugin.TCP.TCPWriter;

public class NetworkTest {
	public static void main(String[] args) {
		TCPReader reader = TCPReader.getInstance();
		reader.registerCommand("toto", CommanndTest.class);
		reader.initializePortAndRun(4001);
		TCPWriter writer = TCPWriter.getInstance();
		InetAddress adress;
		try {
			adress = InetAddress.getLocalHost();
			writer.askTo(adress, "toto", "youpi", new TCPResponseCallback() {
				
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader.kill();
	}
}
