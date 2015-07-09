package org.meta.plugin.tcp.amp.exception;

import org.meta.common.exceptions.MetaException;

public class InvalidAMPCommand extends MetaException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InvalidAMPCommand(String message) {
        super(message);
    }

}
