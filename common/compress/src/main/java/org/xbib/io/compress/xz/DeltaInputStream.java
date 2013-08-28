/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.io.compress.xz;

import java.io.InputStream;
import java.io.IOException;
import org.xbib.io.compress.xz.delta.DeltaDecoder;

/**
 * Decodes raw Delta-filtered data (no XZ headers).
 *
 * The delta filter doesn't change the size of the data and thus it
 * cannot have an end-of-payload marker. It will simply decode until
 * its input stream indicates end of input.
 */
public class DeltaInputStream extends InputStream {
    /**
     * Smallest supported delta calculation distance.
     */
    public static final int DISTANCE_MIN = 1;

    /**
     * Largest supported delta calculation distance.
     */
    public static final int DISTANCE_MAX = 256;

    private InputStream in;
    private final DeltaDecoder delta;

    private IOException exception = null;

    /**
     * Creates a new Delta decoder with the given delta calculation distance.
     *
     * @param       in          input stream from which Delta filtered data
     *                          is read
     *
     * @param       distance    delta calculation distance, must be in the
     *                          range [<code>DISTANCE_MIN</code>,
     *                          <code>DISTANCE_MAX</code>]
     */
    public DeltaInputStream(InputStream in, int distance) {
        // Check for null because otherwise null isn't detect
        // in this constructor.
        if (in == null)
            throw new NullPointerException();

        this.in = in;
        this.delta = new DeltaDecoder(distance);
    }

    /**
     * Decode the next byte from this input stream.
     *
     * @return      the next decoded byte, or <code>-1</code> to indicate
     *              the end of input on the input stream <code>in</code>
     *
     * @throws      IOException may be thrown by <code>in</code>
     */
    public int read() throws IOException {
        byte[] buf = new byte[1];
        return read(buf, 0, 1) == -1 ? -1 : (buf[0] & 0xFF);
    }

    /**
     * Decode into an array of bytes.
     * <p>
     * This calls <code>in.read(buf, off, len)</code> and defilters the
     * returned data.
     *
     * @param       buf         target buffer for decoded data
     * @param       off         start offset in <code>buf</code>
     * @param       len         maximum number of bytes to read
     *
     * @return      number of bytes read, or <code>-1</code> to indicate
     *              the end of the input stream <code>in</code>
     *
     * @throws      XZIOException if the stream has been closed
     *
     * @throws      IOException may be thrown by underlaying input
     *                          stream <code>in</code>
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        if (len == 0)
            return 0;

        if (in == null)
            throw new XZIOException("Stream closed");

        if (exception != null)
            throw exception;

        int size;
        try {
            size = in.read(buf, off, len);
        } catch (IOException e) {
            exception = e;
            throw e;
        }

        if (size == -1)
            return -1;

        delta.decode(buf, off, size);
        return size;
    }

    /**
     * Calls <code>in.available()</code>.
     *
     * @return      the value returned by <code>in.available()</code>
     */
    public int available() throws IOException {
        if (in == null)
            throw new XZIOException("Stream closed");

        if (exception != null)
            throw exception;

        return in.available();
    }

    /**
     * Closes the stream and calls <code>in.close()</code>.
     * If the stream was already closed, this does nothing.
     *
     * @throws  IOException if thrown by <code>in.close()</code>
     */
    public void close() throws IOException {
        if (in != null) {
            try {
                in.close();
            } finally {
                in = null;
            }
        }
    }
}
