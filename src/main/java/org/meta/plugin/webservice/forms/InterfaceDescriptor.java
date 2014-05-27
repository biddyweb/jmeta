package org.meta.plugin.webservice.forms;

import org.bson.BasicBSONObject;

/**
 * Describe a simple interface to discuss with the web service
 * @author faquin
 */
public class InterfaceDescriptor {
	
	private InterfaceOrganizer root = null;
	
	public InterfaceDescriptor(){}
	
	/**
	 * 
	 * @param root the interface root
	 */
	public InterfaceDescriptor(InterfaceOrganizer root){
		this.root = root;
	}
	
	public BasicBSONObject toJson() {
		return root.toJson();
	}
	
	/**
	 * Set the root of the interface
	 * @param root the InterfaceOrganizer to the root Interface
	 */
	public void setRoot(InterfaceOrganizer root) {
		this.root = root;
	}

}
