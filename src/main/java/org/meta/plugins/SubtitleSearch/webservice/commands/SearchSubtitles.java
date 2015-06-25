package org.meta.plugins.SubtitleSearch.webservice.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.meta.model.Data;
import org.meta.model.DataFile;
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

public class SearchSubtitles extends AbstractWebService{

    InterfaceDescriptor initialDescriptor     = null;
    TextOutput            initialTextOutput    = null;
    ModelFactory         factory             = null;
    public SearchSubtitles(){
        //initial descriptor, used to initiate the subtitle search
        TextInput path = new TextInput("path", "Path to the movie");
        rootColumn.addChild(path);
        initialTextOutput = new TextOutput("initialStateOutput", "callback :");
        rootColumn.addChild(initialTextOutput);
        //has a linked button on himself
        rootColumn.addChild(new SelfSubmitButton("submitToMe", "Search"));
        initialDescriptor = new InterfaceDescriptor(rootColumn);
        initialTextOutput.append("");
        try {
            //Second descriptor, used to show results
            factory = Model.getInstance().getFactory();
        } catch (ModelException ex) {
            //TODO remove once we properly initialize the model
        }
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        //Get file path
        String[] pathes = map.get("path");
        String      path   = pathes != null && pathes.length > 0 ? pathes[0] : "";

        //if path is not empty, try to search othrerwise just return the main
        //interface
        if(path != ""){
            File file = new File(path);
            if(file.exists()){
                initialTextOutput.flush();

                //instanciate a new MetaData st:<choosen language>
                TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
                properties.add(new MetaProperty("st", "fr"));
                MetaData metaData = factory.createMetaData(properties);

                //instanciate a new DataFile Object
                DataFile movie = factory.createDataFile(file);

                //create a new search with in input the DataFile and in output
                //the metaData
                Search subtitleSearch = factory.createSearch(movie, metaData, null);

                //lookup on the network to find the subtitles
                super.controler.search(    subtitleSearch.getHash(),
                                        "SubtitleSearch",
                                        "SearchSubtitleCommand",
                                        this);
            }else{
                initialTextOutput.flush();
                initialTextOutput.append("The file does not exist");
            }
        }else{
            initialTextOutput.flush();
            initialTextOutput.append("Please set a valide path name");
        }
    }

    @Override
    public void applySmallUpdate() {}

    @Override
    public void callback(ArrayList<Searchable> results) {
        //Those results are incomplete
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();
            if (searchable instanceof Search) {
                Search search = (Search) searchable;
                List<Data> linkDatas =    search.getLinkedData();
                for (Iterator<Data> k = linkDatas.iterator(); k .hasNext();) {
                    Data data = (Data) k.next();
                    //TODO what to do with the output ?
                    //I think, fill the interface with links, should be good
                    //an idea is to build a request URL to the next step
                    //with those links
                    //data.getHashCode();
                }
            }
        }
    }
}
