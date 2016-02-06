/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.api.common;

/**
 * Base interface to get the result of an asynchronous operation.
 *
 * To be declined for each type of operation.
 *
 * @author nico
 * @param <T> The Subtype of {@link AsyncOperation} this listener listen to.
 * @version $Id: $
 */
public interface OperationListener<T extends AsyncOperation> {

    /**
     * Method called if the listened operation failed.
     *
     * @param operation The operation that failed.
     */
    void failed(T operation);

    /**
     * Method called if the listened operation completed.
     *
     * @param operation The operation that completed.
     */
    void complete(T operation);
}
