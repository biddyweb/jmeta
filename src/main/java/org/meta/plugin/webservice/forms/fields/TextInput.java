package org.meta.plugin.webservice.forms.fields;

import org.meta.plugin.webservice.forms.InterfaceElement;


/**
 * 
 * @author faquin
 *
 */
public class TextInput extends InterfaceElement {

	public TextInput(String id) {
		super(id);
	}

	@Override
	protected String getType() {
		return "TextInput";
	}

}
