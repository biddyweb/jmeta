package org.meta.plugins.SubtitleSearch.webservice.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

import org.meta.common.MetHash;
import org.meta.dht.DHTOperation;
import org.meta.dht.MetaDHT;
import org.meta.dht.OperationListener;
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
import org.meta.plugin.webservice.forms.submit.SelfSubmitButton;

public class GetSubtitles extends AbstractWebService{

    private TextInput           subtitleHash        = null;
    private SelfSubmitButton    submitToMe          = null;
    private TextOutput          errorTextOutput     = null;
    private TextOutput          successTextOutput   = null;
    private ModelFactory        factory             = null;
    private Search              subtitleSearch      = null;
    private String failure;

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
                if(subtitleSearch != null){
                    subtitleSearch.getLinkedData().add(subtitle);
                    successTextOutput.append("serving now "+
                                             subtitle.getFile().getName()
                                             +" on DHT");
                    //Save the search in DB
                    super.controler.getModel().set(subtitleSearch);

                    /*
                     * Now, avert everyone that you've got a search with answers
                     */
                    MetaDHT.getInstance().store(subtitleSearch.
                            getHash()).addListener(
                            new OperationListener<DHTOperation>() {

                        @Override
                        public void failed(DHTOperation operation) {
                            errorTextOutput.append("fail to push search"+
                                            operation.getFailureMessage());
                        }

                        @Override
                        public void complete(DHTOperation operation) {
                            successTextOutput.append("succes to push search");
                        }
                    });
                }else{
                    /*
                     * If there was just an only shot request, just store
                     * the received data
                     */
                    super.controler.getModel().set(subtitle);
                }
                /*
                 * In all the cases, tell everyone that you have a DataFile to
                 * give.
                 */
                MetaDHT.getInstance().store(subtitle. getHash()).addListener(
                        new OperationListener<DHTOperation>() {

                    @Override
                    public void failed(DHTOperation operation) {
                        errorTextOutput.append("fail to push subtitle"+
                                        operation.getFailureMessage());
                    }

                    @Override
                    public void complete(DHTOperation operation) {
                        successTextOutput.append("succes to push subtitle");
                    }
                });
            }
        }
    }

    @Override
    public void callbackFailure(String failureMessage) {
        errorTextOutput.append(failure);
    }
}
