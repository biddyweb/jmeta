package org.meta.plugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.bson.types.BasicBSONList;
import org.meta.common.MetHash;
import org.meta.dht.DHTOperation;
import org.meta.dht.FindPeersOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;
import org.meta.model.Model;
import org.meta.plugin.tcp.SingletonTCPWriter;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.SingletonWebServiceReader;

import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;

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
 *
 * @author Thomas LAVOCAT
 *
 */
public abstract class AbstractPluginWebServiceControler {

    protected Model model = null;
    private SingletonWebServiceReader reader = null;
    protected LinkedHashMap<String, Class<? extends AbstractWebService>> 
                                                            lstCommands = null;
    protected AbstractPluginTCPControler tcpControler = null;
    protected String pluginName = null;

    public AbstractPluginWebServiceControler() {
        reader = SingletonWebServiceReader.getInstance();
        lstCommands = new LinkedHashMap<String, Class<? extends AbstractWebService>>();
    }

    /**
     * @param model the model to set
     */
    public void setModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    /**
     * initialize the plugin
     */
    public void init(String pluginName) {
        this.pluginName = pluginName;
        registercommands(lstCommands);
    }

    /**
     * Fil the lstCommands with all the needed TCP commands.
     *
     * @param lstCommands2
     */
    protected abstract void registercommands(
    		LinkedHashMap<String, Class<? extends AbstractWebService>> commands);

    public void setTcpControler(AbstractPluginTCPControler tcpControler) {
        this.tcpControler = tcpControler;

    }

    public Class<? extends AbstractWebService> getCommand(String command) {
        return lstCommands.get(command);
    }

    public String getJsonCommandList() {
        BasicBSONList list = new BasicBSONList();
        for (Iterator<String> i = lstCommands.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            list.add(key);
        }

        // Serialize BasicBSONList in JSON
        ObjectSerializer json_serializer = JSONSerializers.getStrict();
        return json_serializer.serialize(list);
    }

    public void search(final MetHash hash,
            final String plugin,
            final String command,
            final AbstractWebService abstractWebService) {


        FindPeersOperation peersOperation
                                        = MetaDHT.getInstance().findPeers(hash);
        peersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(FindPeersOperation operation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void complete(FindPeersOperation operation) {
              SingletonTCPWriter writer = SingletonTCPWriter.getInstance();
              Collection<MetaPeer> peers =  operation.getPeers();

              for(Iterator<MetaPeer> i = peers.iterator(); i.hasNext();){
                  MetaPeer peer = i.next();
                  InetAddress adress = peer.getAddress();
                  //TODO control ID validity
                  writer.askTo(    adress,
                                plugin,
                                command,
                                hash,
                                abstractWebService,
                                (int) peer.getPort());
              }
            }
        });
    }

}
