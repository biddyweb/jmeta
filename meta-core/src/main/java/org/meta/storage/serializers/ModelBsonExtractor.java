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

import java.util.Set;
import org.bson.BSON;
import org.bson.BSONObject;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.model.MetaSearch;
import org.meta.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used by the object model storage to access utility serialization methods.
 *
 * @author dyslesiq
 */
public class ModelBsonExtractor {

    private final Logger logger = LoggerFactory.getLogger(ModelBsonExtractor.class);

    private final DataBsonSerializer dataBson;
    private final SearchBsonSerializer searchBson;

    private byte[] serializedData;
    private BSONObject bsonRoot;
    private ModelType type;

    /**
     * Creates this extractor with given serialized data and appropriate serializers.
     *
     * @param data the serialized data
     * @param dataSerializer the data de/serializer
     * @param searchSerializer the search de/serializer
     */
    public ModelBsonExtractor(final byte[] data, final DataBsonSerializer dataSerializer,
            final SearchBsonSerializer searchSerializer) {
        this.serializedData = data;
        this.bsonRoot = BSON.decode(data);
        this.dataBson = dataSerializer;
        this.searchBson = searchSerializer;
    }

    /**
     *
     * @return the type model object extracted from serialized data
     */
    public ModelType getType() {
        String strType = (String) this.bsonRoot.get(BsonSerializer.TYPE_KEY);

        this.type = ModelType.valueOf(strType);
        return this.type;
    }

    /**
     *
     * @return the extracted Search, or null if the bson element is of the wrong type
     */
    public MetaSearch getSearch() {
        if (type == ModelType.SEARCH) {
            return this.searchBson.deserialize(bsonRoot);
        }
        logger.debug("getSearch: type != SEARCH");
        return null;
    }

    /**
     *
     * @return the extracted GenericData, or null if the bson element is of the wrong type
     */
    public Data getData() {
        if (type == ModelType.DATA) {
            return this.dataBson.deserialize(bsonRoot);
        }
        logger.debug("getData: type != DATA");
        return null;
    }

    /**
     *
     * @return the result list of the search, only if the bson element is a search, otherwise returns null
     */
    public Set<MetHash> getSearchResultsHash() {
        if (type == ModelType.SEARCH) {
            return this.searchBson.getResults(bsonRoot);
        }
        return null;
    }

    /**
     *
     * @return the hash of the source, only if the bson element is a search, otherwise returns null
     */
    public MetHash getSearchSourceHash() {
        if (type == ModelType.SEARCH) {
            return this.searchBson.getSourceHash(bsonRoot);
        }
        return null;
    }

}
