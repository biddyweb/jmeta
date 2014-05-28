package org.meta.plugin.webservice.forms.fields;

import org.meta.plugin.webservice.forms.InterfaceField;


/**
 * 
 * @author faquin
 *
 */
public class TextOutput extends InterfaceField {

	public TextOutput(String id, String label) {
		super(id, label);
	}

	@Override
	protected String getType() {
		return "TextOutput";
	}

}
