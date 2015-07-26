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
package org.meta.api.ws;

import com.mongodb.util.JSONSerializers;
import com.mongodb.util.ObjectSerializer;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.bson.types.BasicBSONList;
import org.meta.api.amp.AMPResponseCallback;
import org.meta.api.amp.AMPWriter;
import org.meta.api.common.MetHash;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.FindPeersOperation;
import org.meta.api.dht.MetaDHT;
import org.meta.api.dht.MetaPeer;
import org.meta.api.model.Model;

/**
 * You may extend this class to create the WS part of a plugin. It's allow you to register command to the
 * webservice reader, that can be executed by the user on his interface.
 *
 * Basically, this class offer generic treatment to serve interfaces and search over DHT
 *
 * You may extends registerCommands wich allow you to tel the {@link WebServiceReader} that you may have
 * something to execute.
 *
 * You may use search to search a hash in the DHT, the datas, if founded will arrive in the calback method.
 *
 * @author Thomas LAVOCAT
 *
 */
public abstract class AbstractPluginWebServiceControler {

    /**
     *
     */
    protected Model model = null;

    /**
     *
     */
    protected MetaDHT dht = null;

    /**
     *
     */
    protected AMPWriter ampWriter = null;

    /**
     *
     */
    protected LinkedHashMap<String, Class<? extends AbstractWebService>> lstCommands = null;

    /**
     *
     */
    protected String pluginName = null;

    /**
     *
     */
    public AbstractPluginWebServiceControler() {
        lstCommands = new LinkedHashMap<>();
    }

    /**
     * Initialize the plugin.
     *
     * @param name the plugin name to initialize.
     */
    public final void init(final String name) {
        this.pluginName = name;
        registercommands(lstCommands);
    }

    /**
     * Fill the lstCommands with all the needed webservice commands.
     *
     * @param commands is a HashMap containing a key wich is the command name and a Clas wich is the Class of
     * the command.
     */
    protected abstract void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands);

    /**
     * @param command name of the command
     * @return the className of the command pointed by the given param
     *
     * TODO return an instance here instead of a Class!
     */
    public final Class<? extends AbstractWebService> getCommand(final String command) {
        return lstCommands.get(command);
    }

    /**
     * Serialize as JSON the list of commands.
     *
     * @return a list of commands as JSON
     */
    public final String getJsonCommandList() {
        BasicBSONList list = new BasicBSONList();
        for (Iterator<String> i = lstCommands.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            list.add(key);
        }

        // Serialize BasicBSONList in JSON
        ObjectSerializer jsonSerializer = JSONSerializers.getStrict();
        return jsonSerializer.serialize(list);
    }

    /**
     * Search something on the DHT. If you search a hash, you know which plugin and which command to contact.
     * It corresponds to a command you've developed in TCP part.
     *
     * TODO move this logic elsewhere!
     *
     * @param hash in fact something is this hash
     * @param plugin name of the plugin to call
     * @param command command to execute
     * @param abstractWebService an abstractWebService to call back with the results (or failure)
     */
    public final void search(final MetHash hash,
            final String plugin,
            final String command,
            final AMPResponseCallback abstractWebService) {

        //Find peers for the given hash
        FindPeersOperation peersOperation = this.dht.findPeers(hash);

        //New operation
        peersOperation.addListener(new OperationListener<FindPeersOperation>() {

            @Override
            public void failed(final FindPeersOperation operation) {
                abstractWebService.callbackFailure(operation.getFailureMessage());
            }

            @Override
            public void complete(final FindPeersOperation operation) {
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
     * @param metaModel the model to set
     */
    public final void setModel(final Model metaModel) {
        this.model = metaModel;
    }

    /**
     * @return return the model object
     */
    public final Model getModel() {
        return model;
    }

    /**
     *
     * @return the Meta DHT instance
     */
    public final MetaDHT getDht() {
        return dht;
    }

    /**
     *
     * @param metaDht define the DHT instance
     */
    public final void setDht(final MetaDHT metaDht) {
        this.dht = metaDht;
    }

    /**
     * @return the instance of the AMP writer.
     */
    public final AMPWriter getAmpWriter() {
        return ampWriter;
    }

    /**
     *
     *
     * @param metaAmpWriter define the AMP writer instance
     */
    public final void setAmpWriter(final AMPWriter metaAmpWriter) {
        this.ampWriter = metaAmpWriter;
    }
}
