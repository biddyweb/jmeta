package org.meta.plugin.webservice;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WebRequestHandler extends AbstractHandler {

	private SingletonWebServiceReader webServiceReader = null;

	public WebRequestHandler() {
		webServiceReader = SingletonWebServiceReader.getInstance();
	}
	
	@Override
	public void handle(String arg0, Request arg1, HttpServletRequest arg2,
			HttpServletResponse arg3) throws IOException, ServletException {

	}

}
