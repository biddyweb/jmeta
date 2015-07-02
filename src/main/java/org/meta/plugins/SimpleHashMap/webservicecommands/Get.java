package org.meta.plugins.SimpleHashMap.webservicecommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.meta.model.Data;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.Model;
import org.meta.model.ModelFactory;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.fields.TextInput;
import org.meta.plugin.webservice.forms.fields.TextOutput;
import org.meta.plugin.webservice.forms.submit.SelfSubmitButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Get extends AbstractWebService{

    InterfaceDescriptor  initialDescriptor   = null;
    TextOutput           output              = null;
    ModelFactory         factory             = null;
    ArrayList<DataString>distantResults      = null;
    ArrayList<DataString>localResults        = null;
    private Logger logger = LoggerFactory.getLogger(Get.class);
    
    public Get(){
        distantResults = new ArrayList<DataString>();
        TextInput path = new TextInput("id", "ID");
        rootColumn.addChild(path);
        output = new TextOutput("result", "Result");
        rootColumn.addChild(new SelfSubmitButton("submitToMe", "Search"));
        rootColumn.addChild(output);
        initialDescriptor = new InterfaceDescriptor(rootColumn);
        try {
            factory = Model.getInstance().getFactory();
        } catch (ModelException ex) {
        }
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
            System.out.print("get hash : "+contentSearch.getHash().toString());

            super.controler.search(  contentSearch.getHash(),
                                    "SimpleHashMap",
                                    "getCommand",
                                    this);
            
            try {
                Search localResult =
                        (Search) Model.getInstance().get(contentSearch.getHash());
                if(localResult != null){
                    List<Data> localDatas = localResult.getLinkedData();
                    for(Data d : localDatas)
                        if(d instanceof DataString)
                            localResults.add((DataString) d);
                }
                redrawOutput();
            } catch (ModelException e) {
                logger.error(e.getMessage(), e);
            }
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
                List<Data> linkDatas =    search.getLinkedData();
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
