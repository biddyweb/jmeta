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
package org.meta.model.files;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.meta.api.model.DataFile;

/**
 * Static accessor for data file read/write operation.
 *
 * Provides a bit of caching to avoid opening the same time many times unnecessarily.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public final class DataFileAccessors {

    private final static int MIN_EXECUTOR_THREADS = 1;

    private final static int MAX_EXECUTOR_THREADS = 10;

    private final static ExecutorService filesExecutor = new ThreadPoolExecutor(MIN_EXECUTOR_THREADS,
            MAX_EXECUTOR_THREADS, 120L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private static Map<URI, DataFileAccessor> accessors = new ConcurrentHashMap<>();

    private DataFileAccessors() {
    }

    /**
     * Get a read/write accessor for the given dataFile.
     *
     * @param dataFile the DataFile
     * @return the accessor for the given datafile
     */
    public static synchronized DataFileAccessor getAccessor(final DataFile dataFile) {
        if (!accessors.containsKey(dataFile.getURI())) {
            accessors.put(dataFile.getURI(), new DataFileAccessor(dataFile));
        }
        return accessors.get(dataFile.getURI());
    }

    /**
     * <p>Getter for the field <code>filesExecutor</code>.</p>
     *
     * @return the files executor service
     */
    public static ExecutorService getFilesExecutor() {
        return filesExecutor;
    }

}
