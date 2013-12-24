package com.meta.modele;

import java.util.ArrayList;

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
 * This abstract class represent something that can be searchable and storable
 * in the dataBase
 */
public abstract class Searchable {
	
	private 	String 	 	hashCode 	= null;
	private 	BSONObj 	oldJson 	= null;
	
	private		boolean    createInDb  = false;
	private 	boolean 	updateDB	= false;
	
	/**
	 * This constrcutor is needed for Java reflexion usage
	 */
	public Searchable(){
		hashCode 	= "empty";
	}
	
	/**
	 * This construcotr has to be used in case of creation 
	 * @param hashCode
	 */
	public Searchable(String hashCode){
		this.hashCode 	= hashCode;
		updateDB   = false;
		createInDb = true;
	}
	
	/**
	 * This constructor has to be used in case of data base loading
	 * @param hashCode
	 */
	public Searchable(String hashCode, BSONObj json){
		this.hashCode 	= hashCode;
		setOldJson(json);
	}
	
	/**
	 * 
	 * @return the hashCode of this Searchable object
	 */
	public String getHashCode(){
		return hashCode;
	}
	
	/**
	 * @param hashCode the hashCode to set
	 */
	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}

	/**
	 * 
	 * @return true if you have to update false in other case
	 */
	public boolean haveToUpdate() {
		return updateDB;
	}
	
	/**
	 * 
	 * @return true if you have to create false in othe case
	 */
	public boolean haveToCreate(){
		return createInDb;		
	}
	
	/**
	 * 
	 * @return transform the Searchable object into a JSON that can 
	 * be storable
	 */
	public BSONObj toJson(){
		BSONObj json = new BSONObj();
		json.add("hash", this.getHashCode());
		json.add("class", this.getClass().getName());
		return json;
	}

	/**
	 * return the old json used to load from dataBase
	 * @return
	 */
	public BSONObj getOldJson() {
		return oldJson;
	}

	/**
	 * Set the old json used to load from dataBase
	 * @param oldJson
	 */
	public void setOldJson(BSONObj oldJson) {
		//put the update flag to true dans create to false,
		updateDB 	= true;
		createInDb 	= false;
		this.oldJson = oldJson;
	}
	
	/**
	 *
	 * @return the list of childs to create
	 */
	public ArrayList<Searchable> getChildsToCreate(){
		return new ArrayList<Searchable>();
	}
	
}
