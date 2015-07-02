package org.meta.plugins.SimpleHashMap.webservicecommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.meta.dht.DHTOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.OperationListener;
import org.meta.model.Data;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.ModelFactory;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.fields.TextInput;
import org.meta.plugin.webservice.forms.fields.TextOutput;
import org.meta.plugin.webservice.forms.submit.SelfSubmitButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Put extends AbstractWebService{

    InterfaceDescriptor  initialDescriptor   = null;
    TextOutput           output              = null;
    ModelFactory         factory             = null;
    ArrayList<DataString>results             = null;
    private Logger logger = LoggerFactory.getLogger(Put.class);
    
    public Put(AbstractPluginWebServiceControler controler){
        super(controler);
        results = new ArrayList<DataString>();
        TextInput path = new TextInput("id", "ID");
        rootColumn.addChild(path);
        TextInput content = new TextInput("content", "Content");
        rootColumn.addChild(content);
        output = new TextOutput("result", "Result");
        rootColumn.addChild(new SelfSubmitButton("submitToMe", "put"));
        rootColumn.addChild(output);
        initialDescriptor = new InterfaceDescriptor(rootColumn);
        factory = super.controler.getModel().getFactory();
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        String id      = getParameter("id", map);
        String content = getParameter("content", map);

        if(content != null && id != null){
            output.flush();
            DataString res = factory.createDataString(content);
            List<Data> lst = new ArrayList<Data>();
            lst.add(res);
            TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
            properties.add(new MetaProperty("hashmap", "value"));
            MetaData metaData = factory.createMetaData(properties);
            DataString source = factory.createDataString(id);
            Search hashM = factory.createSearch(source, metaData, lst);
            logger.info("put hash : "+hashM.getHash().toString());
            
            
            //write into dataBase
            //and store it to the DHT
            this.controler.getModel().set(hashM);
            MetaDHT.getInstance().store(hashM.getHash()).addListener(
                    new OperationListener<DHTOperation>() {

                @Override
                public void failed(DHTOperation operation) {
                    output.append("fail to push");
                }

                @Override
                public void complete(DHTOperation operation) {
                    output.append("succes to push");
                }
            
            });
        }else{
            output.flush();
            output.append("Please set an id");
        }
    }

    @Override
    public void applySmallUpdate() {
    }
    

    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        output.flush();
        //Those results are incomplete
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();
            if (searchable instanceof Search) {
                Search search = (Search) searchable;
                List<Data> linkDatas =    search.getLinkedData();
                for (Iterator<Data> k = linkDatas.iterator(); k .hasNext();) {
                    Data data = (Data) k.next();
                    if(data instanceof DataString)
                        this.results.add((DataString) data);
                }
            }
        }
        for(DataString result : this.results){
            output.append(result.getString());
        }
        output.append("waiting for results");
    }

    @Override
    public void callbackFailure(String failureMessage) {
        // TODO Auto-generated method stub
        
    }
}
