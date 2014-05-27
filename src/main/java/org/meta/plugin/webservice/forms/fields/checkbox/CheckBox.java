package org.meta.plugin.webservice.forms.fields.checkbox;

import org.meta.plugin.webservice.forms.InterfaceElement;

public class CheckBox extends InterfaceElement{

	public CheckBox(String id) {
		super(id);
	}

	@Override
	protected String getType() {
		return "radioButton";
	}

}
