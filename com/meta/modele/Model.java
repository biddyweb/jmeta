package com.meta.modele;


import java.io.File;
import java.util.ArrayList;

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
	
	/**
	 * Instanciate a new model. Init the dataBaseConnection.
	 * @throws LibraryException
	 */
	public Model() throws LibraryException{
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
	 * @return a search pointed by his hash. Return null if not found
	 * or if the hash is not pointed a Search object
	 */
	public Search getSearch(String hash){
		Search search = null;
		try {
			//Try to load the search true the data base
			search = (Search) load(hash);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		return search;
	}
	
	/**
	 * 
	 * @param hash
	 * @return a MetaData pointed by his hash or null is the hash is pointed
	 * on nothing or if the hash is pointed on a non MetaData object
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
	 * @return a Data pointed by the hash or null if the hash is pointed on 
	 * nothing or on a non Data Object
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
	 * Recursive synchronized method Load.
	 * 
	 * 
	 * @param hash
	 * @return a searchale object if found or null if not.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private synchronized Searchable load(String hash) throws 
											ClassNotFoundException, 
											InstantiationException, 
											IllegalAccessException 
	{
		//find in the dataBase everything if looking for the hash
		BSONArrayObj resultSearch 	 = connection.find(
						"JMeta", 
						"JMeta", 
						"$\"hash\" == \""+hash+"\"");
		Searchable searchable = null;
		//and get the first element (if exists of course)
		if(resultSearch.length()>=1){
			/*
			 * Every Searchable object have the same globals datas :
			 * - className
			 * - hash
			 */
			BSONObj 	jsonSearcheable	= resultSearch.get(0);
			String 		className	   	= jsonSearcheable.getString("class");
			//The class name is used to instantiate an object
			Class<?> 	clazz			= Class.forName(className);
			searchable 		= (Searchable) clazz.newInstance();
			//Set his hash code
			searchable.setHashCode(hash);
			//set his old json
			searchable.setOldJson(jsonSearcheable);
			
			/*
			 * Now we're looking for what is this object ? And extract the
			 * good one.
			 */
			if(searchable instanceof Search){
				extractSearch(searchable);
			}else if(searchable instanceof MetaData){
				extractMetaData(searchable);
			}else if(searchable instanceof DataFile){
				extractDataFile(searchable);
			}else if(searchable instanceof DataString){
				extractDataString(searchable);
			}
		}
		return searchable;
	}
	
	/**
	 * 
	 * @param searchable
	 */
	private void extractDataString(Searchable searchable) {
		DataString data = (DataString) searchable;
		String string = data.getOldJson().getString("string");
		data.setString(string);
	}

	/**
	 * Extract a data from Searchable object
	 * @param searchable
	 * @param jsonSearcheable
	 */
	private void extractDataFile(Searchable searchable) 
	{
		DataFile data = (DataFile) searchable;
		File file = new File(data.getOldJson().getString("file"));
		data.setFile(file);
	}

	/**
	 * Extract a metadata from a searchale object
	 * @param searchable
	 * @param jsonSearcheable
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	private void extractMetaData(Searchable searchable) 
		throws 
			ClassNotFoundException, 
			InstantiationException, 
			IllegalAccessException 
	{
		MetaData 		metaData		= (MetaData) searchable;
		BSONArrayObj 	arLinkedData 	= metaData.getOldJson().
													getBSONArray("linkedData");
		BSONArrayObj	arProperties	= metaData.getOldJson().
													getBSONArray("properties");

		//foreach datas pointed by the json
		//extract them with load method
		//and put them in the ArrayList datas
		ArrayList<Data> datas = new ArrayList<Data>();
		for(int i=0; i<arLinkedData.length(); i++){
			datas.add((Data) load(
					arLinkedData.get(i).getString("data")
					)
			);
		}
		metaData.setLinkedData(datas);
		
		//Foreach metaproperties
		//extract them from the old json
		//and add them to the ArrayList of properties
		ArrayList<MetaProperty> properties = new ArrayList<MetaProperty>();
 		for(int i=0; i<arProperties.length(); i++){
 			String name = arProperties.get(i).getString("name");
 			String value= arProperties.get(i).getString("value");
			properties.add(new MetaProperty(name, value));
		}
 		metaData.setProperties(properties);
	}

	/**
	 * Extract a search from a searchable object
	 * @param searchable
	 * @param jsonSearcheable
	 * @param hashSource
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessExceptionn
	 */
	private void extractSearch(Searchable searchable) 
	throws ClassNotFoundException, 
			InstantiationException, 
			IllegalAccessException 
	{
		Search 		 search   	= (Search) searchable;
		//load the source from her hash
		Searchable 	 source   	= load(search.getOldJson().getString("source"));
		//foreach metadata in the old json
		//extract them by the load method
		//and put them in the list
		BSONArrayObj arResults	= search.getOldJson().getBSONArray("results");
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
		//only if the searchable have to be updated 
		if(searchable.haveToUpdate()){
			//delete the old data
			BSONObj oldJson = searchable.getOldJson();
			connection.remove(	
					"JMeta", 
					"JMeta",  
					oldJson.getString("_id"), 
					oldJson.getString("_revision"));
			//insert the new
			connection.insert("JMeta", "JMeta", searchable.toJson());
			//And look for anything new to create
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
		//Only in case of updatable object, you can delete it
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
