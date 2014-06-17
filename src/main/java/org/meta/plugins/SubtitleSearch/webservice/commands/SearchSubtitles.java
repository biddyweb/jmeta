package org.meta.plugins.SubtitleSearch.webservice.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.bson.BasicBSONObject;
import org.meta.model.Searchable;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.fields.TextInput;
import org.meta.plugin.webservice.forms.fields.TextOutput;
import org.meta.plugin.webservice.forms.organizers.ColumnOrganizer;
import org.meta.plugin.webservice.forms.submit.SelfSubmitButton;
import org.meta.plugins.SubtitleSearch.PluginSubtitleSearchWebServiceControler;

public class SearchSubtitles extends AbstractWebService{
	
	InterfaceDescriptor initialDescriptor 	= null;
	TextOutput			initialTextOutput	= null;
	
	public SearchSubtitles(){
		//initial descriptor, used to initiate the subtitle search
		ColumnOrganizer column = new ColumnOrganizer("center");
		TextInput path = new TextInput("path", "Path to the movie");
		column.addChild(path);
		initialTextOutput = new TextOutput("initialStateOutput", "callback :");
		column.addChild(initialTextOutput);
		//has a linked button on himself
		column.addChild(new SelfSubmitButton("submitToMe", "Search"));
		initialDescriptor = new InterfaceDescriptor(column);
		//Second descriptor, used to show results
	}
	
	@Override
	public InterfaceDescriptor getInterface(Map<String, String[]> map) {
		return initialDescriptor;
	}

	@Override
	public InterfaceDescriptor execute(Map<String, String[]> map) {
		InterfaceDescriptor response = initialDescriptor;
		//Get file path
		String[] pathes = map.get("path");
		String 	 path   = pathes != null && pathes.length > 0 ? pathes[0] : "";
		
		//if path is not empty, try to search othrerwise just return the main
		//interface
		if(path != ""){
			File file = new File(path);
			if(file.exists()){
				initialTextOutput.flush();
				PluginSubtitleSearchWebServiceControler controler = 
					(PluginSubtitleSearchWebServiceControler) super.controler;
				String hash = controler.getModel().hash(file);
				super.controler.search(hash, "CommandName//TODO", this);//TODO
			}else{
				initialTextOutput.flush();
				initialTextOutput.append("The file does not exist");
			}
		}else{
			initialTextOutput.flush();
			initialTextOutput.append("Please set a valide path name");
		}
		return response;
	}

	@Override
	public InterfaceDescriptor retrieveUpdate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void callback(ArrayList<Searchable> results) {
		// TODO Auto-generated method stub
	}

	@Override
	public BasicBSONObject getNextStep() {
		BasicBSONObject nextStep = new BasicBSONObject();
		nextStep.append("pluginName", "SubtitleSearch");
		nextStep.append("commandName", "getSubtitles");
		//TODO give parameters
		return nextStep;
	}

}
