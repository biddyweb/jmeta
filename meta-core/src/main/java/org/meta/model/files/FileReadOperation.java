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

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import org.meta.api.common.AsyncOperation;
import org.meta.p2pp.BufferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous operation representing the read of a data block from a file.
 *
 * @author dyslesiq
 */
public class FileReadOperation extends AsyncOperation implements CompletionHandler<Integer, Void> {

    private final Logger logger = LoggerFactory.getLogger(FileReadOperation.class);

    private final int length;
    private final int fileOffset;

    private final ByteBuffer readData;

    /**
     * @param offset the byte offset within the file
     * @param dataLength the data length to read
     */
    public FileReadOperation(final int offset, final int dataLength) {
        this.fileOffset = offset;
        this.length = dataLength;
        this.readData = BufferManager.createDirectBuffer(length);
    }

    /**
     *
     * @return the buffer holding read data
     */
    public ByteBuffer getBuffer() {
        return this.readData;
    }

    /**
     *
     * @return the file offset of the requested data
     */
    public int getFileOffset() {
        return fileOffset;
    }

    /**
     *
     * @return the requested length
     */
    public int getLength() {
        return length;
    }

    @Override
    public void completed(final Integer bytes, final Void a) {
        logger.debug("read file comleted, bytes = " + bytes);
        if (bytes == -1) {
            this.setFailed("Reached EOF or I/O issue.");
        }
        if (!this.readData.hasRemaining()) {
            this.readData.rewind();
            this.complete();
        } else {
            this.setFailed("Completed but buffer still has remaining!");
        }
    }

    @Override
    public void failed(final Throwable thrwbl, final Void a) {
        this.setFailed(thrwbl);
    }

}
