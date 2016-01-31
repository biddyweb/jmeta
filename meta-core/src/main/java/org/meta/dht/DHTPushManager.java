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
package org.meta.dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.apache.log4j.Logger;
import org.meta.api.common.MetHash;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.MetaDHT;
import org.meta.api.dht.StoreOperation;
import org.meta.api.model.ModelStorage;
import org.meta.api.model.Searchable;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.KVMapStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.api.storage.MetaTx;
import org.meta.api.storage.Serializer;
import org.meta.dht.storage.PushElementSerializer;
import org.meta.executors.MetaScheduledTask;
import org.meta.storage.comparators.Comparators;
import org.meta.storage.serializers.Serializers;

/**
 * The DHT push manager.
 *
 * This class keeps references on hashs to push on the DHT and push them or discard them at regular intervals.
 *
 * @author nico
 * @version $Id: $
 */
public class DHTPushManager extends MetaScheduledTask {

    /**
     * Interval to wait before pushing the same hash over the DHT.
     *
     * TODO move to global meta parameters.
     */
    private static final int S_PUSHI = 3600000;
    //private static final int S_PUSHI = 5000;

    /**
     * Interval to wait before pushing the same hash over the DHT if the last push failed.
     *
     * TODO move to global meta parameters.
     */
    private static final int F_PUSHI = 500000;
    //private static final int F_PUSHI = 5000;

    private Logger logger = Logger.getLogger(DHTPushManager.class);

    private static final String DHT_PUSH_DB_NAME = "DHT_PUSH_ELEMENTS";

    private static final String PUSH_LIST_DB_NAME = "DHT_PUSH_LIST";

    private final MetaDHT dht;

    private final MetaDatabase db;

    private final ModelStorage model;

    private final Serializer<DHTPushElement> elementSerializer = new PushElementSerializer();

    private final KVMapStorage<MetHash, DHTPushElement> pushElements;

    private final CollectionStorage<DHTPushElement> sortedElements;

    /**
     * <p>Constructor for DHTPushManager.</p>
     *
     * @param metaDht the dht
     * @param database the meta database
     * @param modelStorage the model storage
     */
    public DHTPushManager(final MetaDHT metaDht, final MetaDatabase database,
            final ModelStorage modelStorage) {
        //TODO we should start this timed task only once the DHT has bootstraped
        //TODO the execution interval should also be the same as 'F_PUSHI' to re-push failed hash asap
        super(5, 10);
        this.dht = metaDht;
        this.db = database;
        this.model = modelStorage;
        //We need K/V access and the sorted Set interface from different databases names.
        this.pushElements = this.db.getKVMapStorage(DHT_PUSH_DB_NAME, Serializers.METHASH,
                elementSerializer);
        this.sortedElements = this.db.getCollection(PUSH_LIST_DB_NAME, elementSerializer, Comparators.LONG);
    }

    /**
     * Adds a hash to the push manager.
     *
     * We first check if the hash is already present in the push list. If it is not, it is inserted in two
     * formats: the hash in the Map storage to keep track on registered elements, and in the CollectionStorage
     * in the {@link DHTPushElement} format to have an ordered list of elements.
     *
     * @param hash the hash to push
     * @param expirationDate the expiration timestamp of the hash. 0 for no expiration.
     */
    public void pushElement(final MetHash hash, final long expirationDate) {
        synchronized (pushElements) {
            DHTPushElement element = this.pushElements.get(hash);

            if (element == null) {
                logger.info("DHTPush: hash (" + hash + ") not already present in the push list: adding");
                element = new DHTPushElement(hash, System.currentTimeMillis(), expirationDate);
                this.pushElements.put(null, hash, element);
                this.sortedElements.add(element);
            } else {
                logger.info("DHTPush hash(" + hash + ") already present in the push list.");
            }
        }
    }

    /**
     *
     * @return A fake push element used to retrieve elements before 'now'.
     */
    private DHTPushElement getNowElement() {
        return new DHTPushElement(MetHash.ZERO, System.currentTimeMillis(), 0);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        synchronized (pushElements) {
            List<StoreOperation> operations = new ArrayList<>();
            Map<MetHash, DHTPushElement> elements = new HashMap<>();
            DHTPushElement nowElement = getNowElement();
            long now = nowElement.getNextPushTime();
            MetaTx collectionTx = sortedElements.begin();
            MetaTx kvTx = pushElements.begin();
            SortedSet<DHTPushElement> toPush = sortedElements.headSet(nowElement);

            for (DHTPushElement element : toPush) {
                if (element != null) {
                    Searchable s = model.get(element.getHash());
                    DHTPushElement kvElement = this.pushElements.get(element.getHash());
                    if (kvElement != null && s != null && (element.getExpiration() == 0
                            || element.getExpiration() >= now)) {
                        operations.add(dht.doStore(element.getHash()));
                        elements.put(element.getHash(), element);
                    } else {
                        this.pushElements.remove(kvTx, element.getHash());
                        //If associated hash doesn't exists in the model or if the element has expired, just remove the hash from the KV storage. All elements in the headSet will be removed anyways.
                    }
                } else {
                    //We might retrieve null element from the Set (See serializer).
                    logger.info("Element from SET is null");
                }
            }
            toPush.clear();
            this.pushElements.commit(kvTx);
            if (!operations.isEmpty()) {
                PushListener listener = new PushListener(elements, operations);
                listener.awaitAllPushs();
                for (DHTPushElement element : listener.getElements().values()) {
                    sortedElements.add(element);
                }
            }
            this.sortedElements.commit(collectionTx);
        }
    }

    /**
     *
     */
    private class PushListener implements OperationListener<StoreOperation> {

        private int nbReceived = 0;

        private final int nbOperations;

        private final Map<MetHash, DHTPushElement> elements;

        private final List<StoreOperation> ops;

        /**
         *
         * @param toPush the elements we are pushing
         * @param operations the StoreOperations to wait for
         */
        public PushListener(final Map<MetHash, DHTPushElement> toPush,
                final List<StoreOperation> operations) {
            this.ops = operations;
            this.elements = toPush;
            this.nbOperations = operations.size();
            for (StoreOperation op : ops) {
                op.addListener(this);
            }
        }

        private void pushed(final MetHash hash, final boolean failed) {
            synchronized (this) {
                DHTPushElement element = this.elements.get(hash);

                if (failed) {
                    logger.info("Failed to push: " + element.getHash());
                    element.setNextPushTime(element.getNextPushTime() + F_PUSHI);
                } else {
                    logger.info("Success to push:" + element.getHash());
                    element.setNextPushTime(element.getNextPushTime() + S_PUSHI);
                }
                if (++nbReceived == nbOperations) {
                    this.notifyAll();
                }
            }
        }

        @Override
        public void failed(final StoreOperation operation) {
            pushed(operation.getHash(), true);
        }

        @Override
        public void complete(final StoreOperation operation) {
            pushed(operation.getHash(), false);
        }

        Map<MetHash, DHTPushElement> getElements() {
            return this.elements;
        }

        /**
         * Wait until all operations have completed. Ignores interruptions.
         *
         * @return The operation.
         */
        void awaitAllPushs() {
            synchronized (this) {
                while (nbReceived != nbOperations) {
                    try {
                        this.wait();
                    } catch (final InterruptedException e) {
                        //Do nothing
                    }
                }
            }
        }
    }

}
