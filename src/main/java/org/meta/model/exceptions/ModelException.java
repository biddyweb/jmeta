/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.model.exceptions;

import org.meta.common.exceptions.MetaException;

/**
 *
 * @author nico
 */
public class ModelException extends MetaException {

    public ModelException(String message) {
        super(message);
    }

    public ModelException(Throwable t) {
        super(t);
    }

    public ModelException(String message, Throwable t) {
        super(t);
    }

}
