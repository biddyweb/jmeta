package org.meta.modele;


import java.util.HashMap;

import djondb.BSONObj;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 * @author Thomas LAVOCAT
 *	
 * This class correspond to a data on the hard drive. Pointed by a file.
 */
public class DataString extends Data {

	private String string = null;
	
	/**
	 * needed for java Reflexion
	 */
	public DataString(){
		super();
	}
	
	/**
	 * Instantiate a new Data -> use in case of creation
	 * @param hashCode
	 * @param file
	 */
	public DataString(String hashCode, String string) {
		super(hashCode);
		this.string = string;
	}

	/**
	 * Instantiate a (not) new Data -> use in case of data base loading
	 * @param hashCode
	 * @param file
	 */
	public DataString(String hashCode, String string, BSONObj json) {
		super(hashCode, json);
		this.string = string;
	}
	
	/**
	 * @return the file
	 */
	public String getFile() {
		return string;
	}

	/**
	 * @param file the file to set
	 */
	public void setString(String string) {
		this.string = string;
	}

	public BSONObj toJson() {
		BSONObj json = super.toJson();
		json.add("string", string);
		return json;
	}

	@Override
	protected void fillFragment(HashMap<String, byte[]> fragment) {
		//write the file name
		//TODO
	}

}
