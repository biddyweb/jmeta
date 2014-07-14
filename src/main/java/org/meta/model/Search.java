package org.meta.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import org.bson.BSONObject;
import org.bson.types.BasicBSONList;
import org.meta.common.MetHash;

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
 * @author Thomas LAVOCAT this class correspond to a Search. with a source and
 * some results
 */
public class Search extends Searchable {

    private Searchable source = null;
    private List<MetaData> results = null;

    private String tmpSourceHashes = null;
    private List<String> tmpResultsHashes = null;

    protected Search() {
        super();
        results = new ArrayList<MetaData>();
    }

    /**
     * Create a new Searchable object -> use in case of creation
     *
     * @param hash this search hash
     * @param source search's source
     * @param results search's results
     */
    public Search(
            MetHash hash,
            Searchable source,
            List<MetaData> results
    ) {
        super(hash);
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
        this.updateState();
    }

    /**
     *
     * @return return the list of results
     */
    public List<MetaData> getResults() {
        return Collections.unmodifiableList(results);
    }

    /**
     * 
     * @param metaDatas Results to add to the search's results.
     */
    public void addResults(MetaData ...metaDatas) {
        for (MetaData metaData : metaDatas) {
            this.results.add(metaData);
        }
        this.updateState();
    }

    /**
     * set the list of results
     *
     * @param results
     */
    public void setResults(List<MetaData> results) {
        this.results = results;
        this.updateState();
    }

    /**
     *
     * @return transform the Search object into a BSON Object.
     */
    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();
        bsonObject.put("source", source.getHash().toString());
        BasicBSONList bsonResultsList = new BasicBSONList();
        for (int i = 0; i < results.size(); i++) {
            MetaData metaData = results.get(i);
            bsonResultsList.put(i, metaData.getHash().toString());
        }
        bsonObject.put("results", bsonResultsList);
        return bsonObject;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //write hash source
        fragment.put("_source", source.getHash().toByteArray());
        //write every hash results
        fragment.put("_nbMetaData", (results.size() + "").getBytes());
        for (int i = 0; i < results.size(); i++) {
            MetaData metaData = results.get(i);
            fragment.put("_i" + i + "_metaData_" + i, metaData.getHash().toByteArray());
        }
    }

    @Override
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
        //when this method is called in a Search, her state is no more a real
        //Search but a temporary search, it means, it only represent what's 
        //over the network, so source = null ans result = null
        source = null;
        results = null;

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
}
