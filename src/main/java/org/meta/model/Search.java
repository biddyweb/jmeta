package org.meta.model;

import java.util.LinkedHashMap;

import org.bson.BSONObject;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;

/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Thomas LAVOCAT
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Thomas LAVOCAT this class correspond to a Search. with a source and
 * some results
 */
public class Search extends Searchable {

    private Searchable source = null;
    private MetaData   result = null;

    private String tmpSourceHash  = null;
    private String tmpResultHash = null;

    protected Search() {
        super();
    }

    /**
     * Create a new Searchable object -> use in case of creation
     *
     * @param hash this search hash
     * @param source search's source
     * @param result search's results
     */
    protected Search(
            MetHash hash,
            Searchable source,
            MetaData result
    ) {
        super(hash);
        this.setSource(source);
        this.setResult(result);
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
    protected void setSource(Searchable source) {
        this.source = source;
        this.updateState();
    }

    /**
     *
     * @return return the list of results
     */
    public MetaData getResult() {
        return result;
    }
    /**
     * set the list of results
     *
     * @param result
     */
    protected void setResult(MetaData result) {
        this.result = result;
        this.updateState();
        reHash();
    }
    
    @Override
    public MetHash reHash() {
        String srcHash = source != null ? source.getHash().toString() : "";
        String dstHash = result != null ? result.getHash().toString() : "";
        String concat = srcHash+dstHash;
        hash = MetamphetUtils.makeSHAHash(concat);
        return hash;
    }

    /**
     *
     * @return transform the Search object into a BSON Object.
     */
    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();
        bsonObject.put("source", this.source.getHash().toString());
        bsonObject.put("result", this.result.getHash().toString());
        return bsonObject;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //write hash source
        fragment.put("_source",   source.getHash().toByteArray());
        fragment.put("_metaData", result.getHash().toByteArray());

    }

    @Override
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
        //when this method is called in a Search, her state is no more a real
        //Search but a temporary search, it means, it only represent what's
        //over the network, so source = null and result = null
        source = null;
        result = null;
        //and the Search cannot be write or updated in database

        //extract the source and delete from the fragment
        tmpSourceHash = new String(fragment.get("_source"));
        fragment.remove("_source");

        //extract the metada and delete from fragment
        tmpResultHash = "";
        tmpResultHash = new String(fragment.get("_metaData"));
    }

    public String getTmpSourceHashes() {
        return tmpSourceHash;
    }

    public String getTmpResultsHashes() {
        return tmpResultHash;
    }

    @Override
    public Searchable toOnlyTextData() {
        Search searchClone = new Search();
        searchClone.setSource(source.toOnlyTextData());
        searchClone.setResult((MetaData) result.toOnlyTextData());
        return searchClone;
    }
}
