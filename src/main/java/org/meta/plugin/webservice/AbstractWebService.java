package org.meta.plugin.webservice;

import org.meta.plugin.webservice.commands.InterfaceDescriptor;

public abstract class AbstractWebService {

	public abstract InterfaceDescriptor getInterface();

	public abstract String execute();

}
