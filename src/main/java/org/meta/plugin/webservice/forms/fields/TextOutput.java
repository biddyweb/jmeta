package org.meta.plugin.webservice.forms.fields;

import org.meta.plugin.webservice.forms.InterfaceElement;


/**
 * 
 * @author faquin
 *
 */
public class TextOutput extends InterfaceElement {

	public TextOutput(String id) {
		super(id);
	}

	@Override
	protected String getType() {
		return "TextOutput";
	}

}
