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
package org.meta.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bson.BSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;

/**
 *
 * A Search object is made to point to a list of results. It can be given as a question or as a response.
 *
 * Basically, a search possessed 3 dependences : - the source object which correspond to the source of the
 * interrogation - the metaData which correspond to the criteria of the question/response - the results who
 * are all you now about the pair source/meteData
 *
 * How to represent a search ? --------------------------------------- | Search |
 * ---------------------------------------- | Source | MetaData | ----------------------------------------
 *
 * |
 * -----> List OF Results
 *
 * The search Hash is composed by the concatenation of the source hash and the metaData hash. Wich allow
 * anyone who as a source and question about it to built a Search (who will be in this case a question) and
 * contact anyone who have declared knowing answers.
 *
 * A quick example : To find a subtitle to a movie, you can build a search like this :
 * --------------------------------------- | Unique search | ---------------------------------------- |
 * movie.avi | st:fr | ----------------------------------------
 *
 * You do no now if they are resuts, but you can give a try to look into the DHT. If someone as declared
 * knowing answer to your question, so you will be able to contact him, retrieve the answer and add it in the
 * result list of your search.
 *
 * A search received over network is no longe in a full stable state. It only represent is potential
 * linkedObject with tmp attrbutes.
 *
 * If you really need to update your object, you can use the method UpdateFromNetwork in ModelFactory class
 *
 * @author Thomas LAVOCAT
 */
public final class Search extends Searchable {

    private Searchable source = null;
    private MetaData metaData = null;
    private HashMap<MetHash, Data> linkedData = null;
    //Represent the state after network receiving
    private List<String> tmpLinkedData = null;
    private String tmpSourceHash = null;
    private String tmpMetaDataHash = null;

    /**
     *
     */
    protected Search() {
        super();
        linkedData = new HashMap<>();
        tmpLinkedData = new ArrayList<>();
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
     * @return the list of every data linked to this metaData. Those are stored in an hashMap, but return a
     * Collection, for simplicity purposes
     */
    public Collection<Data> getLinkedData() {
        return linkedData.values();
    }

    /**
     * Add linked datas to the search. Those are stored as an hashMap with the data's hash for key.
     *
     * If a conflict on hash key occurred, the last one will be take.
     *
     * @param lnkData the resultsto add to this search
     */
    public void addLinkedData(final List<Data> lnkData) {
        if (this.linkedData == null) {
            this.linkedData = new HashMap<>();
        }
        for (Data d : lnkData) {
            this.linkedData.put(d.getHash(), d);
        }
        this.updateState();
    }

    /**
     * set a linked Data.
     *
     * @param data the linked data to set
     *
     * if share same hash than another, will be overriding
     */
    public void setLinkedData(final Data data) {
        if (linkedData == null) {
            linkedData = new HashMap<>();
        }
        this.linkedData.put(data.getHash(), data);
        this.updateState();
    }

    /**
     * set the source; Not accessible outside Model package, because of hash processing triggered.
     *
     * @param src the source of this search
     */
    public void setSource(final Searchable src) {
        this.source = src;
        this.updateState();
        reHash();
    }

    /**
     *
     * @return return the list of results
     */
    public MetaData getMetaData() {
        return metaData;
    }

    /**
     * set the metaData. Not accessible outside Model package, because of hash processing triggered
     *
     * @param metData the new meta data for this search.
     */
    public void setMetaData(final MetaData metData) {
        this.metaData = metData;
        this.updateState();
        reHash();
    }

    @Override
    public MetHash reHash() {
        //Hash is composed of concatenation of sourcehash and metaData hash
        String srcHash = null;
        String dstHash = null;

        if (source != null) {
            srcHash = source.getHash().toString();
        } else {
            // TODO check that. This should not happend, ever ever ?!
            srcHash = "";
        }
        if (metaData != null) {
            dstHash = metaData.getHash().toString();
        } else {
            // TODO same as
            dstHash = "";
        }
        String concat = srcHash + dstHash;
        hash = MetamphetUtils.makeSHAHash(concat);
        return hash;
    }

    /**
     *
     * @return transform the Search object into a BSON Object.
     *
     * Linked datas are only pointed by their hash
     */
    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();

        BasicBSONList bsonLinkedData = new BasicBSONList();
        int i = 0;
        for (Iterator<Entry<MetHash, Data>> it = linkedData.entrySet().iterator(); it.hasNext(); i++) {
            Data data = it.next().getValue();
            bsonLinkedData.put(i, data.getHash().toString());
        }
        bsonObject.put("linkedData", bsonLinkedData);

        bsonObject.put("source", this.source.getHash().toString());
        bsonObject.put("metaData", this.metaData.getHash().toString());
        return bsonObject;
    }

    @Override
    protected void fillFragment(final LinkedHashMap<String, byte[]> fragment) {
        //write hash source
        fragment.put("_source", source.getHash().toByteArray());
        fragment.put("_metaData", metaData.getHash().toByteArray());

        //write every data's hash
        fragment.put("_nbLinkedData", (linkedData.size() + "").getBytes());
        int i = 0;
        for (Iterator<Entry<MetHash, Data>> it = linkedData.entrySet().iterator(); it.hasNext(); i++) {
            Data data = it.next().getValue();
            fragment.put("_i" + i + "_data", data.getHash().toByteArray());
        }

    }

    @Override
    protected void decodefragment(final LinkedHashMap<String, byte[]> fragment) {
        //when this method is called in a Search, her state is no more a real
        //Search but a temporary search, it means, it only represent what's
        //over the network, so source = null, metaData = null and LinkedData = null;
        source = null;
        metaData = null;
        linkedData = null;
        //and the Search cannot be write or updated in database

        //extract the source and delete from the fragment
        tmpSourceHash = new MetHash(fragment.get("_source")).toString();
        fragment.remove("_source");

        //extract the metada and delete from fragment
        tmpMetaDataHash = "";
        tmpMetaDataHash = new MetHash(fragment.get("_metaData")).toString();

        //extract all linkedDatas and delete it from the fragment too
        int nbLinkedData = Integer.parseInt(new String(fragment.get("_nbLinkedData")));
        tmpLinkedData = new ArrayList<>();
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

    /**
     *
     * @return ?
     */
    public String getTmpSourceHash() {
        return tmpSourceHash;
    }

    /**
     *
     * @return ?
     */
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

        ArrayList<Data> linkedDataClone = new ArrayList<>();
        for (Iterator<Data> i = linkedData.values().iterator(); i.hasNext();) {
            Data data = i.next();
            linkedDataClone.add((Data) data.toOnlyTextData());
        }
        searchClone.addLinkedData(linkedDataClone);
        return searchClone;
    }

    /**
     * Set all information to the search. TODO remove! normal setters are enough...
     *
     * @param src the new source of the search
     * @param metaDat the new metadata of the search
     * @param linked the new result list of the search
     */
    protected void set(final Searchable src, final MetaData metaDat, final ArrayList<Data> linked) {
        this.source = src;
        this.metaData = metaDat;
        addLinkedData(linked);
    }
}
