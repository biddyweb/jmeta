package org.meta.model;

import java.util.Arrays;
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
 *    along with this program. HashMap If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Thomas LAVOCAT
 *
 * This class correspond to a data on the hard drive. Pointed by a file.
 */
public class DataString extends Data {

    private String string = null;

    /**
     * needed for java Reflexion
     */
    protected DataString() {
        super();
    }

    /**
     * Instantiate a new Data -> use in case of creation
     *
     * @param hashCode
     * @param file
     */
    public DataString(MetHash hash, String string) {
        super(hash);
        this.string = string;
    }

    /**
     * @return the file
     */
    public String getString() {
        return string;
    }

    /**
     * @param file the file to set
     */
    public void setString(String string) {
        this.string = string;
        this.updateState();
    }

    /**
     *
     * @override
     */
    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();
        bsonObject.put("string", string);
        return bsonObject;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //TODO Move elsewhere.
        byte[] totalString = string.getBytes();

        //Send the file, it will surrely be bigger than 65 536o
        long size = totalString.length;
        long count = size / 65536;

        if (count < 1) {
            count = 1;
        }

        //set size
        fragment.put("_size", (size + "").getBytes());
        //set count
        fragment.put("_count", (count + "").getBytes());

        //write every hash results
        for (int i = 1; i <= count; i++) {
            int offset = (i - 1) * 65536;

            //size to read in the file
            int sizeToRead = -1;
            if (i < count) {
                sizeToRead = 65536;
            } else if (count > 1) {
                size = size - i * 65536;
            } else {
                sizeToRead = (int) size;
            }

            //the byte arry where to put the data
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
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
        long size = Long.parseLong(new String(fragment.get("_size")));
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
        return this;
    }

}
