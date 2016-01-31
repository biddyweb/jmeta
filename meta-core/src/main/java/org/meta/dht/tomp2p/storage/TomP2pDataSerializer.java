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
package org.meta.dht.tomp2p.storage;

import java.nio.ByteBuffer;
import net.tomp2p.storage.Data;
import org.meta.api.storage.Serializer;
import org.meta.utils.SerializationUtils;

/**
 * Used by the Tomp2pStorage to store Tomp2p Data objects.
 *
 * This is a simple implementation that keep only the raw data and the expiration timestamp.
 *
 * All other properties from the Data are ignored. Meta doesn't need them.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class TomP2pDataSerializer implements Serializer<Data> {

    /** {@inheritDoc} */
    @Override
    public byte[] serialize(final Data data) {
        byte[] buf = new byte[data.length() + Long.BYTES];

        SerializationUtils.longToBytes(data.expirationMillis(), buf, 0);
        ByteBuffer[] buffers = data.toByteBuffers();
        int pos = Long.BYTES;
        for (ByteBuffer bb : buffers) {
            int remain = bb.remaining();
            bb.get(buf, pos, remain);
            pos += remain;
        }
        return buf;
    }

    /** {@inheritDoc} */
    @Override
    public Data deserialize(final byte[] buf) {
        long expirationMilis = SerializationUtils.bytesToLong(buf);

        Data data = new Data(buf, Long.BYTES, buf.length - Long.BYTES);
        long validFrom = System.currentTimeMillis();
        long ttl = expirationMilis - validFrom;
        data.ttlSeconds((int) (ttl / 1000));
        data.validFromMillis(validFrom);
        return data;
    }
}
