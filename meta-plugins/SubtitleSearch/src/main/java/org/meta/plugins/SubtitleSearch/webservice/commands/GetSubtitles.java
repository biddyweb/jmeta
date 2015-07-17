package org.meta.plugins.SubtitleSearch.webservice.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaProperty;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.api.ws.AbstractWebService;
import org.meta.api.ws.forms.fields.TextInput;
import org.meta.api.ws.forms.fields.TextOutput;
import org.meta.api.ws.forms.submit.SelfSubmitButton;

/**
 *
 * @author nico
 */
public class GetSubtitles extends AbstractWebService{

    private TextInput           subtitleHash        = null;
    private SelfSubmitButton    submitToMe          = null;
    private TextOutput          errorTextOutput     = null;
    private TextOutput          successTextOutput   = null;
    private ModelFactory        factory             = null;
    private Search              subtitleSearch      = null;
    private String failure;

    /**
     *
     * @param controler
     */
    public GetSubtitles(AbstractPluginWebServiceControler controler) {
        super(controler);
        factory = controler.getModel().getFactory();
        
        //Path to the movie
        subtitleHash = new TextInput("subtitleHash", "Hash of the subtitle");
        subtitleHash.setDescription("If you don't know what to do here, "
                                    + "make a full search from searchSubtitle");
        rootColumn.addChild(subtitleHash);
        
        submitToMe = new SelfSubmitButton("submitToMe", "Search");
        //had a linked button on himself
        rootColumn.addChild(submitToMe);

        //tex output
        errorTextOutput = new TextOutput("errorOutput", "Errors");
        rootColumn.addChild(errorTextOutput);
        

        //tex output
        successTextOutput = new TextOutput("successOutput", "Success");
        rootColumn.addChild(successTextOutput);
    }

    @Override
    protected void executeCommand(Map<String, String[]> map) {
        errorTextOutput.flush();
        //Get path to the movie
        String pathToTheMovie = getParameter("path", map);
        
        /*
         * if we are in the user workflow and we have a path to the movie, so 
         * we can rebuild the search to store the future result in a cleany way 
         */
        if(pathToTheMovie != null){
            //instanciate a new MetaData st:<choosen language>
            TreeSet<MetaProperty> properties = new TreeSet<MetaProperty>();
            properties.add(new MetaProperty("st", "fr"));
            MetaData metaData = factory.createMetaData(properties);
    
            //instanciate a new DataFile Object
            DataFile movie = factory.createDataFile(new File(pathToTheMovie));

            //create a new search with in input the DataFile and in output
            //the metaData
            subtitleSearch = factory.createSearch(movie, metaData, null);
            //if a DB element exist, prefer it
            Search dbSearch = super.controler.getModel().getSearch(subtitleSearch.getHash());
            if(dbSearch != null)
                subtitleSearch = dbSearch;
        }

        
        
        String subtitleHash     = getParameter(this.subtitleHash.getId(), map);
        this.subtitleHash.setValue(subtitleHash);
        if(subtitleHash != null && !subtitleHash.equals("")){
            MetHash hash = new MetHash(subtitleHash);
            
            super.controler.search( hash,
                                    "SubtitleSearch",
                                    "GetSubtitleCommand",
                                    this);
            
        }else{
            errorTextOutput.flush();
            errorTextOutput.append("Please set a valide path name");
        }
     
    }

    @Override
    public void applySmallUpdate() {}


    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        /*
         * On success, if there is results, 
         */
        if(results.size() > 0){
            if(results.get(0) instanceof DataFile){
                //Get the first as subtitle
                DataFile subtitle = (DataFile) results.get(0);
                successTextOutput.append(   "New subtitle "+
                                            subtitle.getFile().getName()+
                                            " download to "+
                                            subtitle.getFile().getPath());
                /*
                 * If subtitle search !=null, it mean that we where able to 
                 * rebuild it, and we are in the search subtitle workflow.
                 * We need to change the search value in DB
                 * In this case, it means adding a new subtitle in the dataBase
                 */
                subtitle = (DataFile) super.updateResult(subtitle);
                if(subtitleSearch != null){
                    subtitleSearch = super.updateSearch(subtitleSearch, subtitle);
                    super.saveAndPush(subtitleSearch);
                }else{
                    super.onlySave(subtitle);
                }
                super.onlyPush(subtitle);
            }
        }
    }

    @Override
    public void callbackFailure(String failureMessage) {
        errorTextOutput.append(failure);
    }

    /**
     *
     * @param operation
     * @param s
     */
    @Override
    protected void callbackFailedToPush(AsyncOperation operation, Searchable s) {
        errorTextOutput.append("Fail to push "+s.getHash()+" "+operation.getFailureMessage());
    }

    /**
     *
     * @param operation
     * @param s
     */
    @Override
    protected void callbackSuccessToPush(AsyncOperation operation, Searchable s) {
        successTextOutput.append("Success to push "+s.getHash());
    }
}
