/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.sqlx.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for common I/O operations such as reading from streams, copying data between streams,
 * and handling resource files.
 *
 * @author He Xing Mo
 * @since 1.0
 */

@Slf4j
public class IOUtils {

    /**
     * Default buffer size used for reading and copying operations.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private IOUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Reads the content of an InputStream into a String using UTF-8 encoding.
     *
     * @param in the InputStream to read from
     * @return the content of the InputStream as a String
     */
    public static String read(InputStream in) {
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        return read(reader);
    }

    /**
     * Checks if a resource exists in the classpath.
     *
     * @param resource the path to the resource
     * @return true if the resource exists, false otherwise
     */
    public static boolean exists(String resource) {
        return IOUtils.class.getClassLoader().getResource(resource) != null;
    }

    /**
     * Reads the content of a resource file into a String.
     *
     * @param resource the path to the resource file
     * @return the content of the resource file as a String
     */
    public static String readFromResource(String resource) {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (in == null) {
                in = IOUtils.class.getResourceAsStream(resource);
            }

            if (in == null) {
                return null;
            }

            return IOUtils.read(in);
        } finally {
            close(in);
        }
    }

    /**
     * Reads the content of a resource file into a byte array.
     *
     * @param resource the path to the resource file
     * @return the content of the resource file as a byte array
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readByteArrayFromResource(String resource) throws IOException {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (in == null) {
                return null;
            }

            return readByteArray(in);
        } finally {
            close(in);
        }
    }

    /**
     * Reads the content of an InputStream into a byte array.
     *
     * @param input the InputStream to read from
     * @return the content of the InputStream as a byte array
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * Copies data from an InputStream to an OutputStream.
     *
     * @param input  the InputStream to read from
     * @param output the OutputStream to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        final int EOF = -1;

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Reads the content of a Reader into a String.
     *
     * @param reader the Reader to read from
     * @return the content of the Reader as a String
     */
    public static String read(Reader reader) {
        try {
            StringWriter writer = new StringWriter();

            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }

            return writer.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("read error", ex);
        }
    }

    /**
     * Reads a specified number of characters from a Reader into a String.
     *
     * @param reader the Reader to read from
     * @param length the number of characters to read
     * @return the content of the Reader as a String
     */
    public static String read(Reader reader, int length) {
        try {
            char[] buffer = new char[length];

            int offset = 0;
            int rest = length;
            int len;
            while ((len = reader.read(buffer, offset, rest)) != -1) {
                rest -= len;
                offset += len;

                if (rest == 0) {
                    break;
                }
            }

            return new String(buffer, 0, length - rest);
        } catch (IOException ex) {
            throw new IllegalStateException("read error", ex);
        }
    }

    /**
     * Closes a Closeable resource safely, logging any exceptions that occur.
     *
     * @param x the Closeable resource to close
     */
    public static void close(Closeable x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            log.debug("close error", e);
        }
    }
}
