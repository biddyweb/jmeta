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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.common.OperationListener;
import org.meta.api.model.DataFile;
import org.meta.p2pp.BufferManager;
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

    private FileChannel readSyncChannel;

    private FileChannel writeSyncChannel;

    private AsynchronousFileChannel readChannel;

    private AsynchronousFileChannel writeChannel;

    /**
     *
     * @param file the data file to access
     */
    public DataFileAccessor(final DataFile file) {
        this.dataFile = file;

        this.pieceCount = this.dataFile.getSize() / PIECE_SIZE;
        if (this.dataFile.getSize() % PIECE_SIZE != 0) {
            this.pieceCount++;
        }

        this.blockCount = this.dataFile.getSize() / BLOCK_SIZE;
        if (this.dataFile.getSize() % BLOCK_SIZE != 0) {
            this.blockCount++;
        }
        this.piecesHash = new MetHash[this.pieceCount];
        try {
            checkWrite();
            checkRead();
        } catch (final IOException ex) {
            logger.error("check (read,write) failed: ", ex);
        }
    }

    /**
     * Ensures that the file can be read.
     *
     * @throws IOException if not
     */
    private void checkRead() throws IOException {
        if (!this.dataFile.getFile().exists()) {
            throw new IOException("Cannot read: file doesn't exists!");
        }
        if (!dataFile.getFile().canRead()) {
            throw new IOException("Cannot read: insufficient right!");
        }
        if (this.readChannel == null) {
            readChannel = AsynchronousFileChannel.open(dataFile.getFile().toPath(),
                    Collections.singleton(StandardOpenOption.READ), DataFileAccessors.getFilesExecutor());
        }
        if (this.readSyncChannel == null) {
            this.readSyncChannel = FileChannel.open(dataFile.getFile().toPath(), StandardOpenOption.READ);
            //this.readSyncChannel.force(true);
        }
    }

    /**
     * Ensures that the file can be written and creates writable channels.
     *
     * @throws IOException if not
     */
    private void checkWrite() throws IOException {
        if (!this.dataFile.getFile().exists()) {
            this.createFile();
        } else {
            //TODO set file size if it already exists
            if (!this.dataFile.getFile().canWrite()) {
                throw new IOException("Cannot write: insufficient right!");
            }
            if (this.dataFile.getSize() < this.dataFile.getFile().length()) {
                throw new IOException("Cannot write: file is already larger than given data file!");
            }
        }
        if (this.writeChannel == null) {
            writeChannel = AsynchronousFileChannel.open(dataFile.getFile().toPath(),
                    Collections.singleton(StandardOpenOption.WRITE), DataFileAccessors.getFilesExecutor());
        }
        if (this.writeSyncChannel == null) {
            this.writeSyncChannel = FileChannel.open(this.dataFile.getFile().toPath(),
                    StandardOpenOption.READ, StandardOpenOption.WRITE);
            //this.writeSyncChannel.force(true);
        }
    }

    /**
     * Creates the file and set its length to the expected length.
     *
     * After this call the actual FileSystem size of the file might be inferior (sparse file).
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void createFile() throws FileNotFoundException, IOException {
        this.dataFile.getFile().createNewFile();
        try (RandomAccessFile raf = new RandomAccessFile(this.dataFile.getFile(), "rw")) {
            raf.setLength(this.dataFile.getSize());
        }
    }

    /**
     * Read data from a file asynchronously.
     *
     * @param offset the offset in the file
     * @param length the number of bytes to read
     * @return the asynchronous operation
     */
    public FileReadOperation read(final int offset, final int length) {
        FileReadOperation operation = new FileReadOperation(offset, length);

        readChannel.read(operation.getBuffer(), offset, null, operation);
        return operation;
    }

    /**
     * Read data from a file synchronously using memory mapped file.
     *
     * @param offset the offset in the file
     * @param length the number of bytes to read
     * @return the read buffer, or null if an error occurred
     */
    public ByteBuffer readSync(final int offset, final int length) {
        try {
            MappedByteBuffer mappedBuffer = this.readSyncChannel.map(FileChannel.MapMode.READ_ONLY,
                    offset, length);
            return mappedBuffer;
        } catch (final IOException ex) {
            logger.error("Failed to map for read.", ex);
            return null;
        }
    }

    /**
     * Write data to a file synchronously using memory mapped file.
     *
     * @param pieceIndex the piece index
     * @param byteOffset the offset in the piece
     * @param buf the buffer to write at given position
     * @return true on success, false if an error occurred
     */
    public boolean writeSync(final int pieceIndex, final int byteOffset, final ByteBuffer buf) {
        try {
            int writeOffset = fileOffset(pieceIndex, byteOffset);
            MappedByteBuffer writeBuffer = this.writeSyncChannel.map(FileChannel.MapMode.READ_WRITE,
                    writeOffset, buf.remaining());
            writeBuffer.put(buf);
            return true;
        } catch (IOException ex) {
            logger.error("Failed to map for write.", ex);
            return false;
        }
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
    public FileWriteOperation write(final int pieceIndex, final int byteOffset,
            final ByteBuffer buf) {
        FileWriteOperation op = new FileWriteOperation(pieceIndex, byteOffset, buf);

        int writeOffset = fileOffset(pieceIndex, byteOffset);
        writeChannel.write(buf, writeOffset, null, op);
        return op;
    }

    /**
     * Get the hash of the given piece in the file asynchronously.
     *
     * @param pieceIndex the index of the piece to hash
     * @return the async operation
     */
    public PieceHashOperation getPieceHash(final int pieceIndex) {
        PieceHashOperation op = new PieceHashOperation(pieceIndex);

        synchronized (piecesHash) {
            if (piecesHash[pieceIndex] != null) {
                op.setPieceHash(piecesHash[pieceIndex]);
                op.complete();
                return op;
            }
        }
        //Read the whole piece
        FileReadOperation readOp = this.read(this.fileOffset(pieceIndex, 0), this.pieceSize(pieceIndex));
        readOp.addListener(new OperationListener<FileReadOperation>() {

            @Override
            public void failed(final FileReadOperation operation) {
                op.setFailed(operation.getFailureMessage());
            }

            @Override
            public void complete(final FileReadOperation operation) {
                synchronized (piecesHash) {
                    piecesHash[pieceIndex] = MetamphetUtils.makeSHAHash(operation.getBuffer());
                }
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
    public MetHash getPieceHashSync(final int pieceIndex) {
        if (this.piecesHash[pieceIndex] == null) {
            ByteBuffer buff = this.readSync(fileOffset(pieceIndex, 0), pieceSize(pieceIndex));
            this.piecesHash[pieceIndex] = MetamphetUtils.makeSHAHash(buff);
            BufferManager.release(buff);
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

}
