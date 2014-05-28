package org.meta.plugin.webservice.forms.fields.select;

import org.meta.plugin.webservice.forms.InterfaceField;

public class Select extends InterfaceField{

	public Select(String id, String label) {
		super(id, label);
	}

	@Override
	protected String getType() {
		return "select";
	}

}
