package com.meta.modele;

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
 *
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

	public Searchable getSource() {
		return source;
	}

	public void setSource(Searchable source) {
		this.source = source;
	}

	public ArrayList<MetaData> getResults() {
		return results;
	}

	public void setResults(ArrayList<MetaData> results) {
		this.results = results;
	}

	public BSONObj toJson() {	
		BSONObj json = super.toJson();
		json.add("source", source.getHashCode());
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
		if(source.haveToCreate())
			lstChildsToCreate.add(source);
		lstChildsToCreate.addAll(source.getChildsToCreate());
		for(MetaData metaData : results){
			if(metaData.haveToCreate())
				lstChildsToCreate.add(metaData);
			lstChildsToCreate.addAll(metaData.getChildsToCreate());
		}
		return lstChildsToCreate;
	}

}
