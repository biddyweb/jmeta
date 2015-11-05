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
package org.meta.p2pp.client;

import java.nio.ByteBuffer;
import org.meta.api.common.AsyncOperation;
import org.meta.p2pp.P2PPConstants.ClientRequestStatus;
import org.meta.p2pp.P2PPConstants.P2PPCommand;

/**
 * Abstract class representing a peer-to-peer protocol request on the client side.
 *
 * To be implemented by each command in the P2P protocol.
 *
 * @author dyslesiq
 */
public abstract class P2PPRequest {

    /**
     *
     */
    protected final P2PPClient client;

    /**
     * The request command id.
     */
    protected final P2PPCommand commandId;

    /**
     * The buffer holding the request protocol data.
     */
    protected ByteBuffer buffer;

    /**
     * The status of this request.
     */
    protected ClientRequestStatus status;

    /**
     * The unique token of this request.
     */
    protected short token;

    /**
     *
     * @param id the request command id
     * @param p2ppClient the peer-to-peer protocol client
     */
    protected P2PPRequest(final P2PPCommand id, final P2PPClient p2ppClient) {
        this.status = ClientRequestStatus.CREATED;
        this.commandId = id;
        this.client = p2ppClient;
    }

    /**
     * @return the specific response handler for this request.
     *
     * Could be null, in which case the request won't wait for a response from the server.
     */
    public abstract P2PPResponseHandler getResponseHandler();

    /**
     *
     * @return the asynchronous operation representing the outcome of this request.
     */
    public abstract AsyncOperation getOperation();

    /**
     * Creates the data buffer for this request.
     *
     * Called by the P2PP client when starting to send the request to the server peer.
     *
     * Implementations should take care of disposing of any temporary objects after this call for less
     * resource usage.
     *
     * @param requestToken the unique token of this request
     * @return true is successfully built, false otherwise
     */
    public abstract boolean build(final short requestToken);

    /**
     *
     * @return true if this request expect a response from the server, false otherwise
     */
    public abstract boolean hasResponse();

    /**
     * Called on request completion.
     *
     * The request isn't used anymore after this method call.
     */
    public abstract void finish();

    /**
     *
     * @param failedReason the reason why this request failed.
     */
    public abstract void setFailed(final String failedReason);

    /**
     *
     * @param thrwbl the reason why this request failed.
     *
     */
    public abstract void setFailed(final Throwable thrwbl);

    /**
     *
     * @return the command id of this request
     */
    public final P2PPCommand getCommandId() {
        return this.commandId;
    }

    /**
     *
     * @return the buffer representing the protocol data of this request. Callable only once build() has
     * succeeded
     */
    public final ByteBuffer getBuffer() {
        return this.buffer;
    }

    /**
     *
     * @return the current status of this request
     */
    public final ClientRequestStatus getStatus() {
        return this.status;
    }

    /**
     *
     * @param newStatus the new status of this request
     */
    public void setStatus(final ClientRequestStatus newStatus) {
        this.status = newStatus;
    }

    /**
     *
     * @return the unique token of this request
     */
    public final short getToken() {
        return this.token;
    }

    /**
     *
     * @return the peer-to-peer protocol client
     */
    public final P2PPClient getClient() {
        return this.client;
    }

}
