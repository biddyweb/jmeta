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
package org.meta.model.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.common.OperationListener;
import org.meta.api.model.DataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Piece and block accessor for a DataFile.
 *
 * @author dyslesiq
 */
public class DataFileAccessor {

    /**
     * Constant piece size.
     */
    public static final int PIECE_SIZE = 512 * 1024;

    /**
     * Constant block size.
     */
    public static final int BLOCK_SIZE = 16 * 1024;

    private final Logger logger = LoggerFactory.getLogger(DataFileAccessor.class);

    private final DataFile dataFile;

    private int pieceCount;

    private int blockCount;

    private final MetHash[] piecesHash;

    //private final Path filePath;
    private final AsynchronousFileChannel fileChannel;

    /**
     *
     * @param file the data file to access
     * @throws java.io.IOException If given an invalid file (TODO Explain)
     */
    public DataFileAccessor(final DataFile file) throws IOException {
        this.dataFile = file;
        //this.filePath = Paths.get(this.dataFile.getURI());
        fileChannel = AsynchronousFileChannel.open(dataFile.getFile().toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        this.pieceCount = this.dataFile.getSize() / PIECE_SIZE;
        if (this.dataFile.getSize() % PIECE_SIZE != 0) {
            this.pieceCount++;
        }

        this.blockCount = this.dataFile.getSize() / BLOCK_SIZE;
        if (this.dataFile.getSize() % BLOCK_SIZE != 0) {
            this.blockCount++;
        }
        this.piecesHash = new MetHash[this.pieceCount];
    }

    /**
     * Read data from a file asynchronously.
     *
     * @param offset the offset in the file
     * @param length the number of bytes to read
     * @return the asynchronous operation
     */
    public synchronized FileReadOperation read(final int offset, final int length) {
        FileReadOperation operation = new FileReadOperation(offset, length);

        fileChannel.read(operation.getBuffer(), offset, null, operation);
        return operation;
    }

    /**
     * Write data to a file asynchronously.
     *
     * TODO use file locks!
     *
     * @param pieceIndex the piece index
     * @param byteOffset the offset in the piece
     * @param buf the buffer to write at given position
     * @return the asynchronous operation
     */
    public FileWriteOperation write(final int pieceIndex, final int byteOffset, final ByteBuffer buf) {
        FileWriteOperation op = new FileWriteOperation();

        int writeOffset = (pieceIndex * PIECE_SIZE) + byteOffset;
        fileChannel.write(buf, writeOffset, null, op);
        return op;
    }

    /**
     * Get the hash of the given piece in the file asynchronously.
     *
     * @param pieceIndex the index of the piece to hash
     * @return the hash of the piece
     */
    public PieceHashOperation getPieceHash(final int pieceIndex) {
        PieceHashOperation op = new PieceHashOperation(pieceIndex);
        FileReadOperation readOp = this.read(this.fileOffset(pieceIndex, 0), this.pieceSize(pieceIndex));

        readOp.addListener(new OperationListener<FileReadOperation>() {

            @Override
            public void failed(final FileReadOperation operation) {
                op.setFailed(operation.getFailureMessage());
            }

            @Override
            public void complete(final FileReadOperation operation) {
                piecesHash[pieceIndex] = MetamphetUtils.makeSHAHash(operation.getBuffer());
                op.setPieceHash(piecesHash[pieceIndex]);
                op.complete();
            }
        });
        return op;
    }

    /**
     * Get the hash of the given piece in the file synchronously.
     *
     * @param pieceIndex the index of the piece to hash
     * @return the hash of the piece
     */
    public synchronized MetHash getPieceHashSync(final int pieceIndex) {
        if (this.piecesHash[pieceIndex] == null) {
            FileInputStream fis;
            FileChannel channel;
            ByteBuffer buff;
            try {
                fis = new FileInputStream(dataFile.getFile());
                channel = fis.getChannel();
                buff = channel.map(FileChannel.MapMode.READ_ONLY,
                        pieceIndex * PIECE_SIZE, pieceSize(pieceIndex));
                this.piecesHash[pieceIndex] = MetamphetUtils.makeSHAHash(buff);
            } catch (IOException e) {
                logger.error("IOException while getting piece data for hash calculation", e);
                piecesHash[pieceIndex] = MetHash.ZERO;
            }
        }
        return this.piecesHash[pieceIndex];
    }

    /**
     *
     * @param pieceIdx the piece index
     * @return the size of the given piece or zero if invalid
     */
    public int pieceSize(final int pieceIdx) {
        if (pieceIdx != this.pieceCount - 1) {
            //Only the last piece may have a different size
            return PIECE_SIZE;
        }
        return this.dataFile.getSize() % PIECE_SIZE;
    }

    /**
     * Get the size of the block starting at offset within the given piece.
     *
     * @param pieceIdx the piece index
     * @param offset byte offset within the piece
     * @return the size of the block
     */
    public int blockSize(final int pieceIdx, final int offset) {
        int pieceSize = this.pieceSize(pieceIdx);
        int blockSize = pieceSize - offset;

        if (blockSize > BLOCK_SIZE) {
            return BLOCK_SIZE;
        }
        return blockSize;
    }

    /**
     *
     * @param pieceIdx the piece index
     * @return the number of blocks for the given piece
     */
    public int blockCount(final int pieceIdx) {
        if (pieceIdx != this.pieceCount - 1) {
            //Only the last piece may have a different size
            return PIECE_SIZE / BLOCK_SIZE;
        }
        int pieceSize = this.pieceSize(pieceIdx);
        int blockCnt = pieceSize / BLOCK_SIZE;
        if (pieceSize % BLOCK_SIZE != 0) {
            blockCnt++;
        }
        return blockCnt;
    }

    /**
     *
     * @return the number of pieces for this file
     */
    public int getPieceCount() {
        return this.pieceCount;
    }

    /**
     *
     * @return the number of blocks for this file
     */
    public int getBlockCount() {
        return this.blockCount;
    }

    /**
     * Below are 'dummy' utility functions that could be static but let here in case we change to non-constant
     * piece/block size.
     */
    /**
     * Convenience method to get the byte offset in file for the given piece and block offset.
     *
     * @param pieceIdx the piece index
     * @param blockOffset the offset within the piece
     * @return the byte offset within the file
     */
    public int fileOffset(final int pieceIdx, final int blockOffset) {
        return (pieceIdx * PIECE_SIZE) + blockOffset;
    }

    /**
     * Convenience method to get the block index within the piece for the given piece and byte offset.
     *
     * @param pieceIdx the piece index
     * @param blockOffset the offset within the piece
     * @return the byte offset within the file
     */
    public int blockIndex(final int pieceIdx, final int blockOffset) {
        return blockOffset / BLOCK_SIZE;
    }

    /**
     *
     * @param pieceIdx
     * @param blockNumber
     * @return
     */
//    public int blockOffset(final int pieceIdx, final int blockNumber) {
//
//    }
}
