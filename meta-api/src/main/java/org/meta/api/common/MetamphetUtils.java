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
public final class MetamphetUtils {

    private static final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(MetamphetUtils.class);

    private MetamphetUtils() {

    }

    /**
     * Computes a {@link MetHash} for the given file.
     *
     *
     * @param file The file to hash.
     *
     * @return the hash of the file, or the ZERO value of a MetHash if an error occurs.
     */
    public static MetHash makeSHAHash(final File file) {
        FileInputStream fis = null;
        FileChannel channel = null;
        try {
            fis = new FileInputStream(file);
            channel = fis.getChannel();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (long offest = 0; offest < channel.size(); offest += 64 * 1024) {
                ByteBuffer buff;
                if (channel.size() - offest < 64 * 1024) {
                    buff = channel.map(FileChannel.MapMode.READ_ONLY, offest, (int) channel.size() - offest);
                } else {
                    buff = channel.map(FileChannel.MapMode.READ_ONLY, offest, 64 * 1024);
                }
                md.update(buff);
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
     * @return the hash of the file, or the ZERO value of a MetHash if an error occurs.
     */
    public static MetHash makeSHAHash(final String strInput) {
        byte[] buffer = strInput.getBytes();
        return makeSHAHash(buffer);
    }

    /**
     * Computes a {@link MetHash} for the given byte buffer.
     *
     * @param buffer The buffer to hash.
     *
     * @return the hash of the buffer, or the ZERO value of a MetHash if an error occurs.
     */
    public static MetHash makeSHAHash(final ByteBuffer buffer) {
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
     * @return the hash of the byte array, or the ZERO value of a MetHash if an error occurs.
     */
    public static MetHash makeSHAHash(final byte[] buffer) {
        return makeSHAHash(ByteBuffer.wrap(buffer));
    }

    /**
     * Computes a {@link MetHash} for the given byte array with given offset and length.
     *
     * @param buffer The array to hash.
     * @param offset The offset in buffer.
     * @param length The length of the portion to be hashed inside buffer.
     *
     * @return the hash of the byte array, or the ZERO value of a MetHash if an error occurs.
     */
    public static MetHash makeSHAHash(final byte[] buffer, final int offset, final int length) {
        return makeSHAHash(ByteBuffer.wrap(buffer, offset, length));
    }

    /**
     * Creates a random hash.
     *
     * TODO improve random!
     *
     * @return The randomly initialized hash.
     */
    public static MetHash createRandomHash() {
        // TODO this hardcoded, bad style
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
     * @return true if the bloc's hash matches the given expected hash, false otherwise.
     */
    public static boolean checkHash(final String hash, final byte[] bloc) {
        //TODO
        return true;
    }

    /**
     * Utility function to close all given Closeable at once.
     *
     * @param closables The closeable array to close.
     */
    public static void close(final Closeable... closables) {
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
