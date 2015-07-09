package org.meta.plugin.webservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.meta.dht.DHTOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.OperationListener;
import org.meta.model.Data;
import org.meta.model.MetaProperty;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.tcp.TCPResponseCallbackInteface;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.organizers.ColumnOrganizer;

/**
 * Define how need to work a web service command
 *
 * To register a new web service command you must extends this class, and
 * override at least : - execute command - apply small update
 *
 * You should build your interface in the default constructor, which is required
 *
 * @author faquin
 *
 */
public abstract class AbstractWebService implements TCPResponseCallbackInteface {

    protected ColumnOrganizer rootColumn = null;
    protected InterfaceDescriptor descriptor = null;

    protected AbstractPluginWebServiceControler controler = null;

    /**
     * You need to build you user interface in here. For that, you shall need
     * the root element, named rootColumn.
     *
     * Fill it with anything you want to build your interface
     *
     */
    public AbstractWebService(AbstractPluginWebServiceControler controler) {
        this.controler = controler;
        rootColumn = new ColumnOrganizer("root");
        descriptor = new InterfaceDescriptor(rootColumn);
    }

    /**
     *
     * @param controler give WebServiceControler
     */
    public void setWebServiceControler(AbstractPluginWebServiceControler controler) {
        this.controler = controler;
    }

    /**
     *
     * @return an interface who will be translate as JSON.
     *
     * Client side will surely build a human interface to allow final user to
     * interact with this webservice commands.
     *
     * Note that you can totally bypass the interface, but you loose interest of
     * webservice commands.
     */
    public InterfaceDescriptor getInterface() {
        return descriptor;
    }

    /**
     * Execute the command with the given parameters
     *
     * @param map parameter map from jetty
     * @return the interface descriptor.
     *
     */
    public InterfaceDescriptor execute(Map<String, String[]> map) {
        executeCommand(map);
        return descriptor;
    }

    /**
     * When a final user is ready to interact with your webservice command, he
     * will execute it. Executing your command, mean, to give you some
     * parameters to make it do some work.
     *
     * Parameters come into the map of parameters.
     *
     * @param map a simple map, where for each key, you may find or not an array
     * of values. Those are given by Jetty, which is a http server. So,
     * parameters are given by the end user as a get string :
     * ?key1=bar;key2=foo;key2=barfoo
     *
     * you can easily lookup for parameters using the following tomcat style
     * methods : - getParameter - getParameters
     *
     * If you want any output, make sure you apply small changes to your
     * interface. Beware that big changes are not tested yet, but you want to
     * give a try, your feedback will be warm welcome.
     *
     * Remember that your operation is bloking the user interface, so, faster
     * you send him a result, better it is.
     *
     * You may surely not be able to give any results for now, especially if you
     * search on the DHT see search method in
     * {@link AbstractPluginTCPControler}. They will arrive later in the
     * callback method.
     *
     */
    protected abstract void executeCommand(Map<String, String[]> map);

    /**
     * After execution, some client will fetch the interface every X ms.
     *
     * @return the modified interfaceDescripor
     */
    public InterfaceDescriptor retrieveUpdate() {
        applySmallUpdate();
        return descriptor;
    }

    /**
     *
     * After calling execution, most clients (especially ours) will fetch the
     * interface every X ms, to see if there any changes or any new results.
     *
     * if you want to make small changes in the interface, it's possible here.
     *
     * By small changes, we mean, - make an other DHT search, - take newly
     * arrived results in the callback method and add them into the output text
     * object. - ...
     *
     * Remember that, for now (LSP version) this method will be called every
     * 500ms.
     *
     */
    protected abstract void applySmallUpdate();

    @PreDestroy
    public void kill() {
        controler = null;
    }

    /**
     *
     * @param name name of the parameter
     * @param map map from jetty
     * @return the parameter value or null if not found
     */
    public String[] getParameters(String name, Map<String, String[]> map) {
        return map.get(name);
    }

    /**
     *
     * @param name name of the parameter array
     * @param map map from jetty
     * @return String[] containing the values or null if not found
     */
    public String getParameter(String name, Map<String, String[]> map) {
        String parameter = null;
        String[] parameters = getParameters(name, map);
        if (parameters != null && parameters.length > 0) {
            parameter = parameters[0];
        }
        return parameter;
    }

    /**
     * Update a search with the new result
     *
     * Will check in DB if the Data are already here, in these case, it will not
     * override the data, but just apply changes.
     *
     * After calling this method newSearch will contain newResult has hi list of
     * results.
     *
     * In this case, try to
     *
     * @param newSearch
     * @param newResult
     * @return
     */
    protected Search updateSearch(Search newSearch, Data newResult) {
        //try to get the same from model
        Search searchDB = controler.getModel().getSearch(newSearch.getHash());
        if (searchDB != null) {
            newSearch = searchDB;
        }
        newSearch.setALinkedData(newResult);
        return newSearch;
    }

    /**
     * Look if the content already exist in the DB, and update the reference in
     * this case.
     *
     * Auto merge the Data description objects
     *
     * @param newResult newResultToUpdate
     */
    protected Data updateResult(Data newResult) {
        //If dbResult was not null, remove it and add the new result instead
        //try to get it in the DB
        Data resultDB = controler.getModel()
                .getDataFile(newResult.getHash());
        //if result exist in the DB, just adjust newResult reference
        //to point to it

        //get new description
        ArrayList<MetaProperty> newDescription = newResult.getDescription();

        if (resultDB != null) {
            newResult = resultDB;
        }

        //get db description
        ArrayList<MetaProperty> mergeDescription = newResult.getDescription();

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
        newResult.setDescription(mergeDescription);

        return newResult;
    }

    /**
     * Save the searchable in the DB and push his hash to the DHT
     *
     * @param seachable
     */
    protected void saveAndPush(Searchable searchable) {
        //write into dataBase
        onlySave(searchable);
        //and store it to the DHT
        onlyPush(searchable);
    }

    /**
     * Only save the searchable in the db
     *
     * @param searchable
     */
    protected void onlySave(Searchable searchable) {
        //TODO Error check
        this.controler.getModel().set(searchable);
    }

    /**
     * push the hash of the searchable to the DHT
     *
     * @param searchable
     */
    protected void onlyPush(final Searchable searchable) {
        //store search into DHT 
        this.controler.getDht().store(searchable.getHash()).addListener(
                new OperationListener<DHTOperation>() {

                    @Override
                    public void failed(DHTOperation operation) {
                        callbackFailedToPush(operation, searchable);
                    }

                    @Override
                    public void complete(DHTOperation operation) {
                        callbackSuccessToPush(operation, searchable);
                    }

                });
    }

    protected void callbackFailedToPush(DHTOperation operation, Searchable searchable) {
    }

    protected void callbackSuccessToPush(DHTOperation operation, Searchable searchable) {
    }
}
