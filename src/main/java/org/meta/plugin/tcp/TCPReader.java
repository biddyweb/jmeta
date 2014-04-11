package org.meta.plugin.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


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
 * Singleton who is listened to the request from DHT
 * @author Thomas LAVOCAT
 *
 */
public class TCPReader extends Thread{

	private HashMap<String, Class<? extends AbstractCommand>> mapCommand 	= null;
	private 			boolean 		work		= true;
	private static 		TCPReader 		instance 	= new TCPReader();
	private 			int				port		= 4001;
	private				ServerSocket 	socket		= null;
	
	private TCPReader() {
		mapCommand = new HashMap<String, Class<? extends AbstractCommand>>();
	}
	
	public static TCPReader getInstance() {
		return instance;
	}
	
	public void registerCommand(String commandName, Class<? extends AbstractCommand> clazz){
		mapCommand.put(commandName, clazz);
	}
	
	public Class<? extends AbstractCommand> getCommand(String commandName){
		return mapCommand.get(commandName);
	}
	
	@Override
	public void run() {
		try {
			socket = new ServerSocket(port);
			while(work){
				Socket client = socket.accept();
				AskHandlerThread discussWith = new AskHandlerThread(client);
				discussWith.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public void kill(){
		work = false;
	}

	public void initializePortAndRun(int port) {
		this.port = port;
		this.start();
	}
}
