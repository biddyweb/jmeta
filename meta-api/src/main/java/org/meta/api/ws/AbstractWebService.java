/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.api.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.meta.api.amp.AMPResponseCallback;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.model.MetaProperty;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.api.ws.forms.InterfaceDescriptor;
import org.meta.api.ws.forms.organizers.ColumnOrganizer;

/**
 * Define how need to work a web service command.
 *
 * To register a new web service command you must extends this class, and override at least : - execute
 * command - apply small update
 *
 * You should build your interface in the default constructor, which is required
 *
 * @author faquin
 *
 */
public abstract class AbstractWebService implements AMPResponseCallback {

    /**
     *
     */
    protected ColumnOrganizer rootColumn = null;

    /**
     *
     */
    protected InterfaceDescriptor descriptor = null;

    /**
     *
     */
    protected AbstractPluginWebServiceControler controller = null;

    /**
     * You need to build you user interface in here. For that, you shall need the root element, named
     * rootColumn.
     *
     * Fill it with anything you want to build your interface
     *
     * @param wsController the parent web service controller
     */
    public AbstractWebService(final AbstractPluginWebServiceControler wsController) {
        this.controller = wsController;
        rootColumn = new ColumnOrganizer("root");
        descriptor = new InterfaceDescriptor(rootColumn);
    }

    /**
     *
     * @param wsController give WebServiceControler
     */
    public final void setWebServiceControler(final AbstractPluginWebServiceControler wsController) {
        this.controller = wsController;
    }

    /**
     *
     * @return an interface who will be translate as JSON.
     *
     * Client side will surely build a human interface to allow final user to interact with this webservice
     * commands.
     *
     * Note that you can totally bypass the interface, but you loose interest of webservice commands.
     */
    public final InterfaceDescriptor getInterface() {
        return descriptor;
    }

    /**
     * Execute the command with the given parameters.
     *
     * @param map parameter map from jetty
     * @return the interface descriptor.
     *
     */
    public final InterfaceDescriptor execute(final Map<String, String[]> map) {
        executeCommand(map);
        return descriptor;
    }

    /**
     * When a final user is ready to interact with your webservice command, he will execute it. Executing your
     * command, mean, to give you some parameters to make it do some work.
     *
     * Parameters come into the map of parameters.
     *
     * @param map a simple map, where for each key, you may find or not an array of values. Those are given by
     * Jetty, which is a http server. So, parameters are given by the end user as a get string :
     * ?key1=bar;key2=foo;key2=barfoo
     *
     * you can easily lookup for parameters using the following tomcat style methods : - getParameter -
     * getParameters
     *
     * If you want any output, make sure you apply small changes to your interface. Beware that big changes
     * are not tested yet, but you want to give a try, your feedback will be warm welcome.
     *
     * Remember that your operation is bloking the user interface, so, faster you send him a result, better it
     * is.
     *
     * You may surely not be able to give any results for now, especially if you search on the DHT see search
     * method in {@link PluginAMPController}. They will arrive later in the callback method.
     *
     */
    protected abstract void executeCommand(final Map<String, String[]> map);

    /**
     * After execution, some client will fetch the interface every X ms.
     *
     * @return the modified interfaceDescripor
     */
    public final InterfaceDescriptor retrieveUpdate() {
        applySmallUpdate();
        return descriptor;
    }

    /**
     *
     * After calling execution, most clients (especially ours) will fetch the interface every X ms, to see if
     * there any changes or any new results.
     *
     * if you want to make small changes in the interface, it's possible here.
     *
     * By small changes, we mean, - make an other DHT search, - take newly arrived results in the callback
     * method and add them into the output text object. - ...
     *
     * Remember that, for now (LSP version) this method will be called every 500ms.
     *
     */
    protected abstract void applySmallUpdate();

    /**
     * TODO ?
     */
    public final void kill() {
        controller = null;
    }

    /**
     *
     * @param name name of the parameter
     * @param map map from jetty
     * @return an array of values for the key or null if not found
     */
    public final String[] getParameters(final String name, final Map<String, String[]> map) {
        return map.get(name);
    }

    /**
     *
     * @param name name of the parameter array
     * @param map map from jetty
     * @return the value for the key or null if not found
     */
    public final String getParameter(final String name, final Map<String, String[]> map) {
        String parameter = null;
        String[] parameters = getParameters(name, map);
        if (parameters != null && parameters.length > 0) {
            parameter = parameters[0];
        }
        return parameter;
    }

    /**
     * Update a search with the new result.
     *
     * Will check in DB if the Data are already here, in these case, it will not override the data, but just
     * apply changes.
     *
     * After calling this method newSearch will contain newResult has hi list of results.
     *
     * In this case, try to
     *
     * @param newSearch the search to update
     * @param newResult the result to add
     * @return the updated search
     */
    protected final Search updateSearch(final Search newSearch, final Data newResult) {
        //try to get the same from model
        Search searchDB = controller.getModel().getSearch(newSearch.getHash());
        if (searchDB == null) {
            searchDB = newSearch;
        }
        searchDB.setLinkedData(newResult);
        return searchDB;
    }

    /**
     * Look if the content already exist in the DB, and update the reference in this case.
     *
     * Auto merge the Data description objects. TODO This logic should not be here!
     *
     * @param newResult newResultToUpdate
     * @return the updated data
     */
    protected final Data updateResult(final Data newResult) {
        //If dbResult was not null, remove it and add the new result instead
        //try to get it in the DB
        Data resultDB = controller.getModel()
                .getDataFile(newResult.getHash());
        //if result exist in the DB, just adjust newResult reference
        //to point to it

        if (resultDB == null) {
            return newResult;
        }

        //get new description
        ArrayList<MetaProperty> newDescription = newResult.getDescription();

        //get db description
        ArrayList<MetaProperty> mergeDescription = resultDB.getDescription();

        //for each description of the new one
        for (MetaProperty desc : newDescription) {
            boolean alreadyExist = false;
            for (Iterator<MetaProperty> i = mergeDescription.iterator(); !alreadyExist && i.hasNext();) {
                alreadyExist = desc.compareTo(i.next()) == 0;
            }
            //if do not exists, add it
            if (!alreadyExist) {
                mergeDescription.add(desc);
            }
        }
        resultDB.setDescription(mergeDescription);
        return resultDB;
    }

    /**
     * Save the searchable in the DB and push his hash to the DHT.
     *
     * @param searchable the searchable to save and push
     */
    protected final void saveAndPush(final Searchable searchable) {
        //write into dataBase
        onlySave(searchable);
        //and store it to the DHT
        onlyPush(searchable);
    }

    /**
     * Only save the searchable in the db.
     *
     * @param searchable the searchable to save
     */
    protected final void onlySave(final Searchable searchable) {
        //TODO Error check
        this.controller.getModel().set(searchable);
    }

    /**
     * push the hash of the searchable to the DHT.
     *
     * @param searchable the searchable to push
     */
    protected final void onlyPush(final Searchable searchable) {
        //store search into DHT
        this.controller.getDht().store(searchable.getHash())
                .addListener(new OperationListener<AsyncOperation>() {

                    @Override
                    public void failed(final AsyncOperation operation) {
                        callbackFailedToPush(operation, searchable);
                    }

                    @Override
                    public void complete(final AsyncOperation operation) {
                        callbackSuccessToPush(operation, searchable);
                    }

                });
    }

    /**
     * @param operation the operation that failed.
     * @param searchable the searchable
     */
    protected void callbackFailedToPush(final AsyncOperation operation, final Searchable searchable) {
    }

    /**
     * @param operation the operation that succeeded.
     * @param searchable the searchable
     */
    protected void callbackSuccessToPush(final AsyncOperation operation, final Searchable searchable) {
    }
}
