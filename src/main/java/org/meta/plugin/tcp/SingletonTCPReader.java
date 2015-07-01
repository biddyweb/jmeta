/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 JMeta
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
package org.meta.plugin.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.meta.configuration.MetaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.meta.plugin.AbstractPluginTCPControler;

/**
 * Class listening to peer-to-peer connections over TCP/AMP.
 */
public class SingletonTCPReader extends Thread {

    private static SingletonTCPReader instance = new SingletonTCPReader();
    private static final Logger logger = LoggerFactory.getLogger(SingletonTCPReader.class);
    private ExecutorService executor = null;

    /**
     * True while we should run.
     */
    private boolean work = true;

    /**
     * The server socket.
     */
    private ServerSocket socket = null;

    /**
     * The list of plugins TCP handlers
     */
    private HashMap<String, AbstractPluginTCPControler> mapPlugin = null;

    /**
     * initiate a pluginMap
     */
    private SingletonTCPReader() {
        mapPlugin = new HashMap<>();
        executor = Executors.newFixedThreadPool(100);//TODO add to configuration
    }

    @Override
    public void run() {
        try {
            Short port = MetaConfiguration.getAmpConfiguration().getAmpPort();
            socket = new ServerSocket(port);
            while (work) {
                Socket client = socket.accept();
                //Once a connection is accepted, let AskHandlerThread take
                //care of the rest
                AskHandlerThread discussWith = new AskHandlerThread(client);
                executor.submit(discussWith);
            }
        } catch (IOException e) {
            if (work == true) {
                //TODO Handle correcty this error
                logger.error("Socket error.", e);
            } else {
                logger.info("Tcp thread exiting");
            }
        }
    }

    /**
     * @return the SingletonTCPReader instance
     */
    public static SingletonTCPReader getInstance() {
        if (instance == null) {
            instance = new SingletonTCPReader();
        }
        return instance;
    }

    /**
     * Register a plugin to this TCPReader
     * 
     * @param pluginName                    pluginName
     * @param abstractPluginTCPControler    plugin tcp controler
     */
    public void registerPlugin(String pluginName,
            AbstractPluginTCPControler abstractPluginTCPControler) {
        mapPlugin.put(pluginName, abstractPluginTCPControler);
    }

    /**
     * return the command pointed by pluginName and commandName
     * 
     * @param pluginName    the plugin name
     * @param commandName   the command Name
     * 
     * @return a command to execute if found, null otherwise 
     */
    public AbstractCommand getCommand(String pluginName, String commandName) {
        AbstractCommand command = null;
        AbstractPluginTCPControler plugin = mapPlugin.get(pluginName);

        if (plugin != null) {
            command = plugin.getCommand(commandName);
        }
        return command;
    }

    /**
     * kill the current listing on the server socket
     * Remember there is a timeout for closing a socket
     * Should only be called at the end of the program life cycle
     */
    public void kill() {
        work = false;
        try {
            if(this.socket != null)
                this.socket.close();
        } catch (IOException ex) {
            //No-op here, ex will occur but this is what we want to do.
        }
        try {
            this.join();
        } catch (InterruptedException ex) {
            logger.error("Interruption while waiting for thread to finish", ex);
        }
    }
}
