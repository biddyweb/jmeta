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
package org.meta.p2pp.server;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * <p>P2PPServerEventHandler interface.</p>
 *
 * @author dyslesiq
 * @version $Id: $
 */
public interface P2PPServerEventHandler {

    /**
     * Enum representing possible actions on I/O channels.
     */
    enum ServerEventAction {
        /**
         * Read on the socket.
         */
        READ,
        /**
         * Write to the socket.
         */
        WRITE,
        /**
         * Executes the handler for a received request.
         */
        DISPATCH,
        /*
         * There was an error in the processing of the event.
         */
        ERROR,
        /**
         * No action needed.
         */
        NONE
    };

    /**
     * Structure-like class that embeds an ServerEventAction and the associated buffer if needed.
     *
     * This also provides the ability to chain actions.
     */
    class ServerActionContext {

        private final P2PPServerEventHandler eventHandler;

        private ServerEventAction action;

        private Object attachment;

        private ServerActionContext next;

        ServerActionContext(final P2PPServerEventHandler ioEventHandler) {
            this.eventHandler = ioEventHandler;
        }

        ServerActionContext(final P2PPServerEventHandler ioEventHandler, final ServerEventAction action) {
            this.eventHandler = ioEventHandler;
            this.action = action;
        }

        /**
         *
         * @return the action of this context
         */
        ServerEventAction getAction() {
            return this.action;
        }

        /**
         *
         * @param eventAction the action
         * @return this for convenience
         */
        ServerActionContext setAction(final ServerEventAction eventAction) {
            this.action = eventAction;
            return this;
        }

        /**
         * Set the object that the action will operate on.
         *
         * @param obj the object
         * @return this for convenience
         */
        ServerActionContext setAttachment(final Object obj) {
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
        ServerActionContext next(final ServerActionContext nextAction) {
            this.next = nextAction;
            return this;
        }

        /**
         *
         * @return the next action context, or null
         */
        ServerActionContext next() {
            return this.next;
        }

        /**
         *
         * @return the event handler of this context
         */
        P2PPServerEventHandler getHandler() {
            return this.eventHandler;
        }
    }

    /**
     * <p>getSocket</p>
     *
     * @return the socket
     */
    AsynchronousSocketChannel getSocket();

    /**
     * Called on client connection, when the socket is successfully accepted.
     *
     * @param socket the accepted socket
     * @return the resulting action context
     */
    ServerActionContext accepted(final AsynchronousSocketChannel socket);

    /**
     * Handles the 'data received' event.
     *
     * The given I/O action context can be safely re-used.
     *
     * @param context the context of the event
     * @return the resulting action context
     */
    ServerActionContext dataReceived(final ServerActionContext context);

    /**
     * Handles the 'data sent' event.
     *
     * The given I/O action context can be safely re-used.
     *
     * @param context the context of the event
     * @return the resulting action context
     */
    ServerActionContext dataSent(final ServerActionContext context);

    /**
     * Called when the handler has finished processing a request.
     *
     * @param context the context of the event
     * @return the resulting action context
     */
    ServerActionContext handlerComplete(final ServerActionContext context);

    /**
     * <p>error</p>
     *
     * @return the resulting action context
     */
    ServerActionContext error();

    /**
     * Closes this socket context. Clean up resources, releasing buffers,...
     */
    void close();

}
