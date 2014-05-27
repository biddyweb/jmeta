package org.meta.plugin.webservice.forms.organizers;

import org.meta.plugin.webservice.forms.InterfaceOrganizer;

/**
 * 
 * @author faquin
 *
 */
public class ColumnOrganizer extends InterfaceOrganizer {

	public ColumnOrganizer(String id) {
		super(id);
	}

	@Override
	protected String getType() {
		return "Column";
	}

}
