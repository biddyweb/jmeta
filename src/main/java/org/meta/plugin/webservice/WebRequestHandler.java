package org.meta.plugin.webservice;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.BasicBSONObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.meta.plugin.AbstractPluginWebServiceControler;


public class WebRequestHandler extends AbstractHandler {

	private SingletonWebServiceReader 			webServiceReader 	= null;
	private HashMap<String, AbstractWebService> instanceMap 		= null;
	private int nbCommands = 0;

	public WebRequestHandler() {
		webServiceReader 	= SingletonWebServiceReader.getInstance();
		instanceMap 		= new HashMap<String, AbstractWebService>();
	}
	
	@Override
	public void handle(	String target, 
						Request base, 
						HttpServletRequest request,
						HttpServletResponse response) 
								throws 	IOException, 
										ServletException {
		//Split the incomming url on every /
		String[] 	urlParse 	= target.split("/");
		String 		action		= "";
		String 		plugin		= "";
		String		command		= "";
		
		if(urlParse.length == 4){
			//if theres 3 it means we've got two parameters, an action and
			//a command
			action 		= urlParse[urlParse.length-3];
			plugin		= urlParse[urlParse.length-2];
			command 	= urlParse[urlParse.length-1];
		}else if (urlParse.length == 3){
			action 		= urlParse[urlParse.length-2];
			plugin		= urlParse[urlParse.length-1];
		}else{
			// otherwise just one System action 
			action 	= urlParse[urlParse.length-1];
		}

		response.setContentType("application/json");
		
		
		AbstractPluginWebServiceControler pluginInstance = null;
		
		if(plugin != ""){
			pluginInstance = webServiceReader.getPlugin(plugin);
		}
		
		//Tree cases :
		// we have a command and a plugin
		// we only have a plugin
		// we have nothing
		
		if(command != "" && pluginInstance != null){
			//First case, get the associated command
			
			Class<? extends AbstractWebService> clazzWs = 
										pluginInstance.getCommand(command);
			//if clazzWs is not null it means it was found and executable
			if(clazzWs != null){
				try {
					
					String idCommand = request.getParameter("idCommand");
					AbstractWebService commandWs = null;
					
					if(idCommand != null && idCommand != ""){
						commandWs = instanceMap.get(idCommand);
					}
					if(commandWs == null){
						idCommand = getNewId();
						commandWs = (AbstractWebService) clazzWs.newInstance();
						instanceMap.put(idCommand, commandWs);
					}
					commandWs.setWebServiceControler(pluginInstance);
					
					BasicBSONObject result = null;
					
					switch (action) {
					//fflush memory
					case "terminate" :
						commandWs =instanceMap.get(idCommand);
						if(commandWs != null)
							commandWs.kill();
						instanceMap.remove(idCommand);
						break;
					
					//getNextResults from network
					case "retrieveUpdate" :
						result = commandWs.retrieveUpdate().toJson();
						break ;
					
					//execute command
					case "execute":
						result = commandWs.execute(request.getParameterMap())
						.toJson();
						break;
					
					//get the next step in the workflow
					case "nextStep":
						result = commandWs.getNextStep();
						break;
					
					//default case : get the interface descriptor	
					case "interface":
					default:
						result = commandWs.getInterface(request.getParameterMap()).toJson();
						break;
					}
					
					if(result != null){
						result.append("idCommand", idCommand);
						response.getWriter().print(result.toString());
					}
			        response.setStatus(HttpServletResponse.SC_OK);
			        base.setHandled(true);
				} catch (Exception e) {
					response.getWriter().write(e.getMessage());
			        response.setStatus(HttpServletResponse
			        								.SC_INTERNAL_SERVER_ERROR);
			        base.setHandled(true);
				}
			}else{
		        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        base.setHandled(true);
			}
		} else if(plugin != "" && pluginInstance != null){
			//second case, we only have a plugin
			switch (action){
			case "getCommandList":
			default:
				response.getWriter().print(pluginInstance.getJsonCommandList());
			break;
			}	
			
	        response.setStatus(HttpServletResponse.SC_OK);
	        base.setHandled(true);
		}else{
			//last case
			 switch (action) {
			case "getPluginsList":
			default:
				response.getWriter().print(webServiceReader
						.getPluginListAsJson());
				break;
			}
			 
	        response.setStatus(HttpServletResponse.SC_OK);
	        base.setHandled(true);
		}
	}

	private String getNewId() {
		nbCommands++;
		return "command"+nbCommands;
	}

}
