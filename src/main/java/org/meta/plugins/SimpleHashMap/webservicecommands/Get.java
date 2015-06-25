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

public class Get extends AbstractWebService{

    InterfaceDescriptor  initialDescriptor   = null;
    TextOutput           output              = null;
    ModelFactory         factory             = null;
    ArrayList<DataString>results             = null;
    
    public Get(){
        results = new ArrayList<DataString>();
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
        String id = getParameter("id", map);
        
        if(id != ""){
            output.flush();
            TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
            properties.add(new MetaProperty("hashmap", "value"));
            
            MetaData    metaData     = factory.createMetaData(null, properties);
            DataString  source       = factory.createDataString("id");
            Search      contentSearch= factory.createSearch(source, metaData);

//            super.controler.search(  contentSearch.getHash(),
//                                    "SimpleHashMap",
//                                    "getCommand",
//                                    this);
            
            try {
                Search localResult =
                        (Search) Model.getInstance().get(contentSearch.getHash());
                List<Data> localDatas = localResult.getResult().getLinkedData();
                for(Data d : localDatas)
                    if(d instanceof DataString)
                        results.add((DataString) d);
                redrawOutput();
            } catch (ModelException e) {
                e.printStackTrace();
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
    public void callback(ArrayList<Searchable> results) {
        output.flush();
        //Those results are incomplete
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();
            if (searchable instanceof Search) {
                Search   search   = (Search) searchable;
                MetaData metaData = search.getResult();
                List<Data> linkDatas =    metaData.getLinkedData();
                for (Iterator<Data> k = linkDatas.iterator(); k .hasNext();) {
                    Data data = (Data) k.next();
                    if(data instanceof DataString)
                        this.results.add((DataString) data);
                }
            }
        }
        redrawOutput();
    }

    private void redrawOutput() {
        output.flush();
        for(DataString result : this.results){
            output.append(result.getString());
        }
        output.append("waiting for results");
    }
}
