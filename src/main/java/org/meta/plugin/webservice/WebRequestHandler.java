package org.meta.plugin.webservice;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.webservice.commands.InterfaceDescriptor;

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
		String		action		= urlParse[0];
		String 		command 	= urlParse[1];
		
		Class<? extends AbstractWebService> clazzWs = webServiceReader.getCommand(command);
		if(clazzWs != null){
			try {
				AbstractWebService commandWs = 
									(AbstractWebService) clazzWs.newInstance();
				switch (action) {
				case "execute":
					response.setContentType("application/json");
					response.getWriter().print(commandWs.execute());
					break;

				case "interface":
				default:
					InterfaceDescriptor interfaceDesc = commandWs.getInterface();
					response.setContentType("application/json");
					response.getWriter().print(interfaceDesc.toJson());
					break;
				}
				
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

}
