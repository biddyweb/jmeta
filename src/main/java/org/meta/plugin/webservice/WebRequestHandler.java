package org.meta.plugin.webservice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.BasicBSONObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;

public class WebRequestHandler extends AbstractHandler {

	private SingletonWebServiceReader webServiceReader = null;

	public WebRequestHandler() {
		webServiceReader = SingletonWebServiceReader.getInstance();
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
		String		command		= "";
		
		if(urlParse.length == 3){
			//if theres 3 it means we've got two parameters, an action and
			//a command
			action		= urlParse[urlParse.length-2];
			command 	= urlParse[urlParse.length-1];
		}else{
			//just one action
			action 	= urlParse[urlParse.length-1];
		}

		response.setContentType("application/json");
		if(command != ""){
			//Get the associated command
			Class<? extends AbstractWebService> clazzWs = 
										webServiceReader.getCommand(command);
			if(clazzWs != null){
				try {
					//
					AbstractWebService commandWs = 
									(AbstractWebService) clazzWs.newInstance();
					switch (action) {
					case "execute":
						response.getWriter().print(
								commandWs.execute(request.getParameterMap()));
						break;
	
					case "interface":
					default:
						InterfaceDescriptor interfaceDesc = commandWs.getInterface();
						BasicBSONObject json = interfaceDesc.toJson();
						
						response.getWriter().print(json.toString());
						break;
					}
	
			        response.setStatus(HttpServletResponse.SC_OK);
			        base.setHandled(true);
				} catch (Exception e) {
					response.getWriter().write(e.getMessage());
			        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			        base.setHandled(true);
				}
			}else{
		        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        base.setHandled(true);
			}
		}else{
			 switch (action) {
			case "getPluginList":
			default:
				response.getWriter().print(webServiceReader.getCommandListAsJson());
				break;
			}
			 
	        response.setStatus(HttpServletResponse.SC_OK);
	        base.setHandled(true);
		}
	}

}
