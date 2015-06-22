package org.meta.model;

import java.util.LinkedHashMap;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.meta.common.MetHash;

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
public abstract class Searchable {

    protected MetHash hash = null;
    protected ObjectState state;

    /**
     * Enum to lists the different possible states of a model object.
     */
    protected enum ObjectState {

        //Object has been instanciated, it does not comes from network or database.
        CREATED,
        //Object comes from network.
        FROM_NETWORK,
        //Object comes from database and is up to date.
        UP_TO_DATE,
        //Object comes from database but has been modified.
        DIRTY;
    };

    /**
     * This constructor is needed for Java reflexion usage
     */
    public Searchable() {
        state = ObjectState.CREATED;
    }

    /**
     * This constructor has to be used in case of creation
     *
     * @param hashCode
     */
    public Searchable(MetHash hash) {
        this.hash = hash;
        state = ObjectState.CREATED;
    }

    /**
     *
     * @return the hashCode of this Searchable object
     */
    public MetHash getHash() {
        return hash;
    }

    /**
     * @param hashCode the hashCode to set
     */
    public void setHash(MetHash hash) {
        this.hash = hash;
    }

    /**
     *
     * @return ObjectState The current state of this object.
     */
    public ObjectState getState() {
        return state;
    }

    /**
     * Set the current state of this object.
     *
     * @param state ObjectState the state.
     */
    public void setState(ObjectState state) {
        this.state = state;
    }

    /**
     * For internal purposes, updates the current state of the object based on current state.
     *
     * For now, just sets the object as dirty if it comes from database.
     */
    protected void updateState() {
        if (state == ObjectState.UP_TO_DATE) {
            System.out.println("update state for " + this.hash.toString() + " set to dirty");
            state = ObjectState.DIRTY;
        }
    }

    /**
     *
     * @return transform the Searchable object into a JSON string that can be stored directly
     */
    public String toJson() {
        return this.getBson().toString();
    }

    /**
     *
     * @return transform the Searchable object into a BSON object for serialization.
     */
    public BSONObject getBson() {
        BasicBSONObject bsonObject = new BasicBSONObject("hash", this.hash.toString());
        bsonObject.put("type", ModelType.fromClass(this.getClass()).name());
        return bsonObject;
    }

    public LinkedHashMap<String, byte[]> getAmpAnswerPart() {
        LinkedHashMap<String, byte[]> fragment = new LinkedHashMap<String, byte[]>();
        fragment.put("_type", (this.getClass().getName() + "").getBytes());
        fragment.put("_hash", getHash().toByteArray());
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
        this.hash = new MetHash(fragment.get("_hash"));
        fragment.remove("_hash");
        decodefragment(fragment);
        fragment.clear();
    }

    protected abstract void decodefragment(LinkedHashMap<String, byte[]> fragment);

    public abstract Searchable toOnlyTextData();
}
