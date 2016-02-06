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
package org.meta.storage.serializers;

import org.meta.api.model.Searchable;
import org.meta.api.storage.Serializer;

/**
 * <p>
 * BsonSerializer interface.</p>
 *
 * @param <T> the searchable to serialize
 * @author dyslesiq
 * @version $Id: $
 */
public interface BsonSerializer<T extends Searchable> extends Serializer<T> {

    /**
     * The key under which the type of object will be stored in bson.
     */
    String TYPE_KEY = "type";

    /**
     * The key under which the source hash will be stored in bson.
     */
    String HASH_KEY = "hash";

    /**
     * The key under which the size of a data will be stored in bson.
     */
    String SIZE_KEY = "size";

    /**
     * The key under which the source hash will be stored in bson.
     */
    String SOURCE_KEY = "source";

    /**
     * The key under which the search criteria will be stored in bson.
     */
    String CRITERIA_KEY = "criteria";

    /**
     * The key under which the search results will be stored in bson.
     */
    String RESULTS_KEY = "results";

    /**
     * The key under which the raw data of a Data will be stored in bson.
     */
    String DATA_KEY = "data";

    /**
     * The key under which the meta-data list of a Data will be stored in bson.
     */
    String DATA_META_KEY = "metadata";

    /**
     * The key under which a metadata key will be stored in bson.
     */
    String METADATA_KEY = "key";

    /**
     * The key under which a metadata value will be stored in bson.
     */
    String METADATA_VALUE_KEY = "value";

    /**
     * <p>
     * fromJson</p>
     *
     * @param json the json string
     * @return the created object from json, or null if unable to process
     */
    T fromJson(final String json);

    /**
     * <p>
     * toJson</p>
     *
     * @param object the object to serialize to json
     * @return the json string, or null if unable to process
     */
    String toJson(final T object);

}
