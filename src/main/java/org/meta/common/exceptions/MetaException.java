/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.meta.common.exceptions;

/**
 *
 * @author nico
 */
public class MetaException extends Exception {

    public MetaException(String message) {
        super(message);
    }

    
    public MetaException(Throwable t) {
        super(t);
    }

    public MetaException(String message, Throwable t) {
        super(message, t);
    }
}
