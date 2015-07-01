package org.meta.plugins.SimpleChat.webservice.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.meta.dht.DHTOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.OperationListener;
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

public class Chat extends AbstractWebService{

    InterfaceDescriptor  initialDescriptor   = null;
    TextOutput           output              = null;
    ModelFactory         factory             = null;
    MetaData             chat                = null;
	private Search       retrieveMessage     = null;
	private TextInput path;
	private TextInput nick;
	private TreeMap<Date, String> results    = null;
	private SimpleDateFormat      sdf        = null;
    
    public Chat(){
        results = new TreeMap<Date, String>();
        sdf = new SimpleDateFormat("dd /MM / yyyy HH : mm : ss");
        
        path = new TextInput("channel", "Channel room");
        rootColumn.addChild(path);
        
        nick = new TextInput("nickName", "Nick name");
        rootColumn.addChild(nick);

        output = new TextOutput("chat", "Chat");
        rootColumn.addChild(output);
        
        TextInput content = new TextInput("message", "Message");
        rootColumn.addChild(content);

        rootColumn.addChild(new SelfSubmitButton("send", "Send"));
        
        try {
            factory = Model.getInstance().getFactory();
        } catch (ModelException ex) {
        }
        TreeSet<MetaProperty> props = new TreeSet<MetaProperty>();
        props.add(new MetaProperty("chat", "channel"));
        chat = factory.createMetaData(props);
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        String channel  = getParameter(path.getId(), map);
        String message  = getParameter("message", map);
        String nickName = getParameter(nick.getId(), map);
        
        path.setValue(channel);
        nick.setValue(nickName);

        if(channel != null && !channel.equals("")){
            DataString channelName = factory.createDataString(channel);
            retrieveMessage = factory.createSearch(channelName, chat, null); 
            if(message != null || !message.equals("")){
                Date timeStamp = new Date();
                Data messageToSave = factory.createDataString(""+nickName+";"+timeStamp.getTime()+";"+message);
                results.put(timeStamp, nickName +" : "+ message);
                Search searchToSave = factory.createSearch(channelName, chat, Collections.singletonList(messageToSave));
                //write into dataBase
                //and store it to the DHT
                try {
                    Model.getInstance().set(searchToSave);
                    MetaDHT.getInstance().store(searchToSave.getHash()).addListener(
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
                } catch (ModelException e) {
                    e.printStackTrace();
                }
            }
            super.controler.search(retrieveMessage.getHash(),
                    "SimpleChat",
                    "getLastMessages",
                    this);
            
        }
        redrawOutput();
    }

    @Override
    public void applySmallUpdate() {
        super.controler.search(retrieveMessage.getHash(),
                "SimpleChat",
                "getLastMessages",
                this);
    }
    

    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        //Those results are incomplete
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();
            
            if (searchable instanceof Search) {
                Search search = (Search) searchable;
                List<Data> linkDatas =    search.getLinkedData();
                
                for (Iterator<Data> k = linkDatas.iterator(); k .hasNext();) {
                    Data data = (Data) k.next();
                    if(data instanceof DataString){
                        DataString distantMessage = (DataString) data;
                        String baseMessage = distantMessage.getString();
                        String[] split = baseMessage.split(";");
                        String pseudo = split[0];
                        Date   sDate  = new Date(Long.parseLong(split[1]));
                        String msg    = split[2];
                        this.results.put(sDate, pseudo +" : "+msg);
                    }
                    
                }
            }
        }
        redrawOutput();
    }
    
    @Override
    public void callbackFailure(String failureMessage) {
        // TODO Auto-generated method stub
        
    }

    private void redrawOutput(){
        output.flush();
        for(Entry<Date, String> entry : this.results.entrySet()){
            output.append(sdf.format(entry.getKey())+ " "+ entry.getValue());	
        }
    }
}
