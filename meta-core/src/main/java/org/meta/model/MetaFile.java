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
package org.meta.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.DataType;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public final class MetaFile extends DataFile {

    private final Logger logger = LoggerFactory.getLogger(MetaFile.class);

    private static final DataType META_FILE_TYPE = new DataType("file");

    private ByteBuffer buffer;

    /**
     * Creates an empty MetaFile, its hash will be {@link MetHash.ZERO}.
     */
    protected MetaFile() {
    }

    /**
     * Instantiate a new MetaFile with given file.
     *
     * The hash will be computed from the file.
     *
     * @param aFile The underlying File.
     */
    public MetaFile(final File aFile) {
        this.setFile(aFile);
    }

    /**
     * Instantiate a new MetaFile from given URI.
     *
     * The hash will be calculated from the file pointed to by the URI.
     *
     * @param uri the URI pointing to the file to manage
     */
    public MetaFile(final URI uri) {
        this.setURI(uri);
    }

    /**
     * Instantiate a new MetaFile with given hash and file. Used in case of creation.
     *
     * @param hash The has of the MetaFile.
     * @param fil The underlying File.
     */
    public MetaFile(final MetHash hash, final File fil) {
        this.hash = hash;
        this.file = fil;
        this.sizeFromFile();
        this.updateBuffer();
    }

    /**
     * Instantiate a new MetaFile with given hash and file. Used in case of creation.
     *
     * @param hash The has of the MetaFile.
     * @param uri the URI pointing to the file to manage
     * @param fileSize the size of the file
     */
    public MetaFile(final MetHash hash, final URI uri, final int fileSize) {
        this.hash = hash;
        this.file = new File(uri);
        this.size = fileSize;
        this.updateBuffer();
    }

    /**
     * Creates this data file from the given {@link Data}.
     *
     * It will try to extract the file URI from the buffer, or throw an URISyntaxException if the given data
     * buffer does not contains a valid URI.
     *
     * The hash and the size will be the same than the given data.
     *
     * @param data the GenericData
     * @throws URISyntaxException if invalid URI if found in given data
     */
    public MetaFile(final Data data) throws URISyntaxException {
        this.hash = data.getHash();
        URI fileUri = new URI(data.toString());
        this.file = new File(fileUri);
        this.updateBuffer();
        this.size = data.getSize();
        this.metaDataMap = data.getMetaDataMap();
    }

    /**
     * Set the internal buffer content to the URI of the file.
     */
    private void updateBuffer() {
        if (file != null) {
            this.buffer = SerializationUtils.encodeUTF8(this.file.toURI().toString());
        }
    }

    /**
     * If this.file is a valid file, set the size from the file's length.
     */
    private void sizeFromFile() {
        if (file != null) {
            if (file.isFile() && file.exists()) {
                //TODO this means we can't handle files larger than 4GB
                this.size = (int) file.length();
            }
        }
    }

    @Override
    public MetHash hash() {
        this.hash = MetamphetUtils.makeSHAHash(file);
        return hash;
    }

    /**
     * @return The File object pointing the file
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     * @param aFile the new file for this DataFile.
     *
     */
    @Override
    public void setFile(final File aFile) {
        this.file = aFile;
        this.updateBuffer();
        this.sizeFromFile();
        hash();
    }

    @Override
    public void setURI(final URI uri) {
        setFile(new File(uri));
    }

    @Override
    public URI getURI() {
        try {
            return new URI(this.toString());
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    @Override
    public byte[] getBytes() {
        //Only return copies of the internal buffer.
        byte[] bytes = new byte[this.buffer.limit()];

        this.getBuffer().get(bytes);
        return bytes;
    }

    @Override
    public ByteBuffer getBuffer() {
        ByteBuffer roBuffer = this.buffer.asReadOnlyBuffer();

        roBuffer.rewind();
        return roBuffer;
    }

    @Override
    public String toString() {
        return SerializationUtils.decodeUTF8(this.getBuffer());
    }

    @Override
    public DataType getType() {
        return META_FILE_TYPE;
    }

}
