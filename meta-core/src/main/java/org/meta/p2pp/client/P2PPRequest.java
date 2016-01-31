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
import org.meta.api.common.MetaPeer;
import org.meta.p2pp.P2PPConstants.ClientRequestStatus;
import org.meta.p2pp.P2PPConstants.P2PPCommand;

/**
 * Abstract class representing a peer-to-peer protocol request on the client side.
 *
 * To be implemented by each command in the P2P protocol.
 *
 * @author dyslesiq
 * @version $Id: $
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
    protected volatile ClientRequestStatus status;

    /**
     * The unique token of this request.
     */
    protected char token;

    /**
     * The server peer this request is headed to.
     */
    protected MetaPeer peer;

    /**
     * <p>Constructor for P2PPRequest.</p>
     *
     * @param id the request command id
     * @param p2ppClient the peer-to-peer protocol client
     * @param serverPeer a {@link org.meta.api.common.MetaPeer} object.
     */
    protected P2PPRequest(final P2PPCommand id, final P2PPClient p2ppClient, final MetaPeer serverPeer) {
        this.status = ClientRequestStatus.CREATED;
        this.commandId = id;
        this.client = p2ppClient;
        this.peer = serverPeer;
    }

    /**
     * <p>getResponseHandler</p>
     *
     * @return the specific response handler for this request.
     *
     * Could be null, in which case the request won't wait for a response from the server.
     */
    public abstract P2PPResponseHandler getResponseHandler();

    /**
     * <p>getOperation</p>
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
    public abstract boolean build(final char requestToken);

    /**
     * <p>hasResponse</p>
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
     * <p>setFailed</p>
     *
     * @param failedReason the reason why this request failed.
     */
    public abstract void setFailed(final String failedReason);

    /**
     * <p>setFailed</p>
     *
     * @param thrwbl the reason why this request failed.
     */
    public abstract void setFailed(final Throwable thrwbl);

    /**
     * <p>Getter for the field <code>commandId</code>.</p>
     *
     * @return the command id of this request
     */
    public final P2PPCommand getCommandId() {
        return this.commandId;
    }

    /**
     * <p>Getter for the field <code>buffer</code>.</p>
     *
     * @return the buffer representing the protocol data of this request. Callable only once build() has
     * succeeded
     */
    public final ByteBuffer getBuffer() {
        return this.buffer;
    }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return the current status of this request
     */
    public final ClientRequestStatus getStatus() {
        return this.status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param newStatus the new status of this request
     */
    public void setStatus(final ClientRequestStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * <p>Getter for the field <code>token</code>.</p>
     *
     * @return the unique token of this request
     */
    public final char getToken() {
        return this.token;
    }

    /**
     * <p>Getter for the field <code>client</code>.</p>
     *
     * @return the peer-to-peer protocol client
     */
    public final P2PPClient getClient() {
        return this.client;
    }

}
