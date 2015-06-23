package org.meta.plugin.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meta.plugin.AbstractPluginTCPControler;


/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Thomas LAVOCAT
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Singleton who is listened to the request from DHT TODO need a cleaning ?
 *
 * @author Thomas LAVOCAT
 *
 */
public class SingletonTCPReader {

    private boolean work = true;
    private int port = 4001;
    private ServerSocket socket = null;
    private Thread listenerThread = null;
    private HashMap<String, AbstractPluginTCPControler> mapPlugin = null;
    private static SingletonTCPReader instance = new SingletonTCPReader();

    //The thead routine.
    private Runnable listenerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new ServerSocket(port);
                while (work) {
                    Socket client = socket.accept();
                    AskHandlerThread discussWith = new AskHandlerThread(client);
                    discussWith.start();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private SingletonTCPReader() {
        mapPlugin = new HashMap<String, AbstractPluginTCPControler>();
    }

    public static SingletonTCPReader getInstance() {
        return instance;
    }

    public void registerPlugin(String pluginName,
            AbstractPluginTCPControler abstractPluginTCPControler) {
        mapPlugin.put(pluginName, abstractPluginTCPControler);
    }

    public AbstractCommand getCommand(String pluginName, String commandName) {
        AbstractCommand command = null;
        AbstractPluginTCPControler plugin = mapPlugin.get(pluginName);

        if (plugin != null) {
            command = plugin.getCommand(commandName);
        }

        return command;
    }

    public void kill() {
        work = false;
        try {
            this.listenerThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(SingletonTCPReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if(socket != null)
                socket.close();
        } catch (IOException ex) {
            Logger.getLogger(SingletonTCPReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initializePortAndRun(int port) {
        this.port = port;
        this.listenerThread = new Thread(this.listenerRunnable);
        this.listenerThread.start();
    }
}
