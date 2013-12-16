package com.meta.modele;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import djondb.BSONArrayObj;
import djondb.BSONObj;
import djondb.DjondbConnection;
import djondb.DjondbConnectionManager;
import djondb.LibraryException;
import djondb.Loader;

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
public class Model {
	//DataBase connection
	private DjondbConnection connection = null;	
	
	//this list act like a buffer
	private HashMap<String, Search> hashSearchable = null;
	
	
	public Model() throws LibraryException{
		hashSearchable = new HashMap<String, Search>();
		initDataBase();
	}
	
	/**
	 * Initialize data base connection
	 * @throws LibraryException 
	 */
	private void initDataBase() throws LibraryException {
		Loader.initialize();
		connection = DjondbConnectionManager.getConnection("localhost");
		connection.open();
	}

	/**
	 * 
	 * @param hash
	 * @return
	 */
	public Search getSearch(String hash){
		Search search = hashSearchable.get(hash);
		if(search == null);
			try {
				search = (Search) load(hash);
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				e.printStackTrace();
			}
			if(search != null)
				hashSearchable.put(hash, search);
			return search;
	}
	
	/**
	 * 
	 * @param hash
	 * @return
	 */
	public MetaData getMetaData(String hash){
		MetaData metaData = null;
		try {
			metaData = (MetaData) load(hash);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		return metaData;
	}

	/**
	 * 
	 * @param hash
	 * @return
	 */
	public Data getData(String hash){
		Data data = null;
		try {
			data = (Data) load(hash);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * 
	 * @param hash
	 * @return the Search linked to the hash or null if not found
	 */
	public Searchable getSearchable(String hash){
		Searchable 	foundedObject 	= null;
		try {
			foundedObject = load(hash);
		} catch (ClassNotFoundException 
				| InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
	
		return 	foundedObject;
	}
	
	/**
	 * 
	 * @param hash
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private synchronized Searchable load(String hash) throws 
											ClassNotFoundException, 
											InstantiationException, 
											IllegalAccessException 
	{
		BSONArrayObj resultSearch 	 = connection.find(
						"JMeta", 
						"JMeta", 
						"$\"hash\" == \""+hash+"\"");
		Searchable searchable = null;
		if(resultSearch.length()>=1){
			BSONObj 	jsonSearcheable	= resultSearch.get(0);
			String 		className	   	= jsonSearcheable.getString("class");
			Class 		clazz			= Class.forName(className);
			searchable 		= (Searchable) clazz.newInstance();
			searchable.setHashCode(hash);
			searchable.setOldJson(jsonSearcheable);
			
			if(searchable instanceof Search){
				extractSearch(searchable, jsonSearcheable);
			}else if(searchable instanceof MetaData){
				extractMetaData(searchable, jsonSearcheable);
			}else if(searchable instanceof Data){
				extractData(searchable, jsonSearcheable);
			}
		}
		return searchable;
	}
	
	/**
	 * 
	 * @param searchable
	 * @param jsonSearcheable
	 */
	private void extractData(
			Searchable searchable, 
			BSONObj jsonSearcheable) 
	{
		Data data = (Data) searchable;
		File file = new File(jsonSearcheable.getString("file"));
		data.setFile(file);
	}

	/**
	 * 
	 * @param searchable
	 * @param jsonSearcheable
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	private void extractMetaData(
			Searchable searchable, 
			BSONObj jsonSearcheable) 
		throws 
			ClassNotFoundException, 
			InstantiationException, 
			IllegalAccessException 
	{
		MetaData 		metaData		= (MetaData) searchable;
		BSONArrayObj 	arLinkedData 	= jsonSearcheable.
													getBSONArray("linkedData");
		BSONArrayObj	arProperties	= jsonSearcheable.
													getBSONArray("properties");

		ArrayList<Data> datas = new ArrayList<Data>();
		for(int i=0; i<arLinkedData.length(); i++){
			datas.add((Data) load(
					arLinkedData.get(i).getString("data")
					)
			);
		}
		metaData.setLinkedData(datas);
		
		ArrayList<MetaProperty> properties = new ArrayList<MetaProperty>();
 		for(int i=0; i<arProperties.length(); i++){
 			String name = arProperties.get(i).getString("name");
 			String value= arProperties.get(i).getString("value");
			properties.add(new MetaProperty(name, value));
		}
 		metaData.setProperties(properties);
	}

	/**
	 * 
	 * @param searchable
	 * @param jsonSearcheable
	 * @param hashSource
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessExceptionn
	 */
	private void extractSearch(
			Searchable searchable, 
			BSONObj jsonSearcheable) 
	throws ClassNotFoundException, 
			InstantiationException, 
			IllegalAccessException 
	{
		Search 		 search   	= (Search) searchable;
		Searchable 	 source   	= load(jsonSearcheable.getString("source"));
		
		BSONArrayObj arResults	= jsonSearcheable.getBSONArray("results");
		ArrayList<MetaData> results = new ArrayList<MetaData>();
		for (int i = 0; i < arResults.length(); i++) {
			results.add((MetaData) load(
								arResults.get(i).getString("metaData")
								)
						);
		}
		search.setSource(source);
		search.setResults(results);
	}

	/**
	 * update a search in DB with all dependencies created / updated
	 * @param search the object to create / update
	 */
	public synchronized void updateInDataBase(Searchable searchable){
		if(searchable.haveToUpdate()){
			BSONObj oldJson = searchable.getOldJson();
			connection.remove(	
					"JMeta", 
					"JMeta",  
					oldJson.getString("_id"), 
					oldJson.getString("_revision"));
			connection.insert("JMeta", "JMeta", searchable.toJson());
			ArrayList<Searchable> lstChildsToCreate = 
												searchable.getChildsToCreate();
			for(Searchable itemToCreate : lstChildsToCreate)
				createInDataBase(itemToCreate);
		}
	}

	/**
	 * delete a search in DB with all dependencies created / updated
	 * @param search the object to create / update
	 */
	public synchronized void deleteInDataBase(Searchable searchable){
		if(searchable.haveToUpdate()){
			BSONObj oldJson = searchable.getOldJson();
			oldJson.begin();
			connection.remove(	
					"JMeta", 
					"JMeta",  
					oldJson.getString("_id"), 
					oldJson.getString("_revision"));
			oldJson.end();
		}
	}
	
	/**
	 * Create a search in DB with all dependencies created / updated
	 * @param search the object to create / update
	 */
	public synchronized void createInDataBase(Searchable searchable){
		connection.insert("JMeta", "JMeta", searchable.toJson());
	}
	
	/**
	 * Release the database connection via DjondbConnectionManager
	 */
	public void releaseConnection(){
		DjondbConnectionManager.releaseConnection(connection);
	}
}
