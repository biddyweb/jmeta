package org.meta.plugin.webservice.forms;

import org.bson.BasicBSONObject;

/**
 * 
 * @author faquin
 *
 */
public abstract class InterfaceElement {
	
	private String 	id = null;
	
	public InterfaceElement(String id){
		this.id = id;
	}
	
	public BasicBSONObject toJson() {
		BasicBSONObject bson = new BasicBSONObject("type", getType());
		bson.append("id", id);
		return bson;
		
	}

	protected abstract String getType();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
