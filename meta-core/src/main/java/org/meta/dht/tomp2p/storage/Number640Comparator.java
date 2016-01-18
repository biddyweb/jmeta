package org.meta.dht.tomp2p.storage;

import java.io.Serializable;
import java.util.Comparator;
import net.tomp2p.peers.Number640;

/**
 *
 * @author dyslesiq
 */
public class Number640Comparator implements Comparator<byte[]>, Serializable {

    @Override
    public int compare(final byte[] t, final byte[] t1) {
        Number640 left = Number640Serializer.INSTANCE.deserialize(t);
        Number640 right = Number640Serializer.INSTANCE.deserialize(t1);
        if (left == null) {
            return 1;
        } else if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }
}
