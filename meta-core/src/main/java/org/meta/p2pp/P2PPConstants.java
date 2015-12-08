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
package org.meta.p2pp;

/**
 * Utility class holding constants for the Peer-to-Peer protocol.
 *
 * Some of these constants should be moved to a P2PP configuration class.
 *
 * @author dyslesiq
 */
public final class P2PPConstants {

    /**
     * Private ctor for static class.
     */
    private P2PPConstants() {
    }

    /**
     * List of possible commands in the peer-to-peer protocol.
     */
    public enum P2PPCommand {

        /**
         * The keep-alive request.
         */
        KEEP_ALIVE((byte) 0),
        /**
         * Search request.
         */
        SEARCH((byte) 1),
        /**
         * Search with meta-data request.
         */
        SEARCH_META((byte) 2),
        /**
         * Search with meta-data and data request.
         */
        SEARCH_GET((byte) 3),
        /**
         * Get block of data request.
         */
        GET((byte) 4),
        /**
         * Plugin extension request.
         */
        PLUGIN_COMMAND((byte) 254),
        /**
         * Special use, invalid request.
         */
        INVALID((byte) 255);

        private byte commandId;

        P2PPCommand(final byte id) {
            this.commandId = id;
        }

        /**
         *
         * @return the byte value of this enum.
         */
        public byte getValue() {
            return commandId;
        }

        /**
         *
         * @param value the byte value to convert to command
         * @return the associated command, or {@code P2PPCommand.INVALID} if invalid.
         */
        public static P2PPCommand fromValue(final byte value) {
            try {
                return P2PPCommand.values()[value];
            } catch (ArrayIndexOutOfBoundsException e) {
                return INVALID;
            }
        }
    }

    /**
     * Enum representing the state of a request made to the server over its lifespan.
     */
    public enum ServerRequestStatus {

        /**
         * The request has been created.
         */
        CREATED,
        /**
         * The request header is being read.
         */
        HEADER_PENDING,
        /**
         * The request header has been received.
         */
        HEADER_RECEIVED,
        /**
         * The request content is being read.
         */
        DATA_PENDING,
        /**
         * The request has been fully received and can be dispatched.
         */
        DATA_RECEIVED,
        /**
         * The request has been dispatched to the command handler.
         */
        DISPTACHED,
        /**
         * The handler has finished and response data is ready.
         */
        RESPONSE_READY,
        /**
         * Request has been treated and the response is being sent.
         */
        RESPONSE_PENDING,
        /**
         * The request has been treated entirely. (buffers can be destroyed)
         */
        FINISHED,
        /**
         * The request has been declared invalid.
         */
        DISCARDED
    }

    /**
     * Enum representing the state of a request made by the client over its lifespan.
     */
    public enum ClientRequestStatus {

        /**
         * The request has just been created.
         */
        CREATED,
        /**
         * The request buffer has been built and is ready to be sent.
         */
        BUILT,
        /**
         * The request is being sent to the server.
         */
        SEND_PENDING,
        /**
         * The request has been sent to the server.
         */
        SEND_COMPLETE,
        /**
         * The request is waiting for the response header from the server.
         */
        RESPONSE_HEADER_PENDING,
        /**
         * The response header has been received.
         */
        RESPONSE_HEADER_RECEIVED,
        /**
         * The request is waiting for the response payload from the server.
         */
        RESPONSE_PENDING,
        /**
         * The response has been fully received.
         */
        RESPONSE_RECEIVED,
        /**
         * The server responded to the request and has been handled completely.
         */
        FINISHED,
        /**
         * The request, or associated response is considered invalid.
         */
        INVALID

    }

    /**
     * The size of a request header = 7.
     */
    public static final int REQUEST_HEADER_SIZE = Short.BYTES + Byte.BYTES + Integer.BYTES;

    /**
     * The size of a response header = 7.
     */
    public static final int RESPONSE_HEADER_SIZE = Short.BYTES + Byte.BYTES + Integer.BYTES;

    /**
     * A P2PP request should not exceed that size.
     */
    public static final int MAX_REQUEST_DATA_SIZE = 16 * 1024;

    /**
     * A P2PP response should not exceed that size.
     */
    public static final int MAX_RESPONSE_SIZE = 64 * 1024;

    /**
     * The number seconds after which a socket with no read event will be considered invalid. TODO define
     * correctly.
     */
    public static final int READ_TIMEOUT = 120;

    /**
     * The number seconds after which a write on a socket will be considered invalid. TODO define correctly
     */
    public static final int WRITE_TIMEOUT = 30;

    /**
     * The maximum number of pending requests on the same client wire. TODO define correctly
     */
    public static final int CONCURRENT_CLIENT_REQUESTS = 30;

}
