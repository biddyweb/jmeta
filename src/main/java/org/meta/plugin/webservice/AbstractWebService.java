package org.meta.plugin.webservice;

import java.util.Map;

import org.meta.plugin.webservice.forms.InterfaceDescriptor;

//TODO transform as an interface
public abstract class AbstractWebService {

	public abstract InterfaceDescriptor getInterface();

	public abstract String execute(Map<String, String[]> map);

}
