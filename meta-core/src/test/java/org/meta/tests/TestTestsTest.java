package org.meta.tests;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.SortedSet;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.common.OperationListener;
import org.meta.api.model.ModelStorage;
import org.meta.api.storage.CollectionStorage;
import org.meta.api.storage.KVMapStorage;
import org.meta.api.storage.MetaDatabase;
import org.meta.dht.tomp2p.storage.TomP2pStorage;
import org.meta.model.MetaFile;
import org.meta.model.files.DataFileAccessor;
import org.meta.model.files.DataFileAccessors;
import org.meta.model.files.FileWriteOperation;
import org.meta.p2pp.client.SocketIOState;
import org.meta.storage.BerkeleyKVStorage;
import org.meta.storage.exceptions.StorageException;
import org.meta.api.storage.Serializers;
import static org.meta.tests.MetaBaseTests.getDatabase;
import org.meta.tests.storage.CollectionStorageTest;
import org.meta.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just a class to test stuff but just for fun and dev and no maven and stuff... voila.
 *
 * @author dyslesiq
 */
public class TestTestsTest extends MetaBaseTests {

    private static Logger logger = LoggerFactory.getLogger(TestTestsTest.class);

    private static BerkeleyKVStorage storage;
    private static ModelStorage model;

    private static MetaDatabase db;

    private static CollectionStorage<String> collectionStorage;

    private static KVMapStorage<String, String> map;

    @BeforeClass
    public static void setup() {
        try {
            db = getDatabase(CollectionStorageTest.class.getSimpleName());
            map = db.getKVMapStorage(CollectionStorageTest.class.getSimpleName(), Serializers.STRING, Serializers.STRING);
            collectionStorage = db.getCollection(CollectionStorageTest.class.getSimpleName(), Serializers.STRING);
        } catch (StorageException | IOException ex) {
            Assert.fail("Failed to initialize storage");
        }
    }

    //@Test
    public void testWriteOffset() throws URISyntaxException {
        int fileSize = 1024 * 1024 * 1024; //1GB
        URI fileUri = new URI("file:/home/nico/META_TEST");
        MetaFile f = new MetaFile(MetamphetUtils.createRandomHash(), fileUri, fileSize);
        DataFileAccessor dfAccessor = DataFileAccessors.getAccessor(f);

        ByteBuffer data = SerializationUtils.encodeUTF8("TOTO\n");
        FileWriteOperation fwOp = dfAccessor.write(2, 0, data);
        fwOp.addListener(new OperationListener<FileWriteOperation>() {

            @Override
            public void failed(final FileWriteOperation operation) {
                Assert.fail("Failed to write at offset > file size!!" + operation.getFailureMessage());
            }

            @Override
            public void complete(final FileWriteOperation operation) {
                System.out.println("SUCCESS!!!");
            }
        });
        fwOp.awaitUninterruptibly();
        System.out.println("FINISHED WAITING!");
    }

    //@Test
    public void metHashSerializationTest() {
        MetHash hash = MetamphetUtils.createRandomHash();

        MetHash hashFromByteArray = new MetHash(hash.toByteArray());
        Assert.assertEquals(hash, hashFromByteArray);
        ByteBuffer buf = ByteBuffer.wrap(hash.toByteArray());
        MetHash hashFromByteBuffer = new MetHash(buf, (short) MetHash.BYTE_ARRAY_SIZE);
        Assert.assertEquals(hash, hashFromByteBuffer);

        MetHash hashFromString = new MetHash(hash.toString());
        Assert.assertEquals(hash, hashFromString);

    }

    //@Test
    public void storageMixTest() {
        map.put(null, "abcd", "efgh");
        map.put(null, "bcde", "toto");
        map.put(null, "cdef", "yup");
        map.put(null, "defg", "course");

        SortedSet<String> s = collectionStorage.subSet("bcde", "cdef");

        for (String str : s) {
            logger.info("Collection STR = " + str);
        }
        s.clear();
        logger.info("AFTER CLEAR Map[bcde] == " + map.get("bcde"));
    }

    //@Test
    public void number640ComparatorTest() {
        TomP2pStorage p2pStorage = new TomP2pStorage(db);

        Random r = new Random();
        Number640 key = new Number640(r);

        Data d = new Data("toto".getBytes());
        d.ttlSeconds(3600);
        logger.info("GET: " + p2pStorage.get(key));
        p2pStorage.put(key, d);
        logger.info("GET: " + p2pStorage.get(key));
    }

    @Test
    public void IoStateTest() {
        SocketIOState ioState = new SocketIOState();

        Assert.assertFalse(ioState.isConnecting());
        Assert.assertFalse(ioState.isConnected());
        Assert.assertFalse(ioState.isReading());
        Assert.assertFalse(ioState.isWriting());

        ioState.connecting();
        Assert.assertTrue(ioState.isConnecting());
        ioState.connected();
        Assert.assertTrue(ioState.isConnected());
        ioState.reading();
        Assert.assertTrue(ioState.isReading());
        ioState.writing();
        Assert.assertTrue(ioState.isWriting());

        ioState.writing(false);
        Assert.assertFalse(ioState.isWriting());
        ioState.reading(false);
        Assert.assertFalse(ioState.isReading());
        ioState.connecting(false);
        Assert.assertFalse(ioState.isConnecting());
        ioState.connected(false);
        Assert.assertFalse(ioState.isConnecting());
    }

}
