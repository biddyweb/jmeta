package com.meta.plugin.TCP;

import java.io.Serializable;
import java.util.HashMap;

public class TCPWriter extends Thread{
		
	private HashMap<String, Serializable> 	mapNodeToServe 	= null;
	private boolean 						work			= true;
	
	public TCPWriter(){
		mapNodeToServe = new HashMap<String, Serializable>();
	}
	
	public void addToQueue(String ip, Serializable dataToSend){
		mapNodeToServe.put(ip, dataToSend);
	}
	
	@Override
	public void run() {
		//TODO here the code to switch from a queue to an other to 
		//serve everyone efficiently
		
		//for now the class is only able to open a connection
		//sent the message and pass to another node
		while(work){
			//TODO bind IP and put message
		}
	}
	
	public void kill(){
		work = false;
	}
}
