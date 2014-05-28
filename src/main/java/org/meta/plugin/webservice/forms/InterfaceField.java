package org.meta.plugin.webservice.forms;

import org.bson.BasicBSONObject;


public abstract class InterfaceField extends InterfaceElement {

	protected String label = "";
	
	public InterfaceField(String id, String label) {
		super(id);
		this.label = label;
	}
	
	public BasicBSONObject toJson() {
		BasicBSONObject o = super.toJson();
		o.append("label", label);
		return o;
	}
	
}
