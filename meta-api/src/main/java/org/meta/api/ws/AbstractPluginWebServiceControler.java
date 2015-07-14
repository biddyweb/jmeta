package org.meta.api.ws;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.bson.types.BasicBSONList;
import org.meta.api.common.MetHash;
import org.meta.api.dht.FindPeersOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.dht.MetaPeer;
import org.meta.api.common.OperationListener;

import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;
import org.meta.api.amp.AMPResponseCallback;
import org.meta.api.amp.AMPWriter;
import org.meta.api.model.Model;

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
 * You may extends {@link AbstractPluginWebServiceControler} to create the WS
 * part of a plugin. It's allow you to register command to the webservice
 * reader, that can be executed by the user on his interface.
 *
 * Basically, this class offer generic treatment to serve interfaces and search
 * over DHT
 *
 * You may extends registerCommands wich allow you to tel the
 * {@link WebServiceReader} that you may have something to execute.
 *
 * You may use search to search a hash in the DHT, the datas, if founded will
 * arrive in the calback method.
 *
 * @author Thomas LAVOCAT
 *
 */
public abstract class AbstractPluginWebServiceControler {

    protected Model model = null;

    protected MetaDHT dht = null;

    protected AMPWriter ampWriter = null;

    protected LinkedHashMap<String, Class<? extends AbstractWebService>> lstCommands = null;
    protected String pluginName = null;

    public AbstractPluginWebServiceControler() {
        lstCommands = new LinkedHashMap<String, Class<? extends AbstractWebService>>();
    }

    /**
     * initialize the plugin
     */
    public void init(String pluginName) {
        this.pluginName = pluginName;
        registercommands(lstCommands);
    }

    /**
     * Fill the lstCommands with all the needed webservice commands.
     *
     * @param commands is a HashMap containing a key wich is the command name
     * and a Clas wich is the Class of the command.
     */
    protected abstract void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands);

    /**
     * @param command name of the command
     * @return the className of the command pointed by the given param
     */
    public Class<? extends AbstractWebService> getCommand(String command) {
        return lstCommands.get(command);
    }

    /**
     * Serialize as JSON the list of commands
     *
     * @return a list of commands as BSON
     */
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

    /**
     * Search something on the DHT. If you search a hash, you kown wich plugin
     * and wich command to contact. It correspond to a command you've developp
     * in TCP part
     *
     * @param hash in fact something is this hash
     * @param plugin name of the plugin to call
     * @param command command to execute
     * @param abstractWebService an abstractWebService to call back with the
     * results (or failure)
     */
    public void search(final MetHash hash,
            final String plugin,
            final String command,
            final AMPResponseCallback abstractWebService) {

        //Find peers for the given hash
        FindPeersOperation peersOperation = this.dht.findPeers(hash);

        //New opertaion
        peersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(FindPeersOperation operation) {
                abstractWebService.callbackFailure(operation.getFailureMessage());
            }

            @Override
            public void complete(FindPeersOperation operation) {
                /*
                 * foreach peer found, launch a contact wit TCPWriter
                 */

                Collection<MetaPeer> peers = operation.getPeers();

                for (Iterator<MetaPeer> i = peers.iterator(); i.hasNext();) {
                    MetaPeer peer = i.next();
                    InetAddress adress = peer.getAddress();
                    //TODO control ID validity
                    AbstractPluginWebServiceControler.this.ampWriter.askTo(adress,
                            plugin,
                            command,
                            hash,
                            abstractWebService,
                            (int) peer.getPort());
                }
            }
        });
    }

    /**
     * @param model the model to set
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * @return return the model object
     */
    public Model getModel() {
        return model;
    }

    public MetaDHT getDht() {
        return dht;
    }

    public void setDht(MetaDHT dht) {
        this.dht = dht;
    }

    public AMPWriter getAmpWriter() {
        return ampWriter;
    }

    public void setAmpWriter(AMPWriter ampWriter) {
        this.ampWriter = ampWriter;
    }
}
