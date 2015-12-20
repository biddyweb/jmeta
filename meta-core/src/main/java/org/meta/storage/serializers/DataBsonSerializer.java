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

import java.nio.ByteBuffer;
import java.util.Iterator;
import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaDataMap;
import org.meta.model.GenericData;
import org.meta.model.ModelType;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class DataBsonSerializer implements BsonSerializer<Data> {

    private final Logger logger = LoggerFactory.getLogger(DataBsonSerializer.class);

    /**
     *
     */
    public DataBsonSerializer() {
    }

    private BSONObject getBson(final Data obj) {
        BSONObject bsonObject = new BasicBSONObject();
        bsonObject.put(HASH_KEY, obj.getHash().toString());
        //bsonObject.put(TYPE_KEY, obj.getType().toString());
        bsonObject.put(SIZE_KEY, Integer.toString(obj.getSize()));
        bsonObject.put(TYPE_KEY, ModelType.getType(obj).name());
        //foreach proerties, get value and name and put it in the json
        BasicBSONList bsonProperties = new BasicBSONList();
        int count = 0;
        for (Iterator<MetaData> i = obj.getMetaDataMap().iterator(); i.hasNext(); count++) {
            MetaData property = i.next();
            BasicBSONObject bsonProperty = new BasicBSONObject();
            bsonProperty.put(METADATA_KEY, property.getKey());
            bsonProperty.put(METADATA_VALUE_KEY, property.getValue());
            bsonProperties.put(count, bsonProperty);
        }
        bsonObject.put(DATA_META_KEY, bsonProperties);
        bsonObject.put(DATA_KEY, SerializationUtils.toHexString(obj.getBuffer()));
        return bsonObject;
    }

    @Override
    public Data fromJson(final String json) {
        throw new AbstractMethodError();
    }

    @Override
    public String toJson(final Data object) {
        return getBson(object).toString();
    }

    @Override
    public byte[] serialize(final Data obj) {
        return BSON.encode(getBson(obj));
    }

    /**
     * Deserialize from an existing bson object.
     *
     * No type checking will be done here.
     *
     * @param obj the existing bson object of the correct type.
     * @return the extracted GenericData
     */
    public Data deserialize(final BSONObject obj) {
        MetHash hash = new MetHash((String) obj.get(HASH_KEY));
        ByteBuffer buf = SerializationUtils.fromHexString((String) obj.get(DATA_KEY));
        int size = Integer.parseInt((String) obj.get(SIZE_KEY));
        String type = (String) obj.get(TYPE_KEY);

        Data data = new GenericData(hash, buf, size);

        BasicBSONList bsonProperties = (BasicBSONList) obj.get(DATA_META_KEY);
        MetaDataMap mdMap = new MetaDataMap();
        BSONObject tmp;
        MetaData toAdd;
        for (String k : bsonProperties.keySet()) {
            tmp = (BSONObject) bsonProperties.get(k);
            toAdd = new MetaData(tmp.get(METADATA_KEY).toString(), tmp.get(METADATA_VALUE_KEY).toString());
            mdMap.put(toAdd);
        }
        data.setMetaData(mdMap);
        return data;
    }

    @Override
    public Data deserialize(final byte[] bsonData) {
        BSONObject obj = BSON.decode(bsonData);

        if (obj == null) {
            logger.warn("BSON failed to decode raw data.");
            return null;
        }
//        ModelType type = ModelType.valueOf((String) obj.get(TYPE_KEY));
//        if (type == null) {
//            return null;
//        }
        return deserialize(obj);
    }
}
