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
package org.meta.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Just simple static utility methods for use by tests.
 *
 * @author dyslesiq
 */
public class TestUtils {

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final Random rnd = new Random();

    // temporary directory location
    public static final Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

    //All meta-related tests files will be created under this directory
    public static final Path metaTmpDir = Paths.get(tmpDir.toString(), "meta-tests");

    private TestUtils() {
    }

    /**
     * Look for a usable network interface address to use for tests. It must be routable (even locally). Falls
     * back to '127.0.0.1'
     *
     * //TODO check if local addr choice as something to do with failed tests on debian.
     *
     * @return The local inetAddress for tests
     *
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static InetAddress getLocalAddress() throws UnknownHostException, SocketException {
//        InetAddress localAddr = null;
//        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//
//        for (NetworkInterface netIf : Collections.list(networkInterfaces)) {
//            if (netIf.isUp()) {
//                for (InetAddress ifAddr : Collections.list(netIf.getInetAddresses())) {
//
//                    if (ifAddr instanceof Inet4Address) {
//                        //We prefer ipv4 for tests...
//                        localAddr = ifAddr;
//                        break;
//                    }
//                }
//                if (localAddr != null) {
//                    break;
//                }
//            }
//        }
//        if (localAddr == null) {
//            localAddr = InetAddress.getByName("localhost");
//        }
        return InetAddress.getByName("127.0.0.1");
        //return localAddr;
    }

    /**
     * Creates a random alpha-numeric String of given length
     *
     * @param size the length of the returned random String
     * @return the random String
     */
    public static String getRandomString(final int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    /**
     * Fills the given file with the given String.
     *
     * @param file the file to write to
     * @param toWrite the String to write
     */
    public static void writeToFile(final File file, final String toWrite) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.write(toWrite);
            writer.flush();
        }
    }

    /**
     * Writes random bytes to a file.
     *
     * @param size the number of random bytes to write
     */
    public static void writeRandom(final File file, final int size) throws FileNotFoundException {
        String rndString = getRandomString(size);
        writeToFile(file, rndString);
    }

    /**
     * Creates a temporary file with the given name and size.
     *
     * @param name the name. This will be prefixed by the current time in Milli-seconds
     * @param size the size of the new File to be created
     * @return the newly created temporary file
     * @throws java.io.IOException if an error occurred
     */
    public static File createTempFile(final String name, final int size) throws IOException {
        Path tempFilePath = Paths.get(metaTmpDir.toString(), name);
        if (!Files.exists(tempFilePath)) {
            Files.createFile(tempFilePath);
        }
        File tempFile = tempFilePath.toFile();

        if (size > 0) {
            new RandomAccessFile(tempFile, "rw").setLength(size);
        }
        return tempFile;
    }

    /**
     * Creates a temporary directory.
     *
     * @param name the name. This will be prefixed by the current time in Milli-seconds
     * @return the newly created directory
     * @throws IOException if an error occurred
     */
    public static File createTempDir(final String name) throws IOException {
        Path tempFilePath = Paths.get(metaTmpDir.toString(), name);
        Files.createDirectories(tempFilePath);

        return tempFilePath.toFile();
    }

    /**
     * Creates a temporary file with the given name and size.
     *
     * The content of the file will be a randomly-generated alpha-numeric String.
     *
     * @param name the name of the file.
     * @param size the size of the new File to be created
     * @return the newly created temporary file
     * @throws java.io.IOException if an error occurred
     */
    public static File createRandomTempFile(final String name, final int size) throws IOException {
        File tempFile = createTempFile(name, size);
        if (size > 0) {
            writeRandom(tempFile, size);
        }
        return tempFile;
    }

}
