/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Nicolas Michon
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.dht;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base interface representing an asynchronous operation on the DHT.
 *
 * @author nico
 */
public abstract class DHTOperation {

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

    protected final Object lock = new Object();
    protected Throwable failedReason;
    protected String failedMessage;
    protected OperationState state;
    protected Collection<OperationListener<? extends DHTOperation>> listeners;

    /**
     * Parent constructor.
     */
    public DHTOperation() {
        this.state = OperationState.INIT;
        this.listeners = new ArrayList<OperationListener<? extends DHTOperation>>();
    }

    /**
     * For internal use, set the state in a thread-safe manner.
     *
     * @param state The new state.
     */
    protected void setState(OperationState state) {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return;
        }
        synchronized (lock) {
            this.state = state;
        }
    }

    /**
     * Start the operation.
     */
    public abstract void start();

    /**
     * Finish the operation.
     */
    public abstract void finish();

    /**
     * Cancel the operation and notifies the listeners the operation was canceled.
     */
    public void cancel() {
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
     * Add a listener to listen to this operation events. If the operation already finished, notify the listener
     * immediately.
     *
     * @param listener The listener to add to the list.
     *
     * @return The DHTOperation for convenience.
     */
    public DHTOperation addListener(OperationListener<? extends DHTOperation> listener) {
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
     */
    public DHTOperation removeListerner(OperationListener<? extends DHTOperation> listener) {
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
     */
    public DHTOperation setFailed(Throwable reason) {
        if (this.hasFinished()) {
            //No need to do anything if the operation already finished.
            return this;
        }
        synchronized (lock) {
            this.setFailedMessage(reason);
            this.notifyListeners();
            return this;
        }
    }

    /**
     *
     * Mark the operation as failed and set the failure message.
     *
     * @param message The message of the failure
     */
    public DHTOperation setFailed(String message) {
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
     * @return true if the operation completed (even if it failed), false otherwise.
     */
    public boolean hasFinished() {
        synchronized (lock) {
            return this.state != OperationState.INIT
                    && this.state != OperationState.WAITING;
        }
    }

    /**
     * @return true if operation succeeded, false if there was an error or a false response.
     */
    public boolean isSuccess() {
        synchronized (lock) {
            return state == OperationState.COMPLETE;
        }
    }

    /**
     * @return true if operation failed, false otherwise.
     */
    public boolean isFailure() {
        synchronized (lock) {
            return this.state == OperationState.FAILED;
        }
    }

    /**
     * @return true if the operation was canceled, false otherwise.
     */
    public boolean canceled() {
        synchronized (lock) {
            return this.state == OperationState.CANCELED;
        }
    }

    /**
     * Set the failed message based on the given Throwable.
     *
     * @param t The Throwable.
     */
    protected void setFailedMessage(final Throwable t) {
        this.failedMessage = t.getLocalizedMessage();
    }

    /**
     * @return The string representation of the failure of this operation.
     */
    public String getFailureMessage() {
        return this.failedMessage;
    }

    /**
     * Notify the listeners in case an event occurred (completion, cancellation, ...)
     */
    protected void notifyListeners() {
        synchronized (lock) {
            for (OperationListener l : listeners) {
                if (this.isSuccess()) {
                    l.complete(this);
                } else {
                    l.failed(this);
                }
            }
            this.listeners.clear();
            lock.notifyAll();
        }
    }

    public DHTOperation awaitUninterruptibly() {
        synchronized (lock) {
            while (!this.hasFinished()) {
                try {
                    lock.wait();
                    System.err.println("awaitUninterruptibly got notified...");
                } catch (final InterruptedException e) {
                    //Do nothing
                    System.err.println("awaitUninterruptibly interrupted");
                }
            }
            return this;
        }
    }
}
