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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.bson.BSONObject;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas LAVOCAT
 *
 * File implementation of Data object. Point to a File on the hard drive.
 */
public class DataFile extends Data {

    private File file = null;
    private static final int MAX_BLOC_SIZE = 65536;
    private Logger logger = LoggerFactory.getLogger(DataFile.class);

    /**
     * needed for java reflection
     */
    protected DataFile() {
        super();
    }

    /**
     * Instantiate a new DataFile with given hash and file. Used in case of
     * creation.
     *
     * @param hash The has of the DataFile.
     * @param file The underlying File.
     */
    protected DataFile(MetHash hash, File file) {
        super(hash);
        this.file = file;
    }

    /**
     * @return The File object pointing the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file The new file for this DataFile.
     *
     * This will change the final hash, only callable in the model.
     */
    public void setFile(File file) {
        this.file = file;
        this.updateState();
        reHash();
    }

    @Override
    public MetHash reHash() {
        hash = MetamphetUtils.makeSHAHash(file);
        return hash;
    }

    public BSONObject getBson() {
        /**
         * The hash is simply processed with the entire File content. TODO add
         * specific work depending on file extension
         */
        BSONObject bsonObject = super.getBson();
        bsonObject.put("file", file.getAbsolutePath());
        return bsonObject;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        super.fillFragment(fragment);
        //Test if file exist
        if (file != null && file.exists()) {
            //Put the fileName
            fragment.put("_fileName", file.getName().getBytes());

            /*
             * each data bloc is limited to 64kB in AMP protocol,
             * get the number of the blocs needed to send the file 
             */
            long size = file.length();
            long blocs = size / MAX_BLOC_SIZE;
            if (blocs < 1) {
                blocs = 1;
            }

            //set total size of the fil
            fragment.put("_size", (size + "").getBytes());
            //set number of blocks
            fragment.put("_count", (blocs + "").getBytes());

            //Read the File and write as much blocks as needed
            FileInputStream stream;
            try {
                stream = new FileInputStream(file);

                //For each blocks
                for (int i = 1; i <= blocs; i++) {
                    //Calculate the already read bytes
                    int offset = (i - 1) * MAX_BLOC_SIZE;

                    //size to read in the file
                    int sizeToRead = -1;
                    //if i < count, the size is 64ko
                    if (i < blocs) {
                        sizeToRead = MAX_BLOC_SIZE;
                        //if not but count was > 1, make the difference
                        //original size - nb * 64ko
                    } else if (blocs > 1) {
                        size = size - i * MAX_BLOC_SIZE;
                    } else {
                        //else it's the orinial size
                        sizeToRead = (int) size;
                    }

                    //make a new array to store the read data
                    byte[] bloc = new byte[sizeToRead];

                    //read the bytes from the stream
                    stream.read(bloc, offset, sizeToRead);

                    //Make the hash from the bloc for integrity check
                    MetHash blocHash = MetamphetUtils.makeSHAHash(bloc);

                    //write informations to the fragment
                    //hash
                    fragment.put("_" + i + "_blocHash", blocHash.toByteArray());
                    //bloc
                    fragment.put("_" + i + "_contentPart", bloc);

                }
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
        super.decodefragment(fragment);
        //File is temporary create in the java.io.tmpdir
        //If no fileName, there is no file to recreate
        if (fragment.containsKey("_fileName")) {
            String fileName = new String(fragment.get("_fileName"));
            file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);

            try {
                //Create the file and write every blocks
                if (file.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(file);
                    long count = Long.parseLong(new String(fragment.get("_count")));
                    fragment.remove("_size");
                    fragment.remove("_count");

                    for (int i = 1; i <= count; i++) {
                        String hash = new String(fragment.get("_" + i + "_blocHash"));
                        fragment.remove("_" + i + "_blocHash");
                        byte[] bloc = fragment.get("_" + i + "_contentPart");
                        if (MetamphetUtils.checkHash(hash, bloc)) {
                            fos.write(bloc);
                        } else {
                            //TODO write here the code needed to ask unCorrect blocs.
                        }
                    }
                    fos.close();
                }
            } catch (IOException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }
    }

    @Override
    public Searchable toOnlyTextData() {
        //Give a clone to this object, with no pointer to a File
        DataFile clone = new DataFile();
        clone.setHash(this.hash);
        clone.setDescription(this.getDescription());
        return clone;
    }
}
