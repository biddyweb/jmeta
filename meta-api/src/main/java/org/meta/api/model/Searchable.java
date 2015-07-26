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

import java.util.LinkedHashMap;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.meta.api.common.MetHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Super class of all model types. Contain a hash and a state
 *
 * @author faquin
 *
 */
public abstract class Searchable {

    /**
     *
     */
    protected MetHash hash = null;

    /**
     *
     */
    protected ObjectState state;
    private Logger logger = LoggerFactory.getLogger(Searchable.class);

    /**
     * Enum to lists the different possible states of a model object.
     */
    public enum ObjectState {

        /**
         * Object has been instantiated, it does not comes from network or database.
         */
        CREATED,
        /**
         * Object comes from network.
         */
        FROM_NETWORK,
        /**
         * Object comes from database and is up to date.
         */
        UP_TO_DATE,
        /**
         * Object comes from database but has been modified.
         */
        DIRTY;
    };

    /**
     * This constructor is needed for Java reflexion usage.
     */
    public Searchable() {
        state = ObjectState.CREATED;
    }

    /**
     * This constructor has to be used in case of creation.
     *
     * @param metHash creates a searchable with given hash
     */
    protected Searchable(final MetHash metHash) {
        this.hash = metHash;
        state = ObjectState.CREATED;
    }

    /**
     *
     * @return the hashCode of this Searchable object
     */
    public final MetHash getHash() {
        return hash;
    }

    /**
     * Only callable in model package.
     *
     * @param metHash the hash to set
     */
    public final void setHash(final MetHash metHash) {
        this.hash = metHash;
    }

    /**
     *
     * @return ObjectState The current state of this object.
     */
    public final ObjectState getState() {
        return state;
    }

    /**
     * Set the current state of this object.
     *
     * @param objectState ObjectState the state.
     */
    public final void setState(final ObjectState objectState) {
        this.state = objectState;
    }

    /**
     * For internal purposes, updates the current state of the object based on current state.
     *
     * For now, just sets the object as dirty if it comes from database.
     */
    protected final void updateState() {
        if (state == ObjectState.UP_TO_DATE) {
            logger.info("update state for " + this.hash.toString() + " set to dirty");
            state = ObjectState.DIRTY;
        }
    }

    /**
     * Called for rebuilding the hash.
     *
     * @return the newly updated hash
     */
    public abstract MetHash reHash();

    /**
     *
     * @return transform the Searchable object into a JSON string that can be stored directly
     */
    public final String toJson() {
        return this.getBson().toString();
    }

    /**
     *
     * transform the Searchable object into a BSON object for model serialization. Meant to be
     * object-recursive
     *
     * @return a BSON object filled with all needed information for the DB
     */
    public BSONObject getBson() {
        BasicBSONObject bsonObject = new BasicBSONObject("hash", this.hash.toString());
        bsonObject.put("type", ModelType.fromClass(this.getClass()).name());
        return bsonObject;
    }

    /**
     * build an AMP valid amp message part with the content of the Searchable object. Call "fillFragment" to
     * add specialized content function of implementing type. By default this method add two information in
     * the AnswerPart : - type - hash
     *
     * @return a {@link HashMap} containing a label for key and a byte[] for content. The content will be
     * translated in AMP message elsewhere.
     */
    public final LinkedHashMap<String, byte[]> getAmpAnswerPart() {
        LinkedHashMap<String, byte[]> fragment = new LinkedHashMap<>();
        fragment.put("_type", (this.getClass().getName() + "").getBytes());
        fragment.put("_hash", getHash().toByteArray());
        fillFragment(fragment);
        return fragment;
    }

    /**
     * Called when creating the answer part with the searchable object. Override this method to add usable
     * content in your AMP answers
     *
     * @param fragment the amp fragments
     */
    protected abstract void fillFragment(LinkedHashMap<String, byte[]> fragment);

    /**
     * Rebuild a Searchable object from an Amp answer part. Basically, it did the reverse work of
     * getAmpAnswerPart.
     *
     * @param fragment a {@link HashMap} containing a least the labels and values given in getAmpAnswerPart
     * and fillFragment.
     *
     * call decodeFragment wich allow implementing types to add somme usefull code.
     */
    public final void unParseFromAmpFragment(final LinkedHashMap<String, byte[]> fragment) {
        this.hash = new MetHash(fragment.get("_hash"));
        fragment.remove("_hash");
        decodefragment(fragment);
        fragment.clear();
    }

    /**
     * Called when unParsing from AMP message. Retrieve information in the map as they where put in
     * fillFragment method
     *
     * @param fragment same as unParseFromAmpFragment
     */
    protected abstract void decodefragment(LinkedHashMap<String, byte[]> fragment);

    /**
     * Allow implementing types to give a more textual version of themSelves This may be useful for
     * {@link DataFile}, allowing them to create a copy of themselves containing everything but the final
     * data.
     *
     * @return the
     */
    public abstract Searchable toOnlyTextData();
}
