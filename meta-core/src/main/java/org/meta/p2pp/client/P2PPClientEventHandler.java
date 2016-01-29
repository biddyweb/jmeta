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

import java.nio.channels.AsynchronousSocketChannel;
import org.meta.api.common.MetaPeer;

/**
 *
 * @author dyslesiq
 */
public interface P2PPClientEventHandler {

    /**
     * Enum representing possible actions a P2PPClientEventHandler can do.
     */
    enum ClientAction {
        /**
         * Connect to the server peer.
         */
        CONNECT,
        /**
         * Read on the socket.
         */
        READ,
        /**
         * Write to the socket.
         */
        WRITE,
        /**
         * Executes a request completion handlers.
         */
        COMPLETE_REQUEST,
        /**
         * There was an error in the processing of the event.
         */
        ERROR,
        /**
         * No action needed.
         */
        NONE
    };

    /**
     * Not implemented/used yet. Will list possible error states that we want to handle at the EventHandler
     * level.
     */
    enum IOError {

    };

    /**
     *
     */
    class ClientActionContext {

        private final P2PPClientEventHandler eventHandler;

        private ClientAction action;

        private Object attachment;

        private ClientActionContext next;

        ClientActionContext(final P2PPClientEventHandler handler) {
            this.eventHandler = handler;
        }

        ClientActionContext(final P2PPClientEventHandler handler,
                final ClientAction eventAction) {
            this.eventHandler = handler;
            this.action = eventAction;
        }

        /**
         *
         * @return the event handler that wants to be notified once this operation finishes.
         */
        P2PPClientEventHandler getEventHandler() {
            return this.eventHandler;
        }

        /**
         *
         * @param eventAction the action
         * @return this for convenience
         */
        ClientActionContext setAction(final ClientAction eventAction) {
            this.action = eventAction;
            return this;
        }

        /**
         *
         * @return the action
         */
        public ClientAction getAction() {
            return action;
        }

        /**
         * Set the object that the action will operate on.
         *
         * @param obj the object
         * @return this for convenience
         */
        ClientActionContext setAttachment(final Object obj) {
            this.attachment = obj;
            return this;
        }

        /**
         * Convenience get of the action attachment that provides auto-casting.
         *
         * @param <T> dynamic type of the attachment
         * @return the action attachment
         */
        <T> T getAttachment() {
            return (T) attachment;
        }

        /**
         *
         * @param nextAction the next action
         * @return this for convenience
         */
        ClientActionContext next(final ClientActionContext nextAction) {
            this.next = nextAction;
            return this;
        }

        ClientActionContext next() {
            return this.next;
        }

    }

    /**
     *
     * @return the server peer
     */
    MetaPeer getServerPeer();

    /**
     *
     * @return the socket
     */
    AsynchronousSocketChannel getSocket();

    /**
     * Handles the 'connecting socket' event.
     *
     * @param socket the created socket being connected
     */
    void connecting(final AsynchronousSocketChannel socket);

    /**
     * Handles the 'socket connected' event.
     *
     * @param ioContext the context of the event
     * @return the resulting action context
     */
    ClientActionContext connected(final ClientActionContext ioContext);

    /**
     * Handles the 'data received' event.
     *
     * The given I/O action context can be safely re-used.
     *
     * @param ioContext the context of the event
     * @return the resulting action context
     */
    ClientActionContext dataReceived(final ClientActionContext ioContext);

    /**
     * Handles the 'data sent' event.
     *
     * The given I/O action context can be safely re-used.
     *
     * @param ioContext the context of the event
     * @return the resulting action context
     */
    ClientActionContext dataSent(final ClientActionContext ioContext);

    /**
     *
     * @return the resulting action context
     */
    ClientActionContext error();

    /**
     * Closes this socket context. Clean up resources, releasing buffers,...
     */
    void close();

}
