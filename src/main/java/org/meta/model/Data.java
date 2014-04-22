package org.meta.modele;

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
public abstract class Data extends Searchable {

	
	
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
	public Data(String hashCode) {
		super(hashCode);
	}

	public Data(String hashCode, BSONObj json){
		super(hashCode, json);
	}
	
	public BSONObj toJson(){
		return super.toJson();
	}

}
