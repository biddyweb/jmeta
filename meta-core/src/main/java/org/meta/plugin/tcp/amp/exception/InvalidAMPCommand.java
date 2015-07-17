package org.meta.plugin.tcp.amp.exception;

import org.meta.api.common.exceptions.MetaException;

/**
 *
 * @author nico
 */
public class InvalidAMPCommand extends MetaException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * @param message
     */
    public InvalidAMPCommand(String message) {
        super(message);
    }

}
