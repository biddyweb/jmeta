/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.plugin.exceptions;

import org.meta.api.common.exceptions.MetaException;

/**
 *
 * @author nico
 */
public class PluginLoadException extends MetaException {

    /**
     *
     * @param message
     * @param t
     */
    public PluginLoadException(String message, Throwable t) {
        super(message, t);
    }

    /**
     *
     * @param message
     */
    public PluginLoadException(String message) {
        super(message);
    }

    /**
     *
     * @param t
     */
    public PluginLoadException(Throwable t) {
        super(t);
    }
}