package org.meta.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bson.BSONObject;
import org.bson.types.BasicBSONList;

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
 *	GNU Affero General Public cloneLicense for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Thomas LAVOCAT this class correspond to a Search. with a source and
 * some results
 */
public class Search extends Searchable {

    private Searchable source = null;
    private List<MetaData> results = null;

    private String tmpSourceHashes = null;
    private List<String> tmpResultsHashes = null;

    public Search() {
        super();
        results = new ArrayList<MetaData>();
    }

    /**
     * Create a new Searchable object -> use in case of creation
     *
     * @param hashCode this search hashCode
     * @param source search's source
     * @param results search's results
     */
    public Search(
            String hashCode,
            Searchable source,
            List<MetaData> results
    ) {
        super(hashCode);
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
     *
     * @param source
     */
    public void setSource(Searchable source) {
        this.source = source;
        //TODO make the hash here only if source and results are sets
    }

    /**
     *
     * @return return the list of results
     */
    public List<MetaData> getResults() {
        return results;
    }

    /**
     * set the list of results
     *
     * @param results
     */
    public void setResults(List<MetaData> results) {
        this.results = results;
        //TODO make the hash hehre only if source and results are sets
    }

    /**
     *
     * @return transform the Search object into a BSON Object.
     */
    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();
        bsonObject.put("source", source.getHashCode());
        BasicBSONList bsonResultsList = new BasicBSONList();
        for (int i = 0; i < results.size(); i++) {
            MetaData metaData = results.get(i);
            bsonResultsList.put(i, metaData.getHashCode());
        }
        bsonObject.put("results", bsonResultsList);
        return bsonObject;
    }

    @Override
    public List<Searchable> getChildsToCreate() {
        List<Searchable> lstChildsToCreate = super.getChildsToCreate();
        //check if the source have to be created
        if (source.haveToCreate()) //if, add it
        {
            lstChildsToCreate.add(source);
        }
        //and add all its childs that have to be created
        lstChildsToCreate.addAll(source.getChildsToCreate());
        //foreach results
        for (MetaData metaData : results) {
            //if have to be created
            if (metaData.haveToCreate()) //add it
            {
                lstChildsToCreate.add(metaData);
            }
            //and add his childrens
            lstChildsToCreate.addAll(metaData.getChildsToCreate());
        }
        return lstChildsToCreate;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //write hash source
        fragment.put("_source", source.getHashCode().getBytes());
        //write every hash results
        fragment.put("_nbMetaData", (results.size() + "").getBytes());
        for (int i = 0; i < results.size(); i++) {
            MetaData metaData = results.get(i);
            fragment.put("_i" + i + "_metaData_" + i, metaData.getHashCode().getBytes());
        }

    }

    @Override
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
		//when this method is called in a Search, her state is no more a real
        //Search but a temporary search, it means, it only represent what's 
        //over the network, so source = null ans result = null
        source = null;
        results = null;
        //and the Search cannot be write or updated in database
        updateDB = false;
        createInDb = false;

        //extract the source and delete from the fragment
        tmpSourceHashes = new String(fragment.get("_source"));
        fragment.remove("_source");

        //extract all metaDatas and delete it from the fragment too
        int nbMetaData = Integer.parseInt(new String(fragment.get("_nbMetaData")));
        tmpResultsHashes = new ArrayList<String>();
        for (int i = 0; i < nbMetaData; i++) {
            String metaData = new String(fragment.get("_i" + i + "_metaData_" + 0));
            fragment.remove("_i" + i + "_metaData");
            tmpResultsHashes.add(metaData);
        }
    }

    public String getTmpSourceHashes() {
        return tmpSourceHashes;
    }

    public List<String> getTmpResultsHashes() {
        return tmpResultsHashes;
    }

	@Override
	public Searchable toOnlyTextData() {
		Search searchClone = new Search();
		searchClone.setSource(source.toOnlyTextData());
		ArrayList<MetaData> resultsClone = new ArrayList<MetaData>();
		for (Iterator<MetaData> i = results.iterator(); i.hasNext();) {
			MetaData metaData =  i.next();
			resultsClone.add((MetaData) metaData.toOnlyTextData());
		}
		searchClone.setResults(resultsClone);
		return searchClone;
	}
}
