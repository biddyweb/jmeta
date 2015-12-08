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
package org.meta.plugin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.common.OperationListener;
import org.meta.api.model.DataFile;
import org.meta.api.p2pp.GetOperation;
import org.meta.api.plugin.DownloadOperation;
import org.meta.model.files.DataFileAccessor;
import org.meta.model.files.DataFileAccessors;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.client.MetaP2PPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class in charge of downloading a file through the peer-to-peer protocol.
 *
 * The result of the download will be available using the async operation.
 *
 * For now a really naive approach is made to dispatch Get requests to peers, and should be enhanced.
 *
 * @author dyslesiq
 */
public class DownloadManager implements OperationListener<GetOperation> {

    private static final int MAX_PENDING_GET = 100;

    private final Logger logger = LoggerFactory.getLogger(DownloadManager.class);

    private final Random r = new Random();

    private final MetaP2PPClient client;

    private final DownloadOperation dop;

    private final DataFile destination;

    private final DataFileAccessor accessor;

    private final Collection<MetaPeer> peers;

    private boolean[][] piecesField;

    /**
     *
     * @param p2pp the p2pp commands client
     * @param op the download operation
     * @param peerList the peers to download from
     */
    public DownloadManager(final MetaP2PPClient p2pp, final DownloadOperation op,
            final Collection<MetaPeer> peerList) {
        this.client = p2pp;
        this.dop = op;
        this.destination = this.dop.getFile();
        if (this.destination.getURI() == null) {
            logger.warn("NULL URI!!!!!");
        }
        this.peers = peerList;
        this.accessor = DataFileAccessors.getAccessor(destination);
    }

    /**
     *
     * @param pieceIndex the piece index
     * @return true if completely received, false otherwise
     */
    private boolean isPieceComplete(final int pieceIndex) {
        boolean[] blocksField = this.piecesField[pieceIndex];

        if (blocksField == null) {
            return true;
        }
        for (boolean b : blocksField) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return true if the file has been fully downloaded, false otherwise
     */
    private boolean isFinished() {
        for (int i = 0; i < this.piecesField.length; ++i) {
            if (this.piecesField[i] != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Launch the get operations.
     */
    public void start() {
        this.piecesField = new boolean[this.accessor.getPieceCount()][];

        for (int i = 0; i < this.accessor.getPieceCount(); ++i) {
            getPiece(i);
        }
    }

    /**
     * Launch get block operations for each block in the given piece.
     *
     * @param pieceIndex the piece index
     */
    private void getPiece(final int pieceIndex) {
        int pieceBlockCount = this.accessor.blockCount(pieceIndex);
        if (this.piecesField[pieceIndex] == null) {
            this.piecesField[pieceIndex] = new boolean[pieceBlockCount];
        }
        int blockOffset = 0;

        for (int j = 0; j < pieceBlockCount; ++j) {
            int blockSize = this.accessor.blockSize(pieceIndex, blockOffset);
            getBlock(pieceIndex, blockOffset, blockSize);
            blockOffset += blockSize;
        }
    }

    /**
     *
     * @return a random peer from the pool
     */
    private MetaPeer getRandomPeer() {
        Iterator<MetaPeer> it = this.peers.iterator();
        for (int i = 0; i < r.nextInt(this.peers.size()) - 1; ++i) {
            it.next();
        }
        return it.next();
    }

    /**
     * Select a random peer to launch a get request for the given block.
     *
     * @param pieceIndex the piece index
     * @param byteOffset the byte offset within the piece
     * @param length the requested data length
     */
    private void getBlock(final int pieceIndex, final int byteOffset, final int length) {
        this.client.get(getRandomPeer(), this.destination.getHash(), pieceIndex, byteOffset, length)
                .addListener(this);
    }

    /**
     *
     * @param pieceIndex the piece index
     * @param byteOffset the byte offset within the piece
     * @param pieceHash the piece hash
     */
    private synchronized void blockComplete(final int pieceIndex, final int byteOffset,
            final MetHash pieceHash) {
        logger.debug("Block written to disk");
        if (this.piecesField[pieceIndex] != null) {
            this.piecesField[pieceIndex][this.accessor.blockIndex(pieceIndex, byteOffset)] = true;
        } else {
            logger.error("RECEIVED BLOCK OF FINISHED PIECE!!!!!!!!");
        }
        if (isPieceComplete(pieceIndex)) {
            MetHash filePieceHash = this.accessor.getPieceHashSync(pieceIndex);
            if (filePieceHash == MetHash.ZERO) {
                logger.error("PIECE HASH NOT VALID == MetHash.ZERO");
                pieceFailed(pieceIndex);
                return;
            }
            if (!filePieceHash.equals(pieceHash)) {
                logger.error("PIECE HASH VALIDATION FAILED! : " + filePieceHash + " != " + pieceHash);
                pieceFailed(pieceIndex);
                return;
            }
            piecesField[pieceIndex] = null;
            if (isFinished()) {
                dop.complete();
            }
//            this.accessor.getPieceHash(pieceIndex).addListener(new OperationListener<PieceHashOperation>() {
//
//                @Override
//                public void failed(final PieceHashOperation operation) {
//                    logger.error("Piece hash operation failed! Error: " + operation.getFailureMessage());
//                    pieceFailed(pieceIndex);
//                }
//
//                @Override
//                public void complete(final PieceHashOperation operation) {
//                    if (operation.getPieceHash().equals(pieceHash)) {
//                        //The piece is complete and integrity is verified.
//                        piecesField[pieceIndex] = null;
//                        if (isFinished()) {
//                            dop.complete();
//                        }
//                    } else {
//                        logger.error("PIECE HASH NOT VALID!!");
//                        pieceFailed(pieceIndex);
//                    }
//                }
//            });
        }
    }

    /**
     * Called in case the p2pp get operation or the write to file operation failed.
     *
     * @param pieceIndex the piece index of the failed block
     * @param byteOffset the byte offset within the piece
     */
    private void blockFailed(final int pieceIndex, final int byteOffset, final int length) {
        logger.error("BLOCK FAILED.");
        //If a get block operation failed, try to fetch it from a different peer
        //getBlock(pieceIndex, byteOffset, length);
    }

    /**
     * Called in case the given piece failed integrity check.
     *
     * @param pieceIndex the piece index
     */
    private void pieceFailed(final int pieceIndex) {
        logger.error("PIECE FAILED: " + pieceIndex);
        //Invalidate each block first
        for (int i = 0; i < this.piecesField[pieceIndex].length; ++i) {
            this.piecesField[pieceIndex][i] = false;
        }
        //getPiece(pieceIndex);
    }

    @Override
    public void failed(final GetOperation operation) {
        logger.warn("Get block operation failed! Error: " + operation.getFailureMessage());
        blockFailed(operation.getPieceIndex(), operation.getByteOffset(), operation.getDataLength());
    }

    @Override
    public void complete(final GetOperation operation) {
        if (operation.getDataLength() != operation.getData().remaining()) {
            logger.error("INVALID BUFFER FROM GET OPERATION");
            return;
        }
        boolean res = this.accessor.writeSync(operation.getPieceIndex(),
                operation.getByteOffset(), operation.getData());

        if (!res) {
            logger.error("Write operation to file failed!");
            blockFailed(operation.getPieceIndex(),
                    operation.getByteOffset(), operation.getDataLength());
        } else {
            blockComplete(operation.getPieceIndex(), operation.getByteOffset(),
                    operation.getPieceHash());
        }
        BufferManager.release(operation.getData());
//        this.accessor.write(operation.getPieceIndex(), operation.getByteOffset(), operation.getData())
//                .addListener(new OperationListener<FileWriteOperation>() {
//
//                    @Override
//                    public void failed(final FileWriteOperation fwOperation
//                    ) {
//                        BufferManager.release(operation.getData());
//                        logger.error("Write operation to file failed! Error: "
//                                + fwOperation.getFailureMessage());
//                        blockFailed(operation.getPieceIndex(),
//                                operation.getByteOffset(), operation.getDataLength());
//                    }
//
//                    @Override
//                    public void complete(final FileWriteOperation fwOperation
//                    ) {
//                        BufferManager.release(operation.getData());
//                        blockComplete(operation.getPieceIndex(), operation.getByteOffset(),
//                                operation.getPieceHash());
//                    }
//                }
//                );
    }

}
