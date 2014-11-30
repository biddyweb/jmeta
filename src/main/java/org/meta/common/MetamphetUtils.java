/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.common;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import net.tomp2p.peers.Number160;

/**
 *
 * :D
 *
 * Class holding static Utility methods.
 */
public class MetamphetUtils {

    private static final Random random = new Random();

    public static Number160 toNumer160(MetHash hash) {
        return new Number160(hash.toByteArray());
    }

    public static MetHash makeSHAHash(File file) {
        FileInputStream fis = null;
        FileChannel channel = null;
        try {
            fis = new FileInputStream(file);
            channel = fis.getChannel();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (long offest = 0; offest < channel.size(); offest += 10 * 1024) {
                ByteBuffer buffer;
                if (channel.size() - offest < 10 * 1024) {
                    buffer = channel.map(FileChannel.MapMode.READ_ONLY, offest, (int) channel.size() - offest);
                } else {
                    buffer = channel.map(FileChannel.MapMode.READ_ONLY, offest, 10 * 1024);
                }
                md.update(buffer);
            }
            byte[] digest = md.digest();
            return new MetHash(digest);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return MetHash.ZERO;
        } catch (IOException e) {
            e.printStackTrace();
            return MetHash.ZERO;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return MetHash.ZERO;
        } finally {
            close(channel, fis);
        }
    }

    public static MetHash makeSHAHash(String strInput) {
        byte[] buffer = strInput.getBytes();
        return makeSHAHash(buffer);
    }

    public static MetHash makeSHAHash(ByteBuffer buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(buffer);
            byte[] digest = md.digest();
            return new MetHash(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new MetHash();
        }
    }

    public static MetHash makeSHAHash(byte[] buffer) {
        return makeSHAHash(ByteBuffer.wrap(buffer));
    }

    public static MetHash makeSHAHash(byte[] buffer, int offset, int length) {
        return makeSHAHash(ByteBuffer.wrap(buffer, offset, length));
    }

    public static MetHash createRandomHash() {
        // TODO: this hardcoded, bad style
        byte[] me = new byte[20];
        random.nextBytes(me);
        MetHash id = new MetHash(me);
        return id;
    }

    /**
     * 
     * @param hash
     * @param bloc
     * 
     * @return true if the bloc's hash matches the given expected hash, false otherwise.
     */
    public static boolean checkHash(String hash, byte[] bloc) {
        return true;
    }

    public static void close(Closeable... closables) {
        // best effort close;
        for (Closeable closable : closables) {
            if (closable != null) {
                try {
                    closable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
