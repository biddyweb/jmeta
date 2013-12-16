package com.meta.modele;

import java.io.File;

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
 */
public class Data extends Searchable {

	private File file = null;
	
	/**
	 * needed for java Reflexion
	 */
	public Data(){
		super();
	}
	
	/**
	 * Instantiate a new Data -> use in case of creation
	 * @param hashCode
	 * @param file
	 */
	public Data(String hashCode, File file) {
		super(hashCode);
		this.file = file;
	}

	/**
	 * Instantiate a (not) new Data -> use in case of data base loading
	 * @param hashCode
	 * @param file
	 */
	public Data(String hashCode, File file, BSONObj json) {
		super(hashCode, json);
		this.file = file;
	}
	
	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	public BSONObj toJson() {
		BSONObj json = super.toJson();
		json.add("file", file.getAbsolutePath());
		return json;
	}

}
