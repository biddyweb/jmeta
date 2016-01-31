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
package org.meta.storage;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.collections.CurrentTransaction;
import com.sleepycat.collections.StoredCollections;
import com.sleepycat.collections.StoredSortedKeySet;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Transaction;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.MetaTx;
import org.meta.api.storage.Serializer;

/**
 *
 * Oracle Berkeley java edition database implementation of CollectionStorage.
 *
 * All Collection operations are backed by Berkeley's StoredSortedKeySet.
 *
 * This implementation just adds explicit transaction support and a name to the collection.
 *
 * @param <T> the type of this collection elements
 * @author dyslesiq
 * @version $Id: $
 */
public class BerkeleyCollectionStorage<T> implements CollectionStorage<T> {

    //private final BerkeleyDatabase berkeleyEnv;
    private final Database db;

    private final StoredSortedKeySet<T> set;

    private final CurrentTransaction trans;

    private final Serializer<T> serializer;

    /**
     * <p>Constructor for BerkeleyCollectionStorage.</p>
     *
     * @param env the backing Berkeley's database
     * @param dbName the db name
     * @param s the serializer
     * @param comparator the comparator
     * @param <T> a T object.
     */
    public BerkeleyCollectionStorage(final BerkeleyDatabase env, final String dbName, final Serializer<T> s,
            final Comparator<byte[]> comparator) {
        this.serializer = s;
        this.db = env.openDatabase(dbName, comparator);

        StoredSortedKeySet<T> iSet = new StoredSortedKeySet<>(db, new BindingAdaptor<>(serializer), true);
        set = (StoredSortedKeySet<T>) StoredCollections.configuredSortedSet(iSet, env.getCursorConfig());
        trans = CurrentTransaction.getInstance(this.db.getEnvironment());
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseName() {
        return this.db.getDatabaseName();
    }

    /** {@inheritDoc} */
    @Override
    public Serializer<T> getSerializer() {
        return this.serializer;
    }

    /** {@inheritDoc} */
    @Override
    public MetaTx begin() {
        Transaction tx = trans.getTransaction();

        if (tx == null || !tx.isValid()) {
            trans.beginTransaction(null);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean commit(final MetaTx tx) {
        trans.commitTransaction();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean rollback(final MetaTx tx) {
        trans.abortTransaction();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Comparator<? super T> comparator() {
        //Berkley does not support comparators :(
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<T> subSet(final T fromElement, final T toElement) {
        return this.set.subSet(fromElement, toElement);
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<T> headSet(final T toElement) {
        return this.set.headSet(toElement);
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<T> tailSet(final T fromElement) {
        return this.set.tailSet(fromElement);
    }

    /** {@inheritDoc} */
    @Override
    public T first() {
        return this.set.first();
    }

    /** {@inheritDoc} */
    @Override
    public T last() {
        return this.set.last();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return this.set.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return this.set.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Object o) {
        return this.set.contains(o);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator() {
        return this.set.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
        return this.set.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public <T> T[] toArray(final T[] arg0) {
        return this.set.toArray(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(final T e) {
        return this.set.add(e);
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(final Object o) {
        return this.set.remove(o);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.set.containsAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return this.set.addAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.set.retainAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.set.removeAll(c);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        this.set.clear();
    }

    /**
     * Adaptor between Meta's serializers and Berkeley's bindings for internal DB key/value storage.
     *
     * @param <T> the type
     */
    private static class BindingAdaptor<T> implements EntryBinding<T> {

        private final Serializer<T> serializer;

        /**
         *
         * @param serialize the serializer to use
         */
        BindingAdaptor(final Serializer<T> serialize) {
            this.serializer = serialize;
        }

        @Override
        public T entryToObject(final DatabaseEntry entry) {
            return this.serializer.deserialize(entry.getData());
        }

        @Override
        public void objectToEntry(final T object, DatabaseEntry entry) {
            entry.setData(this.serializer.serialize(object));
        }
    }

}
