package org.lavocat.PluginExemple.webservice.commands;

import java.util.Map;

import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.fields.TextInput;
import org.meta.plugin.webservice.forms.organizers.ColumnOrganizer;

public class ExempleWsCommand extends AbstractWebService {

	@Override
	public InterfaceDescriptor getInterface() {
		ColumnOrganizer column = new ColumnOrganizer("column1");
		column.addChild(new TextInput("name"));
		InterfaceDescriptor descriptor = new InterfaceDescriptor(column);
		return descriptor;
	}

	@Override
	public String execute(Map<String, String[]> map) {
		return "you rules !";
	}
}
