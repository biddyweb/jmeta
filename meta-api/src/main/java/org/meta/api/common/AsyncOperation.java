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
package org.meta.api.common;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base interface representing a stateful asynchronous operation.
 *
 * @author nico
 */
public abstract class AsyncOperation {

    /**
     * Represents the state of the async operation.
     */
    public enum OperationState {

        /**
         * The operation has not yet started.
         */
        INIT,
        /**
         * The operation is still in progress.
         */
        WAITING,
        /**
         * The operation has completed successfully.
         */
        COMPLETE,
        /**
         * The operation failed.
         */
        FAILED,
        /**
         * Cancel was called on this operation.
         */
        CANCELED
    };

    /**
     * The lock object.
     */
    protected final Object lock = new Object();

    /**
     * The Throwable that make the operation failed.
     */
    protected Throwable failedReason;

    /**
     * The failure message.
     */
    protected String failedMessage;

    /**
     * The current state of the operation.
     */
    protected OperationState state;

    /**
     * The listeners of this operation.
     */
    protected Collection<OperationListener<? extends AsyncOperation>> listeners;

    /**
     * Parent constructor.
     */
    public AsyncOperation() {
        this.state = OperationState.INIT;
        this.listeners = new ArrayList<>();
    }

    /**
     * For internal use, set the state in a thread-safe manner.
     *
     * @param operationState The new state.
     */
    protected final void setState(final OperationState operationState) {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return;
        }
        synchronized (lock) {
            this.state = operationState;
        }
    }

    /**
     * Start the operation.
     */
    public void start() {
    }

    /**
     * Finish the operation.
     */
    public void finish() {
    }

    /**
     * Cancel the operation and notifies the listeners the operation was canceled.
     */
    public final void cancel() {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return;
        }
        synchronized (lock) {
            this.state = OperationState.FAILED;
            this.failedMessage = "Operation canceled.";
        }
        notifyListeners();
    }

    /**
     * Add a listener to listen to this operation events. If the operation already finished, notify the
     * listener immediately.
     *
     * @param listener The listener to add to the list.
     *
     * @return The AsyncOperation for convenience.
     */
    public final AsyncOperation addListener(final OperationListener<? extends AsyncOperation> listener) {
        boolean notifyNow = false;

        synchronized (lock) {
            if (this.listeners.contains(listener)) {
                return this;
            }
            this.listeners.add(listener);
            if (this.hasFinished()) {
                notifyNow = true;
            }
        }
        if (notifyNow) {
            notifyListeners();
        }
        return this;
    }

    /**
     * Removes a listener on this operation.
     *
     * @param listener The listener to remove from the list.
     * @return this
     */
    public final AsyncOperation removeListerner(final OperationListener<? extends AsyncOperation> listener) {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return this;
        }
        synchronized (lock) {
            this.listeners.remove(listener);
        }
        return this;
    }

    /**
     *
     * Mark the operation as failed and set the reason of the failure.
     *
     * @param reason The origin of the failure
     * @return this
     */
    public final AsyncOperation setFailed(final Throwable reason) {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return this;
        }
        synchronized (lock) {
            this.setFailedMessage(reason);
            this.state = OperationState.FAILED;
        }
        this.notifyListeners();
        return this;
    }

    /**
     *
     * Mark the operation as failed and set the failure message.
     *
     * @param message The message of the failure
     * @return this
     */
    public final AsyncOperation setFailed(final String message) {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return this;
        }
        synchronized (lock) {
            this.failedMessage = message;
            this.state = OperationState.FAILED;

        }
        this.notifyListeners();
        return this;
    }

    /**
     * Mark the operation has complete and notify listeners.
     */
    public final void complete() {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return;
        }
        synchronized (lock) {
            this.state = OperationState.COMPLETE;
        }
        this.notifyListeners();
    }

    /**
     * @return true if the operation completed (even if it failed), false otherwise.
     */
    public final boolean hasFinished() {
        synchronized (lock) {
            return this.state != OperationState.INIT
                    && this.state != OperationState.WAITING;
        }
    }

    /**
     * @return true if operation succeeded, false if there was an error or a false response.
     */
    public final boolean isSuccess() {
        synchronized (lock) {
            return state == OperationState.COMPLETE;
        }
    }

    /**
     * @return true if operation failed, false otherwise.
     */
    public final boolean isFailure() {
        synchronized (lock) {
            return this.state == OperationState.FAILED;
        }
    }

    /**
     * @return true if the operation was canceled, false otherwise.
     */
    public final boolean canceled() {
        synchronized (lock) {
            return this.state == OperationState.CANCELED;
        }
    }

    /**
     * Set the failed message based on the given Throwable. Should be called from a synchronized block!
     *
     * @param t The Throwable.
     */
    protected final void setFailedMessage(final Throwable t) {
        this.failedMessage = t.getLocalizedMessage();
    }

    /**
     * @return The string representation of the failure of this operation.
     */
    public final String getFailureMessage() {
        return this.failedMessage;
    }

    /**
     * Notify the listeners in case an event occurred (completion, cancellation, ...).
     */
    protected final void notifyListeners() {
        notifyListeners(true);
    }

    protected final void notifyListeners(final boolean clearListeners) {
        for (OperationListener l : listeners) {
            if (this.isSuccess()) {
                l.complete(this);
            } else {
                l.failed(this);
            }
        }
        if (clearListeners) {
            this.listeners.clear();
        }
        synchronized (lock) {
            this.lock.notifyAll();
        }
    }

    /**
     * Wait until the operation has completed. Ignores interruptions.
     *
     * @return The operation.
     */
    public final AsyncOperation awaitUninterruptibly() {
        synchronized (lock) {
            while (!this.hasFinished()) {
                try {
                    lock.wait();
                } catch (final InterruptedException e) {
                    //Do nothing
                }
            }
            return this;
        }
    }
}
