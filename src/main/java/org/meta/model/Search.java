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
 * A Search object is made to point to a list of results.
 * It can be given as a question or as a response.
 * 
 * Basically, a search possessed 3 dependences :
 * - the source object which correspond to the source of the interrogation
 * - the metaData which correspond to the criteria of the question/response
 * - the results who are all you now about the pair source/meteData
 *  
 * How to represent a search ? 
 *  ---------------------------------------
 * |              Search                   |
 * ----------------------------------------
 * |    Source      |      MetaData        |
 * ----------------------------------------
 *        
 *        |
 *        ----->  List OF Results
 *  
 * The search Hash is composed by the concatenation of the source hash and 
 * the metaData hash. Wich allow anyone who as a source and question about it
 * to built a Search (who will be in this case a question) and contact anyone
 * who have declared knowing answers.
 *  
 * A quick example :
 * To find a subtitle to a movie, you can build a search like this : 
 * ---------------------------------------
 * |              Unique search            |
 * ----------------------------------------
 * |    movie.avi  |      st:fr            |
 * ----------------------------------------
 * 
 * You do no now if they are resuts, but you can give a try to look into the DHT.
 * If someone as declared knowing answer to your question, so you will be able
 * to contact him, retrieve the answer and add it in the result list of your 
 * search. 
 * 
 * A search received over network is no longe in a full stable state.
 * It only represent is potential linkedObject with tmp attrbutes.
 * 
 * If you really need to update your object, you can use the method
 * UpdateFromNetwork in ModelFactory class
 * 
 * @author Thomas LAVOCAT 
 */
public class Search extends Searchable {

    private Searchable   source      = null;
    private MetaData     metaData    = null;
    private List<Data>   linkedData  = null;
    //Represent the state after network receiving
    private List<String> tmpLinkedData  = null;
    private String       tmpSourceHash  = null;
    private String       tmpMetaDataHash  = null;

    protected Search() {
        super();
    }

    /**
     * Create a new Searchable object -> use in case of creation
     *
     * @param hash       this search hash
     * @param source     search's source
     * @param metaData   search's metaData
     * @param linkedData search's results
     */
    protected Search(
            MetHash     hash,
            Searchable  source,
            List<Data>  linkedData,
            MetaData    metaData
    ) {
        super(hash);
        this.setSource(source);
        this.setMetaData(metaData);
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
     * set the source;
     * Not accessible outside Model package, because of hash processing triggered
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
        return metaData;
    }
    /**
     * set the metaData.
     * Not accessible outside Model package, because of hash processing triggered
     * @param metaData
     */
    protected void setMetaData(MetaData metaData) {
        this.metaData = metaData;
        this.updateState();
        reHash();
    }
    
    @Override
    public MetHash reHash() {
        //Hash is composed of concatenation of sourcehash and metaData hash
        String srcHash = source != null ? source.getHash().toString() : "";
        String dstHash = metaData != null ? metaData.getHash().toString() : "";
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
        bsonObject.put("result", this.metaData.getHash().toString());
        return bsonObject;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //write hash source
        fragment.put("_source",   source.getHash().toByteArray());
        fragment.put("_metaData", metaData.getHash().toByteArray());

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
        source      = null;
        metaData    = null;
        linkedData  = null;
        //and the Search cannot be write or updated in database

        //extract the source and delete from the fragment
        tmpSourceHash = new MetHash(fragment.get("_source")).toString();
        fragment.remove("_source");

        //extract the metada and delete from fragment
        tmpMetaDataHash = "";
        tmpMetaDataHash = new MetHash(fragment.get("_metaData")).toString();

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

    public String getTmpSourceHash() {
        return tmpSourceHash;
    }

    public String getTmpmetaDataHash() {
        return tmpMetaDataHash;
    }

    @Override
    public Searchable toOnlyTextData() {
        //the OnlyText will return
        //a clone of this search only containing source, metada and a list
        //of results in an onyText way
        Search searchClone = new Search();
        searchClone.setSource(source.toOnlyTextData());
        searchClone.setMetaData((MetaData) metaData.toOnlyTextData());

        ArrayList<Data> linkedDataClone = new ArrayList<Data>();
        for (Iterator<Data> i = linkedData.iterator(); i.hasNext();) {
            Data data = i.next();
            linkedDataClone.add((Data) data.toOnlyTextData());
        }
        searchClone.setLinkedData(linkedDataClone);
        return searchClone;
    }
    
    /**
     * Set all information to the search 
     * @param source
     * @param metaData
     * @param linked
     */
    protected void set(Searchable source, MetaData metaData, ArrayList<Data> linked) {
        this.source = source;
        this.metaData =  metaData;
        this.linkedData = linked;
    }
}
