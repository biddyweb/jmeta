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
package org.meta.api.model;

import java.io.File;
import java.net.URI;
import org.meta.api.common.MetHash;

/**
 * File implementation of a Data. Point to a File on the local hard drive.
 *
 * @author Thomas LAVOCAT
 * @version $Id: $
 */
public abstract class DataFile extends Data {

    /**
     *
     */
    protected File file = null;

    /**
     * Default constructor with hash = {@link MetHash#ZERO}.
     */
    public DataFile() {
        super(MetHash.ZERO);
    }

    /**
     * <p>
     * Getter for the field <code>file</code>.</p>
     *
     * @return the file instance managed by this AbstractDataFile, or null if not set
     */
    public abstract File getFile();

    /**
     * Defines the file managed by this DataFile.
     *
     * @param aFile the file instance
     */
    public abstract void setFile(final File aFile);

    /**
     * Defines the file managed by this DataFile from its URI.
     *
     * @param uri the URI instance
     */
    public abstract void setURI(final URI uri);

    /**
     * Get the URI of the file managed by this DataFile.
     *
     * @return the URI
     */
    public abstract URI getURI();
}
