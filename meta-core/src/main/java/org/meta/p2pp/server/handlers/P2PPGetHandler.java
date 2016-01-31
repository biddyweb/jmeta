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
package org.meta.p2pp.server.handlers;

import java.nio.ByteBuffer;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;
import org.meta.api.common.OperationListener;
import org.meta.api.model.DataFile;
import org.meta.model.files.DataFileAccessor;
import org.meta.model.files.DataFileAccessors;
import org.meta.model.files.FileReadOperation;
import org.meta.model.files.PieceHashOperation;
import org.meta.p2pp.BufferManager;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.ServerRequestStatus;
import org.meta.p2pp.server.P2PPCommandHandler;
import org.meta.p2pp.server.P2PPServer;
import org.meta.p2pp.server.P2PPServerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The Get request handler for the P2PP server.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class P2PPGetHandler extends P2PPCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPGetHandler.class);

    //private P2PPServerClientContext clientContext;
    private P2PPServerRequestContext request;

    private MetHash requestedDataHash;

    private int pieceIdx;

    private int byteOffset;

    private int dataLength;

    private ByteBuffer getBuffer;

    private MetHash pieceHash;

    //private GetBlockListener readBlockListener;
    private ByteBuffer responseBuffer;

    /**
     * <p>Constructor for P2PPGetHandler.</p>
     *
     * @param p2ppServer the p2ppServer
     */
    public P2PPGetHandler(final P2PPServer p2ppServer) {
        super(p2ppServer);
    }

    /** {@inheritDoc} */
    @Override
    public void handle(final P2PPServerRequestContext req) {
        this.request = req;
        logger.debug("handle get request");

        if (!this.parse()) {
            logger.debug("Failed to parse request.");
            requestError();
        } else {
            this.prepareResponse();
            if (this.request.getStatus() != ServerRequestStatus.DISCARDED) {
                buildResponse();
            }
        }
    }

    private void requestError() {
        this.request.setStatus(ServerRequestStatus.DISCARDED);
    }

    /**
     * Parses the get request content.
     *
     * @return true on success, false otherwise
     */
    protected boolean parse() {
        ByteBuffer buf = request.getDataBuffer();
        buf.rewind();
        short sizeofHash = buf.getShort();
        this.requestedDataHash = new MetHash(buf, sizeofHash);
        this.pieceIdx = buf.getInt();
        this.byteOffset = buf.getInt();
        this.dataLength = buf.getInt();
        return true;
    }

    private void prepareResponse() {
        DataFile dataFile = this.server.getStorage().getDataFile(requestedDataHash);

        if (dataFile == null) {
            logger.warn("Asked data file inexistent in DB.");
            this.requestError();
            return;
        }
        DataFileAccessor dfAccessor = DataFileAccessors.getAccessor(dataFile);
        if (this.pieceIdx >= dfAccessor.getPieceCount()) {
            logger.warn("Invalid piece number");
            this.requestError();
            return;
        }
        if (this.dataLength > DataFileAccessor.BLOCK_SIZE
                || this.byteOffset + this.dataLength > dfAccessor.pieceSize(pieceIdx)) {
            logger.warn("Invalid block offset or block size");
            this.requestError();
            return;
        }
        this.pieceHash = dfAccessor.getPieceHashSync(pieceIdx);
        if (this.pieceHash == MetHash.ZERO) {
            logger.error("Piece hash failed ( ==MetHash.ZERO )");
            requestError();
            return;
        }
        this.getBuffer = dfAccessor.readSync(dfAccessor.fileOffset(pieceIdx, byteOffset), dataLength);
        if (this.getBuffer == null) {
            requestError();
        }
    }

    private void buildResponse() {
        int responseSize = Short.BYTES + MetHash.BYTE_ARRAY_SIZE + this.dataLength;
        logger.debug("Build response, response length = " + responseSize);
        responseBuffer = BufferManager.aquireDirectBuffer(responseSize + P2PPConstants.RESPONSE_HEADER_SIZE);

        responseBuffer.putShort((short) this.request.getToken());
        responseBuffer.put((byte) 0); //Remaining frames, unused for now
        responseBuffer.putInt(responseSize);
        responseBuffer.putShort((short) MetHash.BYTE_ARRAY_SIZE);
        responseBuffer.put(this.pieceHash.toByteArray());
        responseBuffer.put(getBuffer);
        this.responseBuffer.rewind();
        this.request.setResponseBuffer(responseBuffer);
    }

    /**
     * Operation listener for the asynchronous read block operation.
     */
    class GetBlockListener implements OperationListener<AsyncOperation> {

        private FileReadOperation op;

        private MetHash pieceHash;

        private boolean hashReceived = false;

        private boolean dataReceived = false;

        @Override
        public void failed(final AsyncOperation operation) {
            logger.warn("GetBlockListener: block read or piece hash failed:" + operation.getFailureMessage());
            requestError();
        }

        @Override
        public void complete(final AsyncOperation operation) {
            if (operation instanceof FileReadOperation) {
                logger.debug("P2PP GET HANDLER GetBlockListener: RECEIVED DATA BUFFER     !!!!!");
                this.op = (FileReadOperation) operation;
                dataReceived = true;
            } else if (operation instanceof PieceHashOperation) {
                logger.debug("P2PP GET HANDLER GetBlockListener: RECEIVED PIECE HASH      !!!!!");
                this.pieceHash = ((PieceHashOperation) operation).getPieceHash();
                hashReceived = true;
            }
            if (hashReceived && dataReceived) {
                buildResponse();
            }
        }

        /**
         *
         * @return the operation
         */
        public FileReadOperation getOperation() {
            return op;
        }

        /**
         *
         * @return the requested piece hash
         */
        public MetHash getPieceHash() {
            return pieceHash;
        }
    }

}
