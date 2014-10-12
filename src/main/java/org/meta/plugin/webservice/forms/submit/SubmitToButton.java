package org.meta.plugin.webservice.forms.submit;

import org.bson.BasicBSONObject;
import org.meta.plugin.webservice.forms.InterfaceButton;

public class SubmitToButton extends InterfaceButton{
	
	private String destination 	= null;
	
	public SubmitToButton(String id, String label, String destination) {
		super(id, label);
		this.destination = destination;		
	}

	@Override
	protected String getType() {
		return "submitToButton";
	}
	
	public BasicBSONObject toJson() {
		BasicBSONObject o = super.toJson();
		o.append("destination", destination);
		return o;
	}
}
