package org.meta.plugin.webservice.forms.fields.checkbox;

import org.meta.plugin.webservice.forms.InterfaceField;

public class CheckBox extends InterfaceField{

	public CheckBox(String id, String label) {
		super(id, label);
	}

	@Override
	protected String getType() {
		return "checkBox";
	}

}
