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

/**
 *
 * Asynchronous operation representing the write of a data block from a file.
 *
 * @author dyslesiq
 */
public class FileWriteOperation extends AsyncOperation implements CompletionHandler<Integer, Void> {

    final int pieceIndex;
    final int byteOffset;
    final ByteBuffer buffer;

    /**
     *
     * @param pieceIdx the piece index
     * @param offset
     * @param buf
     */
    public FileWriteOperation(final int pieceIdx, final int offset, final ByteBuffer buf) {
        this.pieceIndex = pieceIdx;
        this.byteOffset = offset;
        this.buffer = buf;
    }

    @Override
    public void completed(final Integer v, final Void a) {
        this.complete();
    }

    @Override
    public void failed(final Throwable thrwbl, final Void a) {
        this.setFailed(thrwbl);
    }

}
