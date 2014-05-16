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
		String[] 	urlParse 	= target.split("/");
		String		action		= urlParse[urlParse.length-2];
		String 		command 	= urlParse[urlParse.length-1];
		
		Class<? extends AbstractWebService> clazzWs = webServiceReader.getCommand(command);
		if(clazzWs != null){
			try {
				AbstractWebService commandWs = 
									(AbstractWebService) clazzWs.newInstance();
				switch (action) {
				case "execute":
					response.setContentType("application/json");
					response.getWriter().print(commandWs.execute(request.getParameterMap()));
					break;

				case "interface":
				default:
					InterfaceDescriptor interfaceDesc = commandWs.getInterface();
					response.setContentType("application/json");
					BasicBSONObject json = interfaceDesc.toJson();
					
					response.getWriter().print(json.toString());
					break;
				}

		        response.setStatus(HttpServletResponse.SC_OK);
		        base.setHandled(true);
			} catch (Exception e) {
				response.getWriter().write(e.getMessage());
		        response.setStatus(HttpServletResponse.SC_OK);
		        base.setHandled(true);
			}
		}
	}

}
