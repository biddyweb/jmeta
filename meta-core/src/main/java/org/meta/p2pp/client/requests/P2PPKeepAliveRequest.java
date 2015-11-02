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
package org.meta.p2pp.client.requests;

import java.nio.ByteBuffer;
import org.meta.api.common.AsyncOperation;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPConstants.P2PPCommand;
import org.meta.p2pp.client.P2PPRequest;
import org.meta.p2pp.client.P2PPResponseHandler;

/**
 * The keep-alive request implementation.
 *
 * This is the most simple implementation of a request.
 *
 * @author dyslesiq
 */
public class P2PPKeepAliveRequest extends P2PPRequest {

    private final AsyncOperation operation;

    /**
     * Default constructor.
     */
    public P2PPKeepAliveRequest() {
        //Giving null as p2p client because this request does not need it...
        super(P2PPCommand.KEEP_ALIVE, null);
        this.buffer = ByteBuffer.allocateDirect(P2PPConstants.REQUEST_HEADER_SIZE);
        //placeholder for the token
        this.buffer.position(Short.BYTES);
        this.buffer.put(this.commandId.getValue());
        //Hard-coded request payload size of zero.
        this.buffer.putInt(0);
        this.operation = new DummyAsyncOperation();
    }

    @Override
    public P2PPResponseHandler getResponseHandler() {
        return null;
    }

    @Override
    public boolean build(final short buildToken) {
        this.token = buildToken;
        this.buffer.rewind();
        this.buffer.putShort(this.token);
        this.buffer.rewind();
        return true;
    }

    @Override
    public P2PPConstants.ClientRequestStatus dataReceived() {
        return this.status;
    }

    @Override
    public boolean hasResponse() {
        return false;
    }

    @Override
    public void finish() {
        operation.complete();
    }

    @Override
    public void setFailed(final String failedReason) {
        this.operation.setFailed(failedReason);
    }

    @Override
    public void setFailed(final Throwable thrwbl) {
        this.operation.setFailed(thrwbl);
    }

    @Override
    public AsyncOperation getOperation() {
        return this.operation;
    }

}
