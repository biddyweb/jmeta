/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.p2pp.exceptions;

import org.meta.api.common.exceptions.MetaException;

/**
 * Base exception for Peer-to-Peer Protocol errors.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class P2PPException extends MetaException {

    /**
     * <p>Constructor for P2PPException.</p>
     *
     * @param message the exception message
     */
    public P2PPException(final String message) {
        super(message);
    }

    /**
     * <p>Constructor for P2PPException.</p>
     *
     * @param t the initial throwable
     */
    public P2PPException(final Throwable t) {
        super(t);
    }

    /**
     * <p>Constructor for P2PPException.</p>
     *
     * @param message the exception message
     * @param t the initial throwable
     */
    public P2PPException(final String message, final Throwable t) {
        super(message, t);
    }

}
