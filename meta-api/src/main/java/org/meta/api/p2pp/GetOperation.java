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
package org.meta.api.p2pp;

import java.nio.ByteBuffer;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;

/**
 * An asynchronous operation representing the process of downloading a piece block from a peer through the
 * peer-to-peer protocol.
 *
 * @author dyslesiq
 */
public class GetOperation extends AsyncOperation {

    private final MetHash targetDataHash;

    private MetHash pieceHash;

    private final int pieceIndex;

    private final int byteOffset;

    private final int dataLength;

    private ByteBuffer data;

    /**
     *
     * @param hash the target data hash
     * @param pieceIdx the piece index
     * @param offset the byte offset inside the piece
     * @param length the requested data length
     */
    public GetOperation(final MetHash hash, final int pieceIdx, final int offset, final int length) {
        this.targetDataHash = hash;
        this.pieceIndex = pieceIdx;
        this.byteOffset = offset;
        this.dataLength = length;
    }

    @Override
    public void finish() {
        this.complete();
    }

    /**
     *
     * @return the piece index
     */
    public int getPieceIndex() {
        return pieceIndex;
    }

    /**
     *
     * @return the byte offset within the piece
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     *
     * @return the block data
     */
    public ByteBuffer getData() {
        return data;
    }

    /**
     *
     * @param blockData the block data
     */
    public void setData(final ByteBuffer blockData) {
        this.data = blockData;
    }

    /**
     *
     * @return the piece hash
     */
    public MetHash getPieceHash() {
        return pieceHash;
    }

    /**
     *
     * @param hash the piece hash
     */
    public void setPieceHash(final MetHash hash) {
        this.pieceHash = hash;
    }

    /**
     *
     * @return the target data hash
     */
    public MetHash getTargetDataHash() {
        return targetDataHash;
    }

    /**
     *
     * @return the requested data length
     */
    public int getDataLength() {
        return dataLength;
    }

}
