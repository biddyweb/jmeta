/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 JMeta
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.configuration.exceptions;

import org.meta.common.exceptions.MetaException;

/**
 * Exception thrown if an invalid or inexistent configuration file is encountered.
 */
public class InvalidConfigurationFileException extends MetaException {

    /**
     * 
     * @param message 
     */
    public InvalidConfigurationFileException(String message) {
        super(message);
    }

    /**
     * 
     * @param t 
     */
    public InvalidConfigurationFileException(Throwable t) {
        super(t);
    }

    /**
     * 
     * @param message
     * @param t 
     */
    public InvalidConfigurationFileException(String message, Throwable t) {
        super(message, t);
    }

}
