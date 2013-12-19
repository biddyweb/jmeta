package com.meta.plugin.TCP;

import java.io.Serializable;
import java.util.ArrayList;

public class SerializableCommandParameters implements Serializable {
	
	private String name;
	private ArrayList<String> parameters;
	
	public SerializableCommandParameters(){}
	
	
	/**
	 * 
	 * @param name
	 * @param parameters
	 */
	public SerializableCommandParameters(String name, ArrayList<String> parameters) {
		super();
		this.name = name;
		this.parameters = parameters;
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parameters
	 */
	public ArrayList<String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(ArrayList<String> parameters) {
		this.parameters = parameters;
	}
	
	
}
