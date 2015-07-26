/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.api.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import org.bson.BSONObject;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;

/**
 *
 * @author Thomas LAVOCAT
 *
 * String implementation of a Data object used to carry a simple text message.
 */
public final class DataString extends Data {

    private String string = null;
    private static final int MAX_BLOC_SIZE = 65536;

    /**
     * needed for java Reflexion.
     */
    protected DataString() {
        super();
    }

    /**
     * Instantiate a new Data -> use in case of creation.
     *
     * @param hash the hash of this data
     * @param string the content of the datastring
     */
    protected DataString(final MetHash hash, final String string) {
        super(hash);
        this.string = string;
    }

    /**
     * @return the string content
     */
    public String getString() {
        return string;
    }

    /**
     * As the String is used to process the hash calculation, this setter is only callable from the model
     * package.
     *
     * @param string the new content
     */
    public void setString(final String string) {
        this.string = string;
        this.updateState();
        reHash();
    }

    @Override
    public MetHash reHash() {
        //hash is made by hashing the String value
        hash = MetamphetUtils.makeSHAHash(string);
        return hash;
    }

    @Override
    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();
        bsonObject.put("string", string);
        return bsonObject;
    }

    @Override
    protected void fillFragment(final LinkedHashMap<String, byte[]> fragment) {
        super.fillFragment(fragment);

        byte[] totalString = string.getBytes();

        //Count how many blocks we have in the String
        long size = totalString.length;
        long count = size / MAX_BLOC_SIZE;

        if (count < 1) {
            count = 1;
        }

        //set size
        fragment.put("_size", (size + "").getBytes());
        //set count
        fragment.put("_count", (count + "").getBytes());

        //write every hash results
        for (int i = 1; i <= count; i++) {
            int offset = (i - 1) * MAX_BLOC_SIZE;

            //size to read in the String
            int sizeToRead = -1;
            if (i < count) {
                sizeToRead = MAX_BLOC_SIZE;
            } else if (count > 1) {
                size = size - i * MAX_BLOC_SIZE;
            } else {
                sizeToRead = (int) size;
            }

            //the byte array where to put the data
            byte[] bloc = Arrays.copyOfRange(totalString, offset, sizeToRead);

            //Make the hash from the bloc
            MetHash blocHash = MetamphetUtils.makeSHAHash(bloc);

            //write informations to the fragment
            //bloc number
            fragment.put("_i" + i + "_i", ((i - 1) + "").getBytes());
            //hash
            fragment.put("_i" + i + "_blocHash", blocHash.toByteArray());
            //bloc
            fragment.put("_i" + i + "_contentPart", bloc);
        }

    }

    @Override
    protected void decodefragment(final LinkedHashMap<String, byte[]> fragment) {
        super.decodefragment(fragment);
        long count = Long.parseLong(new String(fragment.get("_count")));
        fragment.remove("_size");
        fragment.remove("_count");

        StringBuilder sb = new StringBuilder();
        //write every hash results
        for (int i = 1; i <= count; i++) {
            String hash = new String(fragment.get("_i" + i + "_blocHash"));
            fragment.remove("_i" + i + "_blocHash");
            byte[] bloc = fragment.get("_i" + i + "_contentPart");
            if (MetamphetUtils.checkHash(hash, bloc)) {
                sb.append(new String(bloc));
            } else {
                //TODO write here the code needed to ask unCorrect blocs.
            }
        }
        //TODO final size check
        string = sb.toString();
    }

    @Override
    public Searchable toOnlyTextData() {
        //only return this
        return this;
    }

}
