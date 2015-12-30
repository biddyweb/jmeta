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
package org.meta.tests.p2pp;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetaPeer;
import org.meta.api.common.OperationListener;
import org.meta.api.model.Data;
import org.meta.api.model.DataFile;
import org.meta.api.model.MetaData;
import org.meta.api.model.MetaDataMap;
import org.meta.api.model.ModelStorage;
import org.meta.api.model.Search;
import org.meta.api.model.SearchCriteria;
import org.meta.api.p2pp.GetOperation;
import org.meta.api.plugin.MetAPI;
import org.meta.api.plugin.SearchOperation;
import org.meta.api.storage.MetaStorage;
import org.meta.configuration.MetaConfiguration;
import org.meta.model.files.DataFileAccessor;
import org.meta.model.files.DataFileAccessors;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.P2PPManager;
import org.meta.p2pp.client.MetaP2PPClient;
import org.meta.p2pp.client.P2PPClient;
import org.meta.p2pp.exceptions.P2PPException;
import org.meta.storage.KyotoCabinetStorage;
import org.meta.storage.MetaModelStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.tests.MetaBaseTests;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class P2PPTest extends MetaBaseTests {

    private static final Logger logger = LoggerFactory.getLogger(P2PPTest.class);

    private static P2PPManager manager;

    private static MetaPeer serverPeer;

    private static ModelStorage model;

    private static P2PPClient client;

    private static MetaP2PPClient metaP2ppClient;

    private static MetAPI api;

    private static MetHash searchHash;

    private static MetHash searchHash2;

    private static Data result;

    private static Data result2;

    private static DataFile dataFile;

    @BeforeClass
    public static void setup() {
        setUpModel();
        setUpServer();
        fillModel();
    }

    /**
     * Starts the database.
     */
    public static void setUpModel() {
        try {
            MetaStorage storage = new KyotoCabinetStorage(MetaConfiguration.getModelConfiguration());
            model = new MetaModelStorage(storage);
        } catch (StorageException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Starts the server.
     */
    public static void setUpServer() {
        try {
            manager = new P2PPManager(MetaConfiguration.getP2ppConfiguration(), model);
            client = manager.getClient();
            metaP2ppClient = new MetaP2PPClient(client);
            manager.startServer();
        } catch (P2PPException ex) {
            Assert.fail(ex.getMessage());
        }
        InetSocketAddress serverAddr = new InetSocketAddress(
                "127.0.0.1", MetaConfiguration.getP2ppConfiguration().getNetworkConfig().getPort());
        serverPeer = new MetaPeer(null, serverAddr);
    }

    /**
     * Just adds a few entries in the model to be retrieved using P2PP.
     */
    public static void fillModel() {
        result = model.getFactory().getData("result");

        MetaDataMap mdMap = new MetaDataMap();
        mdMap.put(new MetaData("key", "value"));
        mdMap.put("a", "b");
        result.setMetaData(mdMap);

        result2 = model.getFactory().getData("result2");
        //result2 has no meta-data

        Data source = model.getFactory().getData("source");

        SearchCriteria criteria = model.getFactory().createCriteria(new MetaData("key", "value"));

        SearchCriteria criteria2 = model.getFactory().createCriteria(new MetaData("name", "crit"));

        Search search = model.getFactory().createSearch(source, criteria, Collections.singletonList(result));

        Search search2 = model.getFactory().createSearch(source, criteria2, Collections.singletonList(result2));

        searchHash = search.getHash();

        searchHash2 = search2.getHash();

        logger.debug("Storing search: " + searchHash);
        logger.debug("Storing search2: " + searchHash2);
        logger.debug("Result hash: " + result.getHash() + " result data size: " + result.getSize());
        logger.debug("Result2 hash: " + result2.getHash() + " result2 data size: " + result2.getSize());

        dataFile = model.getFactory().getDataFile(new File("/home/nico/Documents/projects/jmeta/AUTHORS")); // TODO change

        if (!model.set(search) || !model.set(search2) || !model.set(dataFile)) {
            Assert.fail("Failed to model items");
        }
    }

    @Before
    public void removeContext() {
        //client.closeConnection(serverPeer);
    }

    @Test
    public void simpleSearchTest() {
        logger.info("BEGIN TEST: simpleSearchTest");
        SearchOperation op = metaP2ppClient.search(serverPeer, null, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                logger.debug("Search complete!");
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    Assert.assertEquals("Retrieved result hash is different!", result.getHash(), data.getHash());
                    Assert.assertEquals("Retrieved result size is different!", result.getSize(), data.getSize());
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchTestRefinementFailure() {
        logger.info("BEGIN TEST: simpleSearchTest");
        //validate metadata refinement
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("key", "value2");
        filter.put("a", "b");
        SearchOperation op = metaP2ppClient.search(serverPeer, filter, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                logger.debug("Search complete!");
                Assert.assertEquals("There should be 0 fetched datas", 0, results.size());
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchTestRefinementSuccess1() {
        logger.info("BEGIN TEST: simpleSearchTest");
        //validate metadata refinement
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("a", "b");
        SearchOperation op = metaP2ppClient.search(serverPeer, filter, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                Assert.assertEquals("There should be 1 fetched datas", 1, results.size());
                logger.debug("Search complete!");
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    Assert.assertEquals("Retrieved result hash is different!", result.getHash(), data.getHash());
                    Assert.assertEquals("Retrieved result size is different!", result.getSize(), data.getSize());
                    Assert.assertEquals("MetaData value is wrong", result.getMetaData("key"), "value");
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchTestRefinementSuccess2() {
        logger.info("BEGIN TEST: simpleSearchTest");
        //validate metadata refinement
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("key", "value");
        filter.put("a", "b");
        SearchOperation op = metaP2ppClient.search(serverPeer, filter, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                Assert.assertEquals("There should be 1 fetched datas", 1, results.size());
                logger.debug("Search complete!");
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    Assert.assertEquals("Retrieved result hash is different!", result.getHash(), data.getHash());
                    Assert.assertEquals("Retrieved result size is different!", result.getSize(), data.getSize());
                    Assert.assertEquals("MetaData value is wrong", result.getMetaData("key"), "value");
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchMetaTest() {
        logger.info("BEGIN TEST: simpleSearchMetaTest");
        Set<String> metaDataKeys = new HashSet<>();
        //only ask for a sub-set of results meta-data
        metaDataKeys.add("key");
        SearchOperation op = metaP2ppClient.searchMeta(serverPeer, null, metaDataKeys, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                logger.debug("Search complete!");
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    for (MetaData md : data.getMetaDataMap()) {
                        logger.info("Retrieved meta-data: " + md.getKey() + ":" + md.getValue());
                        if (md.getKey().equals("key")) {
                            //Assert.assertEquals(md.getValue(), P2PPTest.result.getMetaDataMap().c);
                        }
                    }
                    Assert.assertEquals("Retrieved result hash is different!", result.getHash(), data.getHash());
                    Assert.assertEquals("Retrieved result size is different!", result.getSize(), data.getSize());
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchMetaTestRefinementFailure() {
        logger.info("BEGIN TEST: simpleSearchMetaTest");
        Set<String> metaDataKeys = new HashSet<>();
        //only ask for a sub-set of results meta-data
        metaDataKeys.add("key");
        //validate metadata refinement
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("key", "value2");
        filter.put("a", "b");
        SearchOperation op = metaP2ppClient.searchMeta(serverPeer, filter, metaDataKeys, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                logger.debug("Search complete!");
                Assert.assertEquals("There should be 0 fetched datas", 0, results.size());
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchMetaTestRefinementSucess1() {
        logger.info("BEGIN TEST: simpleSearchMetaTest");
        Set<String> metaDataKeys = new HashSet<>();
        //only ask for a sub-set of results meta-data
        metaDataKeys.add("key");
        //validate metadata refinement
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("a", "b");
        SearchOperation op = metaP2ppClient.searchMeta(serverPeer, filter, metaDataKeys, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                logger.debug("Search complete!");
                Assert.assertEquals("There should be 1 fetched datas", 1, results.size());
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    for (MetaData md : data.getMetaDataMap()) {
                        logger.info("Retrieved meta-data: " + md.getKey() + ":" + md.getValue());
                        if (md.getKey().equals("key")) {
                            //Assert.assertEquals(md.getValue(), P2PPTest.result.getMetaDataMap().c);
                        }
                    }
                    Assert.assertEquals("Retrieved result hash is different!", result.getHash(), data.getHash());
                    Assert.assertEquals("Retrieved result size is different!", result.getSize(), data.getSize());
                    Assert.assertEquals("MetaData value is wrong", result.getMetaData("key"), "value");
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchMetaTestRefinementSucess2() {
        logger.info("BEGIN TEST: simpleSearchMetaTest");
        Set<String> metaDataKeys = new HashSet<>();
        //only ask for a sub-set of results meta-data
        metaDataKeys.add("key");
        //validate metadata refinement
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("key", "value");
        filter.put("a", "b");
        SearchOperation op = metaP2ppClient.searchMeta(serverPeer, filter, metaDataKeys, searchHash);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                logger.debug("Search complete!");
                Assert.assertEquals("There should be 1 fetched datas", 1, results.size());
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    for (MetaData md : data.getMetaDataMap()) {
                        logger.info("Retrieved meta-data: " + md.getKey() + ":" + md.getValue());
                        if (md.getKey().equals("key")) {
                            //Assert.assertEquals(md.getValue(), P2PPTest.result.getMetaDataMap().c);
                        }
                    }
                    Assert.assertEquals("Retrieved result hash is different!", result.getHash(), data.getHash());
                    Assert.assertEquals("Retrieved result size is different!", result.getSize(), data.getSize());
                    Assert.assertEquals("MetaData value is wrong", result.getMetaData("key"), "value");
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void twoHashSearchMetaTest() {
        logger.info("BEGIN TEST: twoHashSearchMetaTest");
        Set<String> metaDataKeys = new HashSet<>();
        //only ask for a sub-set of results meta-data
        metaDataKeys.add("key");
        SearchOperation op = metaP2ppClient.searchMeta(serverPeer, null, metaDataKeys, searchHash, searchHash2);
        op.addListener(new OperationListener<SearchOperation>() {
            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }
            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();
                logger.debug("two-hash Search complete!");
                Assert.assertEquals("There should be 2 results", results.size(), 2);
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    for (MetaData md : data.getMetaDataMap()) {
                        logger.info("Retrieved meta-data: " + md.getKey() + ":" + md.getValue());
                    }
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void simpleSearchGetTest() {
        logger.info("BEGIN TEST: simpleSearchGetTest");
        Set<String> metaDataKeys = new HashSet<>();
        //only ask for a sub-set of results meta-data
        metaDataKeys.add("key");
        metaDataKeys.add("inexistent");
        SearchOperation op = metaP2ppClient.searchGet(serverPeer, null, metaDataKeys, searchHash);

        op.addListener(new OperationListener<SearchOperation>() {

            @Override
            public void failed(final SearchOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }

            @Override
            public void complete(final SearchOperation operation) {
                Collection<Data> results = operation.getResults();

                logger.debug("Search complete!");
                for (Data data : results) {
                    logger.info("Result hash: " + data.getHash());
                    logger.info("Result data content = " + data.toString());
                    for (MetaData md : data.getMetaDataMap()) {
                        logger.info("Retrieved meta-data: " + md.getKey() + ":" + md.getValue());
                    }
                    Assert.assertEquals("Retrieved result hash is different!", result.getHash(),
                            data.getHash());
                    Assert.assertEquals("Retrieved result size is different!", result.getSize(),
                            data.getSize());
                }
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void testKeepAlive() {
        logger.info("BEGIN TEST: testKeepAlive");
        AsyncOperation op = metaP2ppClient.keepAlive(serverPeer);

        op.addListener(new OperationListener<AsyncOperation>() {

            @Override
            public void failed(final AsyncOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }

            @Override
            public void complete(final AsyncOperation operation) {
                logger.debug("Keep alive complete!");
            }
        });
        op.awaitUninterruptibly();
    }

    @Test
    public void testStackKeepAlive() {
        logger.info("BEGIN TEST: testStackKeepAlive");
        AsyncOperation op = null;
        OperationListener listener = new OperationListener<AsyncOperation>() {

            @Override
            public void failed(final AsyncOperation operation) {
                logger.debug("FAILED " + operation.getFailureMessage());
                Assert.fail(operation.getFailureMessage());
            }

            @Override
            public void complete(final AsyncOperation operation) {
                logger.debug("Keep alive complete!");
            }
        };
        for (int i = 0; i < P2PPConstants.CONCURRENT_CLIENT_REQUESTS * 2; ++i) {
            op = metaP2ppClient.keepAlive(serverPeer);
            op.addListener(listener);
        }
        op.awaitUninterruptibly();
    }

    @Test
    public void testSimpleGet() {
        logger.info("BEGIN TEST: testSimpleGet");
        DataFileAccessor accessor = DataFileAccessors.getAccessor(dataFile);
        if (accessor.getPieceCount() != 1 || accessor.blockSize(0, 0) > accessor.BLOCK_SIZE) {
            Assert.fail("testSimpleGet should be given a valid, single piece/block datafile");
        }
        GetOperation op = metaP2ppClient.get(serverPeer, dataFile.getHash(), 0, 0, dataFile.getSize());
        op.addListener(new OperationListener<GetOperation>() {
            @Override
            public void failed(final GetOperation operation) {
                Assert.fail("Failed to get block from server peer: " + operation.getFailureMessage());
            }

            @Override
            public void complete(final GetOperation operation) {
                operation.getData().rewind();
                logger.debug("GET block complete! Read data UTF-8 =" + SerializationUtils.decodeUTF8(operation.getData()));
            }
        });
        op.awaitUninterruptibly();
    }

    @AfterClass
    public static void close() {
        logger.debug("Shuting down everything.");
        manager.getServer().close();
        model.close();
    }

}
