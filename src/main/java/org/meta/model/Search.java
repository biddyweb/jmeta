package org.meta.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bson.BSONObject;
import org.bson.types.BasicBSONList;
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

    private Searchable   source      = null;
    private MetaData     result      = null;
    private List<Data>   linkedData  = null;
    private List<String> tmpLinkedData;

    private String tmpSourceHash  = null;
    private String tmpResultHash  = null;

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
            List<Data> linkedData,
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
     *
     * @return A read-only list of every data linked to this metaData
     */
    public List<Data> getLinkedData() {
        return Collections.unmodifiableList(linkedData);
    }

    /**
     * Set linked data
     *
     * @param linkedData
     */
    protected void setLinkedData(List<Data> linkedData) {
        this.linkedData = linkedData;
        this.updateState();
    }

 
    /**
     * set the source
     *
     * @param source
     */
    protected void setSource(Searchable source) {
        this.source = source;
        this.updateState();
        reHash();
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

        BasicBSONList bsonLinkedData = new BasicBSONList();
        for (int i = 0; i < linkedData.size(); ++i) {
            bsonLinkedData.put(i, linkedData.get(i).getHash().toString());
        }
        bsonObject.put("linkedData", bsonLinkedData);
 
        bsonObject.put("source", this.source.getHash().toString());
        bsonObject.put("result", this.result.getHash().toString());
        return bsonObject;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //write hash source
        fragment.put("_source",   source.getHash().toByteArray());
        fragment.put("_metaData", result.getHash().toByteArray());

        //write every data's hash
        fragment.put("_nbLinkedData", (linkedData.size() + "").getBytes());
        for (int i = 0; i < linkedData.size(); i++) {
            Data data = linkedData.get(i);
            fragment.put("_i" + i + "_data", data.getHash().toByteArray());
        }
 
    }

    @Override
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
        //when this method is called in a Search, her state is no more a real
        //Search but a temporary search, it means, it only represent what's
        //over the network, so source = null and result = null
        source = null;
        result = null;
        linkedData = null;
        //and the Search cannot be write or updated in database

        //extract the source and delete from the fragment
        tmpSourceHash = new MetHash(fragment.get("_source")).toString();
        fragment.remove("_source");

        //extract the metada and delete from fragment
        tmpResultHash = "";
        tmpResultHash = new MetHash(fragment.get("_metaData")).toString();

        //extract all linkedDatas and delete it from the fragment too
        int nbLinkedData = Integer.parseInt(new String(fragment.get("_nbLinkedData")));
        tmpLinkedData = new ArrayList<String>();
        for (int i = 0; i < nbLinkedData; i++) {
            String data = new MetHash(fragment.get("_i" + i + "_data")).toString();
            fragment.remove("_i" + i + "_data");
            tmpLinkedData.add(data);
        }
 
    }
    /**
     *
     * @return a list of the futures results
     */
    public List<String> getTmpLinkedData() {
        return tmpLinkedData;
    }

    public void set(ArrayList<Data> linked) {
        this.linkedData = linked;
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

        ArrayList<Data> linkedDataClone = new ArrayList<Data>();
        for (Iterator<Data> i = linkedData.iterator(); i.hasNext();) {
            Data data = i.next();
            linkedDataClone.add((Data) data.toOnlyTextData());
        }
        searchClone.setLinkedData(linkedDataClone);
        return searchClone;
    }

    public void set(Searchable source, Searchable result, ArrayList<Data> linked) {
        this.source = source;
        this.result = (MetaData) result;
        this.linkedData = linked;
    }
}
