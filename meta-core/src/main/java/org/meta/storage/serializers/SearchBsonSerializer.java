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

import java.util.HashSet;
import java.util.Set;
import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.api.model.SearchCriteria;
import org.meta.model.MetaSearch;
import org.meta.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bson de/serializer for a Search.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class SearchBsonSerializer implements BsonSerializer<MetaSearch> {

    private Logger logger = LoggerFactory.getLogger(SearchBsonSerializer.class);

    private BSONObject getBson(final MetaSearch obj) {
        BSONObject bsonObject = new BasicBSONObject();

        logger.debug("Get bson for search: " + obj.getHash().toString());

        //Results
        BasicBSONList bsonResults = new BasicBSONList();
        int i = 0;
        for (Data data : obj.getResults()) {
            logger.debug("Adding search result to bson list: " + data.getHash().toString());
            bsonResults.put(i++, data.getHash().toString());
        }
        bsonObject.put(RESULTS_KEY, bsonResults);
        //Criteria
        BasicBSONList bsonCriteria = new BasicBSONList();
        int count = 0;
        BSONObject tmp;
        for (MetaData metaData : obj.getCriteria().getCriteria()) {
            logger.debug("Adding search criteria to bson list: " + metaData.getKey());
            tmp = new BasicBSONObject();
            tmp.put(METADATA_KEY, metaData.getKey());
            tmp.put(METADATA_VALUE_KEY, metaData.getValue());
            bsonCriteria.put(count++, tmp);
        }
        bsonObject.put(CRITERIA_KEY, bsonCriteria);
        bsonObject.put(SOURCE_KEY, obj.getSource().getHash().toString());
        bsonObject.put(HASH_KEY, obj.getHash().toString());
        bsonObject.put(TYPE_KEY, ModelType.getType(obj).name());
        return bsonObject;
    }

    /** {@inheritDoc} */
    @Override
    public MetaSearch fromJson(final String json) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String toJson(final MetaSearch object) {
        return getBson(object).toString();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] serialize(final MetaSearch object) {
        return BSON.encode(getBson(object));
    }

    /**
     * Extracts the result list from the given bson object.
     *
     * @param obj the bson object
     * @return the extract Set of result hash
     */
    public Set<MetHash> getResults(final BSONObject obj) {
        BasicBSONList bsonResults = (BasicBSONList) obj.get(RESULTS_KEY);
        Set<MetHash> results = new HashSet<>();

        for (String k : bsonResults.keySet()) {
            results.add(new MetHash((String) bsonResults.get(k)));
        }
        return results;
    }

    /**
     * Extracts the source hash from the given bson object.
     *
     * @param obj the bsn object
     * @return the extracted MetHash
     */
    public MetHash getSourceHash(final BSONObject obj) {
        return new MetHash((String) obj.get(SOURCE_KEY));
    }

    /**
     * Deserialize from an existing bson object.
     *
     * No type checking will be done here.
     *
     * @param obj the existing bson object of the correct type.
     * @return the extracted Search
     */
    public MetaSearch deserialize(final BSONObject obj) {
        MetHash hash = new MetHash((String) obj.get(HASH_KEY));

        BasicBSONList criteriaBson = (BasicBSONList) obj.get(CRITERIA_KEY);
        BSONObject tmp;
        String tmpKey, tmpValue;
        Set<MetaData> criteriaSet = new HashSet<>();
        for (String k : criteriaBson.keySet()) {
            tmp = (BSONObject) criteriaBson.get(k);
            tmpKey = (String) tmp.get(METADATA_KEY);
            tmpValue = (String) tmp.get(METADATA_VALUE_KEY);
            criteriaSet.add(new MetaData(tmpKey, tmpValue));
        }
        SearchCriteria criteria = new SearchCriteria(criteriaSet);
        MetaSearch search = new MetaSearch(hash, criteria, null, null);
        return search;
    }

    /** {@inheritDoc} */
    @Override
    public MetaSearch deserialize(final byte[] data) {
        BSONObject obj = BSON.decode(data);
        ModelType type = ModelType.valueOf((String) obj.get(TYPE_KEY));

        if (type == null || type != ModelType.SEARCH) {
            return null;
        }
        return deserialize(obj);
    }

}
