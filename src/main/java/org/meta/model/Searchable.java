package org.meta.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;

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
 * @author Thomas LAVOCAT This abstract class represent something that can be
 * searchable and storable in the dataBase
 */
public abstract class Searchable {

    protected String hash = null;
    protected boolean createInDb = false;
    protected boolean updateDB = false;

    /**
     * This constructor is needed for Java reflexion usage
     */
    public Searchable() {
        hash = "empty";
    }

    /**
     * This constructor has to be used in case of creation
     *
     * @param hashCode
     */
    public Searchable(String hashCode) {
        this.hash = hashCode;
        updateDB = false;
        createInDb = true;
    }

    /**
     *
     * @return the hashCode of this Searchable object
     */
    public String getHashCode() {
        return hash;
    }

    /**
     * @param hashCode the hashCode to set
     */
    public void setHashCode(String hashCode) {
        this.hash = hashCode;
    }

    /**
     *
     * @return true if you have to update false in other case
     */
    public boolean haveToUpdate() {
        return updateDB;
    }

    /**
     *
     * @return true if you have to create false in other case
     */
    public boolean haveToCreate() {
        return createInDb;
    }

    /**
     *
     * @return transform the Searchable object into a JSON string that can be
     * stored directly
     */
    public String toJson() {
        return this.getBson().toString();
    }

    /**
     *
     * @return transform the Searchable object into a JSON that can be stored
     */
    public BSONObject getBson() {
        BasicBSONObject bsonObject = new BasicBSONObject("hash", this.hash);
        bsonObject.put("class", this.getClass().getName());
        return bsonObject;
    }

    /**
     *
     * @return the list of children to create
     */
    public List<Searchable> getChildsToCreate() {
        return new ArrayList<Searchable>();
    }

    public LinkedHashMap<String, byte[]> getAmpAnswerPart() {
        LinkedHashMap<String, byte[]> fragment = new LinkedHashMap<String, byte[]>();
        fragment.put("_type", (this.getClass().getName() + "").getBytes());
        fragment.put("_hash", getHashCode().getBytes());
        fillFragment(fragment);
        return fragment;
    }

    /**
     * Fill the fragment with useful informations
     *
     * @param fragment
     */
    protected abstract void fillFragment(LinkedHashMap<String, byte[]> fragment);

    public void unParseFromAmpFragment(LinkedHashMap<String, byte[]> fragment) {
        this.createInDb = true;
        this.updateDB = false;
        this.hash = new String(fragment.get("_hash"));
        fragment.remove("_hash");
        decodefragment(fragment);
        fragment.clear();
    }

    protected abstract void decodefragment(LinkedHashMap<String, byte[]> fragment);
}
