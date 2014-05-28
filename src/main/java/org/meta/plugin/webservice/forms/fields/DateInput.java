package org.meta.plugin.webservice.forms.fields;

import org.meta.plugin.webservice.forms.InterfaceField;


/**
 * 
 * @author faquin
 *
 */
public class DateInput extends InterfaceField {

	public DateInput(String id, String label) {
		super(id, label);
	}
	
	@Override
	protected String getType() {
		return "DateInput";
	}

}
