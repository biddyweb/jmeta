package org.meta.plugins.SimpleHashMap.webservicecommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.meta.api.model.Data;
import org.meta.api.model.DataString;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaProperty;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.api.ws.AbstractWebService;
import org.meta.api.ws.forms.InterfaceDescriptor;
import org.meta.api.ws.forms.fields.TextInput;
import org.meta.api.ws.forms.fields.TextOutput;
import org.meta.api.ws.forms.submit.SelfSubmitButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class Get extends AbstractWebService{

    InterfaceDescriptor  initialDescriptor   = null;
    TextOutput           output              = null;
    ModelFactory         factory             = null;
    ArrayList<DataString>distantResults      = null;
    ArrayList<DataString>localResults        = null;
    private Logger logger = LoggerFactory.getLogger(Get.class);
    
    /**
     *
     * @param controler
     */
    public Get(AbstractPluginWebServiceControler controler){
        super(controler);
        distantResults = new ArrayList<DataString>();
        TextInput path = new TextInput("id", "ID");
        rootColumn.addChild(path);
        output = new TextOutput("result", "Result");
        rootColumn.addChild(new SelfSubmitButton("submitToMe", "Search"));
        rootColumn.addChild(output);
        initialDescriptor = new InterfaceDescriptor(rootColumn);
        factory = controler.getModel().getFactory();
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
    	distantResults = new ArrayList<DataString>();
    	output.flush();
        localResults = new ArrayList<DataString>();
        String id = getParameter("id", map);
        
        if(id != ""){
            output.flush();
            TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
            properties.add(new MetaProperty("hashmap", "value"));
            
            MetaData    metaData     = factory.createMetaData(properties);
            DataString  source       = factory.createDataString(id);
            Search      contentSearch= factory.createSearch(source, metaData, null);
            logger.info("get hash : "+contentSearch.getHash().toString());

            super.controller.search(  contentSearch.getHash(),
                                    "SimpleHashMap",
                                    "getCommand",
                                    this);
            
            Search localResult =
                    (Search) controller.getModel().get(contentSearch.getHash());
            if(localResult != null){
                Collection<Data> localDatas = localResult.getLinkedData();
                for(Data d : localDatas)
                    if(d instanceof DataString)
                        localResults.add((DataString) d);
            }
            redrawOutput();
        }else{
            output.flush();
            output.append("Please set an id");
        }
    }

    @Override
    public void applySmallUpdate() {
        redrawOutput();
    }
    

    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        output.flush();
        //Those results are incomplete
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();
            if (searchable instanceof Search) {
                Search   search   = (Search) searchable;
                Collection<Data> linkDatas =    search.getLinkedData();
                for (Iterator<Data> k = linkDatas.iterator(); k .hasNext();) {
                    Data data = (Data) k.next();
                    if(data instanceof DataString)
                        this.distantResults.add((DataString) data);
                }
            }
        }
        redrawOutput();
    }

    @Override
    public void callbackFailure(String failureMessage) {
        // TODO Auto-generated method stub
        
    }

    private void redrawOutput() {
        output.flush();
        for(DataString result : this.distantResults){
            output.append("distant : "+result.getString());
        }
        for(DataString result : this.localResults){
            output.append("local : "+result.getString());
        }
        output.append("waiting for results");
    }
}
