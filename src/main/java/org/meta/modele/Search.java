package org.meta.modele;

import java.util.ArrayList;

import djondb.BSONArrayObj;
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
 * this class correspond to a Search. with a source and some results
 */
public class Search extends Searchable {

	private Searchable 				source 		= null;
	private ArrayList<MetaData>		results 	= null;
	
	public Search(){
		super();
		results = new ArrayList<MetaData>();
	}
	
	/**
	 * Create a new Searchable object -> use in case of creation
	 * @param hashCode this search hashCode
	 * @param source search's source
	 * @param results search's results
	 */
	public Search(
			String 				hashCode, 
			Searchable 			source, 
			ArrayList<MetaData> results
	) {
		super(hashCode);
		this.setSource(source);
		this.setResults(results);
	}
	
	/**
	 * Create a (not) new Searchable object -> use in case of data base loading
	 * @param hashCode this search hashCode
	 * @param source search's source
	 * @param results search's results
	 */
	public Search(
			String 				hashCode, 
			Searchable 			source, 
			ArrayList<MetaData> results,
			BSONObj json
	) {
		super(hashCode, json);
		this.setSource(source);
		this.setResults(results);
	}

	/**
	 * 
	 * @return the source
	 */
	public Searchable getSource() {
		return source;
	}

	/**
	 * set the source
	 * @param source
	 */
	public void setSource(Searchable source) {
		this.source = source;
	}

	/**
	 * 
	 * @return return the list of results
	 */
	public ArrayList<MetaData> getResults() {
		return results;
	}

	/**
	 * set the list of results
	 * @param results
	 */
	public void setResults(ArrayList<MetaData> results) {
		this.results = results;
	}

	public BSONObj toJson() {	
		BSONObj json = super.toJson();
		//add the source's hash to the json
		json.add("source", source.getHashCode());
		//foreach results, add their hashes to the json
		BSONArrayObj bsonResults = new BSONArrayObj();
		for (int i=0; i<results.size(); i++) {
			MetaData metaData = results.get(i);
			BSONObj bsonMetaData = new BSONObj();
			bsonMetaData.add("metaData", metaData.getHashCode());
			bsonResults.add(bsonMetaData);
		}
		json.add("results", bsonResults);
		return json;
	}
	
	@Override
	public ArrayList<Searchable> getChildsToCreate() {
		ArrayList<Searchable> lstChildsToCreate = super.getChildsToCreate(); 
		//check if the source have to be created
		if(source.haveToCreate())
			//if, add it
			lstChildsToCreate.add(source);
		//and add all its childs that have to be created
		lstChildsToCreate.addAll(source.getChildsToCreate());
		//foreach results
		for(MetaData metaData : results){
			//if have to be created
			if(metaData.haveToCreate())
				//add it
				lstChildsToCreate.add(metaData);
			//and add his childrens
			lstChildsToCreate.addAll(metaData.getChildsToCreate());
		}
		return lstChildsToCreate;
	}

}
