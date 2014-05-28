package org.meta.plugin.webservice.forms.fields.radio;

import org.meta.plugin.webservice.forms.InterfaceField;

public class RadioButton extends InterfaceField{

	public RadioButton(String id, String label) {
		super(id, label);
	}

	@Override
	protected String getType() {
		return "radioButton";
	}

}
