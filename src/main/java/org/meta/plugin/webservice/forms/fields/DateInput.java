package org.meta.plugin.webservice.forms.fields;

import org.meta.plugin.webservice.forms.InterfaceElement;


/**
 * 
 * @author faquin
 *
 */
public class DateInput extends InterfaceElement {

	public DateInput(String id) {
		super(id);
	}

	@Override
	protected String getType() {
		return "DateInput";
	}

}
