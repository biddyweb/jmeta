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

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryMultiKeyCreator;
import com.sleepycat.je.Transaction;
import java.util.Comparator;
import java.util.Set;
import org.meta.api.storage.KVStorage;
import org.meta.api.storage.MetaTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle Berkeley java edition database implementation of {@link KVStorage}.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class BerkeleyKVStorage implements KVStorage {

    private final Logger logger = LoggerFactory.getLogger(BerkeleyKVStorage.class);

    private final BerkeleyDatabase bDb;

    private final Environment env;

    private Database db;

    /**
     * <p>Constructor for BerkeleyKVStorage.</p>
     *
     * @param berkeleyEnv the berkeley db impl
     * @param dbName the name of the backing database
     * @param comparator the comparator to use. Can be null
     */
    public BerkeleyKVStorage(final BerkeleyDatabase berkeleyEnv, final String dbName,
            final Comparator<byte[]> comparator) {
        bDb = berkeleyEnv;
        this.db = berkeleyEnv.openDatabase(dbName, comparator);
        env = this.db.getEnvironment();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] get(final byte[] key) {
        DatabaseEntry dbKey = new DatabaseEntry(key);
        DatabaseEntry data = new DatabaseEntry();
        Cursor cursor = this.db.openCursor(null, bDb.getCursorConfig());
        OperationStatus status = cursor.getSearchKey(dbKey, data, LockMode.DEFAULT);
        cursor.close();
        if (status == OperationStatus.SUCCESS) {
            return data.getData();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean store(final MetaTx tx, final byte[] key, final byte[] value) {
        DatabaseEntry dbKey = new DatabaseEntry(key);
        DatabaseEntry data = new DatabaseEntry(value);
        OperationStatus status = this.db.put(unwrapTx(tx), dbKey, data);
        return status == OperationStatus.SUCCESS;
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(final MetaTx tx, final byte[] key) {
        DatabaseEntry dbKey = new DatabaseEntry(key);
        OperationStatus status = this.db.delete(unwrapTx(tx), dbKey);
        return status == OperationStatus.SUCCESS || status == OperationStatus.NOTFOUND;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] pop(final MetaTx tx, final byte[] key) {
        DatabaseEntry dbKey = new DatabaseEntry(key);
        DatabaseEntry data = new DatabaseEntry();
        Cursor cursor = this.db.openCursor(unwrapTx(tx), this.bDb.getCursorConfig());
        OperationStatus status = cursor.getSearchKey(dbKey, data, LockMode.DEFAULT);
        if (status == OperationStatus.SUCCESS) {
            cursor.delete();
        }
        cursor.close();
        if (status != OperationStatus.SUCCESS) {
            return null;
        }
        return data.getData();
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        return this.db.count();
    }

    /** {@inheritDoc} */
    @Override
    public MetaTx begin() {
        Transaction tx = env.beginTransaction(null, this.bDb.getTxConfig());
        return new BerkeleyTx(tx);
    }

    /** {@inheritDoc} */
    @Override
    public boolean commit(final MetaTx tx) {
        Transaction bTx = unwrapTx(tx);
        if (bTx == null) {
            return true;
        }
        try {
            bTx.commit();
            return true;
        } catch (final DatabaseException ex) {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean rollback(final MetaTx tx) {
        Transaction bTx = unwrapTx(tx);
        if (bTx == null) {
            return true;
        }
        try {
            bTx.abort();
            return true;
        } catch (final DatabaseException ex) {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.db.close();
    }

    /**
     * Utility function to return the nested Transaction inside the MetaTx object.
     *
     * @param tx the meta transaction object containing the Berkeley transaction
     * @return the Berkeley db transaction or null if tx == null
     */
    private Transaction unwrapTx(final MetaTx tx) {
        if (tx == null) {
            return null;
        }
        BerkeleyTx bTx = (BerkeleyTx) tx;
        return bTx.getTx();
    }

    /** {@inheritDoc} */
    @Override
    public String getDatabaseName() {
        return this.db.getDatabaseName();
    }

    /**
     * Berkeley db implementation of Transactions for the meta storage layer.
     */
    private class BerkeleyTx implements MetaTx {

        private final Transaction tx;

        /**
         *
         * @param tx the real transaction
         */
        BerkeleyTx(final Transaction transaction) {
            this.tx = transaction;
        }

        Transaction getTx() {
            return tx;
        }

    }

    private static class SecondaryKeyCreatorAdaptor implements SecondaryMultiKeyCreator {

        @Override
        public void createSecondaryKeys(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data,
                Set<DatabaseEntry> results) {
        }

    }

}
