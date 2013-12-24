package com.meta.plugin.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientHandlerThread extends Thread{
	
	private Socket 		client	= null;
	private TCPReader 	rearder = TCPReader.getInstance();

	public ClientHandlerThread(Socket client){
		this.client = client;
	}
	
	@Override
	public void run() {
		try {
			InputStream inputStream = client.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
