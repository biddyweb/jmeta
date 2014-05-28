package org.meta.plugin.webservice.forms.fields;

import org.meta.plugin.webservice.forms.InterfaceField;


/**
 * 
 * @author faquin
 *
 */
public class TextInput extends InterfaceField {

	public TextInput(String id, String label) {
		super(id, label);
	}

	@Override
	protected String getType() {
		return "TextInput";
	}

}
