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

import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;

/**
 *
 * Asynchronous operation representing the write of a data block from a file.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class PieceHashOperation extends AsyncOperation {

    private final int pieceIndex;

    private MetHash pieceHash;

    /**
     * <p>Constructor for PieceHashOperation.</p>
     *
     * @param pieceIdx the piece index
     */
    public PieceHashOperation(final int pieceIdx) {
        this.pieceIndex = pieceIdx;
    }

    /**
     * <p>Getter for the field <code>pieceIndex</code>.</p>
     *
     * @return the piece index
     */
    public int getPieceIndex() {
        return pieceIndex;
    }

    /**
     * <p>Getter for the field <code>pieceHash</code>.</p>
     *
     * @return the piece hash
     */
    public MetHash getPieceHash() {
        return pieceHash;
    }

    /**
     * <p>Setter for the field <code>pieceHash</code>.</p>
     *
     * @param hash the piece hash
     */
    public void setPieceHash(final MetHash hash) {
        this.pieceHash = hash;
    }

}
