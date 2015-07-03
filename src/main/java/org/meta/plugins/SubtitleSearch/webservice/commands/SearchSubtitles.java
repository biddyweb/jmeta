package org.meta.plugins.SubtitleSearch.webservice.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.MetaData;
import org.meta.model.MetaProperty;
import org.meta.model.ModelFactory;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.fields.TextInput;
import org.meta.plugin.webservice.forms.fields.TextOutput;
import org.meta.plugin.webservice.forms.fields.radio.RadioButton;
import org.meta.plugin.webservice.forms.fields.radio.RadioList;
import org.meta.plugin.webservice.forms.submit.SelfSubmitButton;
import org.meta.plugin.webservice.forms.submit.SubmitToButton;

public class SearchSubtitles extends AbstractWebService{

    TextOutput       initialTextOutput   = null;
    ModelFactory     factory             = null;
    TextInput        path                = null;
    ArrayList<Data>  results             = null;
    SelfSubmitButton submitToMe          = null;
    SubmitToButton   getSubtitleButton   = null;
    RadioList        resultsOutput       = null;
    String           failure             = null;
    
    public SearchSubtitles(AbstractPluginWebServiceControler controler){
        super(controler);

        //Path to the movie
        path = new TextInput("path", "Path to the movie");
        path.setDescription("Point to the file on your hardrive");
        rootColumn.addChild(path);

        submitToMe = new SelfSubmitButton("submitToMe", "Search");
        //had a linked button on himself
        rootColumn.addChild(submitToMe);

        //tex output
        initialTextOutput = new TextOutput("initialStateOutput", "callback :");
        rootColumn.addChild(initialTextOutput);
        factory = this.controler.getModel().getFactory();
        
        results = new ArrayList<Data>();
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        //initiate state
        initialTextOutput.flush();
        rootColumn.removeChild(resultsOutput);
        resultsOutput       = null;
        rootColumn.removeChild(getSubtitleButton);
        getSubtitleButton   = null;
        //Get file path
        String path = getParameter(this.path.getId(), map);

        //set retrieving path as new defaults value of path field
        this.path.setValue(path);

        //if path is not empty, try to search othrerwise just return the main
        //interface
        if(path != ""){
            //Only go further if the file exist
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
                super.controler.search(  subtitleSearch.getHash(),
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
        //Change the label of the selfSubmit to me to "make a new search"
        submitToMe.setLabel("Make a new search");
    }

    @Override
    public void applySmallUpdate() {}

    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        for (Iterator<Searchable> i = results.iterator(); i.hasNext();) {
            Searchable searchable = i.next();
            if (searchable instanceof Search) {
                Search search = (Search) searchable;
                /*
                 * SearchSubtitle TCP command as send the linked datas as
                 * onlyText but we still have acces to the description
                 */
                if(search.getLinkedData() != null){
                    this.results.addAll(search.getLinkedData());
                }
            }
        }
        redrawOutPut();
    }
    
    @Override
    public void callbackFailure(String failureMessage) {
        failure = failureMessage;
    }

    private void redrawOutPut() {
        /*
         * At first successful callback, initialize the resultOutput radioList
         * and add the button to submit to the getCommand
         */
        if(resultsOutput == null){
            resultsOutput = new RadioList("subtitleHash", "Pick a subtitle");
            rootColumn.addChild(resultsOutput);
        }
        if(getSubtitleButton == null){
            getSubtitleButton = new SubmitToButton(
                                        "send", 
                                        "Download selected subtitle", 
                                        "getSubtitles");
            rootColumn.addChild(getSubtitleButton);
            
        }
        //Feed the output with the results
        ArrayList<RadioButton> buttons = new ArrayList<RadioButton>();
        for(Data data : results){
            String description = extractDescription(data.getDescription());
            buttons.add(new RadioButton(data.getHash().toString(), description));
        }
        resultsOutput.setButtons(buttons);
    }

    private String extractDescription(ArrayList<MetaProperty> properties) {
        String description = ""; 
        for(MetaProperty property : properties){
            if(property.getName().equals("description"))
                description = description+property.getValue()+";";
        }
        return description;
    }

}
