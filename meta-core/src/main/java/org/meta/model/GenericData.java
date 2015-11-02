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

import java.nio.ByteBuffer;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.model.Data;
import org.meta.api.model.DataType;
import org.meta.utils.SerializationUtils;

/**
 * There is a size limit of 65535 bytes for the buffer.
 *
 * If created from a String, internal buffer will be UTF-8 encoded.
 *
 * @author Thomas LAVOCAT
 */
public class GenericData extends Data {

    private static final DataType GENERIC_DATA_TYPE = new DataType("abstract");

    /**
     * To avoid computing the hash every time a property changes, allow 'lazy hashing'. True if the hash needs
     * to be computed, false otherwise.
     */
    private boolean needRehash;

    /**
     * The underlying buffer buffer.
     */
    protected ByteBuffer buffer;

    /**
     * Empty data. Hash will be {@link MetHash.ZERO}.
     */
    protected GenericData() {
        super(MetHash.ZERO);
        needRehash = false;
    }

    /**
     * @param hash the hash
     */
    public GenericData(final MetHash hash) {
        super(hash);
        needRehash = false;
    }

    /**
     * @param buf byteBuffer to be copied as internal buffer. The given buffer's position remains unchanged.
     */
    public GenericData(final ByteBuffer buf) {
        this(MetHash.ZERO, buf);
        needRehash = true;
    }

    /**
     *
     * @param data content to be copied as internal buffer
     */
    public GenericData(final byte[] data) {
        this(MetHash.ZERO, data);
        needRehash = true;
    }

    /**
     *
     * @param string the string to be added as UTF-8 encoded buffer
     */
    public GenericData(final String string) {
        this(MetHash.ZERO, string);
        needRehash = true;
    }

    /**
     * @param hash the hash of this buffer
     * @param string the string to be added as UTF-8 encoded buffer
     */
    public GenericData(final MetHash hash, final String string) {
        super(hash);
        this.buffer = SerializationUtils.encodeUTF8(string);
        this.size = this.buffer.limit();
    }

    /**
     *
     * @param hash the hash of this buffer
     * @param data content to be copied as internal buffer
     */
    public GenericData(final MetHash hash, final byte[] data) {
        super(hash);
        this.buffer = ByteBuffer.allocate(data.length);
        this.buffer.put(data);
        this.size = data.length;
    }

    /**
     *
     * @param hash the hash of this buffer
     * @param buf byteBuffer to be used as internal buffer.
     */
    public GenericData(final MetHash hash, final ByteBuffer buf) {
        super(hash);
        this.buffer = buf;
        this.size = buf.limit();
    }

    /**
     *
     * @param hash the hash of this buffer
     * @param buf byteBuffer to be used as internal buffer.
     * @param realSize the size of the data. !Might be different than buf.limit()!
     */
    public GenericData(final MetHash hash, final ByteBuffer buf, final int realSize) {
        super(hash);
        this.buffer = buf;
        this.size = realSize;
    }

    /**
     *
     * @param str the buffer content as a string
     */
    public void setString(final String str) {
        this.buffer = SerializationUtils.encodeUTF8(str);
        this.size = this.buffer.limit();
        needRehash = true;
    }

    @Override
    public MetHash hash() {
        if (needRehash) {
            hash = MetamphetUtils.makeSHAHash(this.getBuffer());
        }
        return hash;
    }

    @Override
    public MetHash getHash() {
        if (needRehash) {
            return hash();
        }
        return this.hash;
    }

    @Override
    public byte[] getBytes() {
        //Only return copies of the internal buffer.
        byte[] bytes = new byte[this.buffer.capacity()];

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
        return GENERIC_DATA_TYPE;
    }

}
