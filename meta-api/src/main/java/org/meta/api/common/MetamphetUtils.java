/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.api.common;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * :D
 *
 * Class holding static Utility methods for hash and SHA1.
 */
public class MetamphetUtils {

    private static final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(MetamphetUtils.class);

    /**
     * Computes a {@link MetHash} for the given file.
     *
     *
     * @param file The file to hash.
     *
     * @return the hash of the file, or the ZERO value of a MetHash if an error
     * occurs.
     */
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
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            return MetHash.ZERO;
        } finally {
            close(channel, fis);
        }
    }

    /**
     * Computes a {@link MetHash} for the given file.
     *
     * @param strInput The string to hash.
     *
     * @return the hash of the file, or the ZERO value of a MetHash if an error
     * occurs.
     */
    public static MetHash makeSHAHash(String strInput) {
        byte[] buffer = strInput.getBytes();
        return makeSHAHash(buffer);
    }

    /**
     * Computes a {@link MetHash} for the given byte buffer.
     *
     * @param buffer The buffer to hash.
     *
     * @return the hash of the buffer, or the ZERO value of a MetHash if an
     * error occurs.
     */
    public static MetHash makeSHAHash(ByteBuffer buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(buffer);
            byte[] digest = md.digest();
            return new MetHash(digest);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            return MetHash.ZERO;
        }
    }

    /**
     * Computes a {@link MetHash} for the given byte array.
     *
     * @param buffer The array to hash.
     *
     * @return the hash of the byte array, or the ZERO value of a MetHash if an
     * error occurs.
     */
    public static MetHash makeSHAHash(byte[] buffer) {
        return makeSHAHash(ByteBuffer.wrap(buffer));
    }

    /**
     * Computes a {@link MetHash} for the given byte array with given offset and
     * length.
     *
     * @param buffer The array to hash.
     * @param offset The offset in buffer.
     * @param length The length of the portion to be hashed inside buffer.
     *
     * @return the hash of the byte array, or the ZERO value of a MetHash if an
     * error occurs.
     */
    public static MetHash makeSHAHash(byte[] buffer, int offset, int length) {
        return makeSHAHash(ByteBuffer.wrap(buffer, offset, length));
    }

    /**
     * Creates a random hash.
     * 
     * TODO improve random ?
     * 
     * @return The randomly initialized hash.
     */
    public static MetHash createRandomHash() {
        // TODO: this hardcoded, bad style
        byte[] me = new byte[20];
        random.nextBytes(me);
        MetHash id = new MetHash(me);
        return id;
    }

    /**
     * Checks if the given hash equals the hash of the given byte array.
     * 
     * @param hash The hash to check.
     * @param bloc The block to hash and check.
     *
     * @return true if the bloc's hash matches the given expected hash, false
     * otherwise.
     */
    public static boolean checkHash(String hash, byte[] bloc) {
        //TODO
        return true;
    }

    /**
     * Utility function to close all given Closeable at once.
     * 
     * @param closables The closeable array to close.
     */
    public static void close(Closeable... closables) {
        // best effort close;
        for (Closeable closable : closables) {
            if (closable != null) {
                try {
                    closable.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
