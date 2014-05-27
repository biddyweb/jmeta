package org.meta.plugin.webservice.forms.fields.select;

import org.meta.plugin.webservice.forms.InterfaceElement;

public class Select extends InterfaceElement{

	public Select(String id) {
		super(id);
	}

	@Override
	protected String getType() {
		return "radioButton";
	}

}
