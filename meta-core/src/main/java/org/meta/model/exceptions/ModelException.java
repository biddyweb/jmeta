/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 JMeta
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
package org.meta.model.exceptions;

import org.meta.api.common.exceptions.MetaException;

/**
 * The base exception for model operations.
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