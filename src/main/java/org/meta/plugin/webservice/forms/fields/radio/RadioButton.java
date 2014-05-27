package org.meta.plugin.webservice.forms.fields.radio;

import org.meta.plugin.webservice.forms.InterfaceElement;

public class RadioButton extends InterfaceElement{

	public RadioButton(String id) {
		super(id);
	}

	@Override
	protected String getType() {
		return "radioButton";
	}

}
