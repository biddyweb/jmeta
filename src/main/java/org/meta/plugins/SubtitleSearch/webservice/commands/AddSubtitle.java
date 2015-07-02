package org.meta.plugins.SubtitleSearch.webservice.commands;

import java.util.ArrayList;
import java.util.Map;

import org.meta.model.Searchable;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.webservice.AbstractWebService;

public class AddSubtitle extends AbstractWebService{

    public AddSubtitle(AbstractPluginWebServiceControler controler) {
        super(controler);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void executeCommand(Map<String, String[]> map) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void applySmallUpdate() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void callbackFailure(String failureMessage) {
        // TODO Auto-generated method stub
        
    }
}
