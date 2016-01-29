package org.meta.p2pp.client;

/**
 * Simple lightweight class that provides flag-based states for I/O operations on sockets.
 *
 * @author dyslesiq
 */
public class SocketIOState {

    /**
     * CONNECTING flag mask value.
     */
    public static final int CONNECTING = 0x02;

    /**
     * CONNECTED flag mask value.
     */
    public static final int CONNECTED = 0x04;

    /**
     * READING flag mask value.
     */
    public static final int READING = 0x08;

    /**
     * WRITING flag mask value.
     */
    public static final int WRITING = 0x10;

    private volatile int state;

    /**
     *
     * @return true if connecting, false otherwise
     */
    public boolean isConnecting() {
        return (state & CONNECTING) != 0;
    }

    /**
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return (state & CONNECTED) != 0;
    }

    /**
     *
     * @return true if reading, false otherwise
     */
    public boolean isReading() {
        return (state & READING) != 0;
    }

    /**
     *
     * @return true if writing, false otherwise
     */
    public boolean isWriting() {
        return (state & WRITING) != 0;
    }

    /**
     * Set the CONNECTING flag.
     */
    public void connecting() {
        state |= CONNECTING;
    }

    /**
     * Set or unset the CONNECTING flag.
     *
     * @param b true to set, false to unset
     */
    public void connecting(final boolean b) {
        if (b) {
            state |= CONNECTING;
        } else {
            state &= ~CONNECTING;
        }
    }

    /**
     * Set the CONNECTED flag.
     *
     */
    public void connected() {
        state |= CONNECTED;
    }

    /**
     * Set the CONNECTED flag.
     *
     * @param b true to set, false to unset
     */
    public void connected(final boolean b) {
        if (b) {
            state |= CONNECTED;
        } else {
            state &= ~CONNECTED;
        }
    }

    /**
     * Set the READING flag.
     *
     */
    public void reading() {
        state |= READING;
    }

    /**
     * Set the READING flag.
     *
     * @param b true to set, false to unset
     */
    public void reading(final boolean b) {
        if (b) {
            state |= READING;
        } else {
            state &= ~READING;
        }
    }

    /**
     * Set the WRITING flag.
     *
     */
    public void writing() {
        state |= WRITING;
    }

    /**
     * Set the WRITING flag.
     *
     * @param b true to set, false to unset
     */
    public void writing(final boolean b) {
        if (b) {
            state |= WRITING;
        } else {
            state &= ~WRITING;
        }
    }

    /**
     * Clears all states.
     */
    public void reset() {
        this.state = 0;
    }

}
