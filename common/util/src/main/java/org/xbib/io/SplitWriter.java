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
package org.xbib.io;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer that can switch over to a new writer. This is useful for example,
 * when a large file must be split into smaller ones.
 *
 * Large XML files produced by a {@link javax.xml.transform.stream.StreamResult}
 * can be split by manipulating the SAX handler. At the split condition, the
 * handler should send endDocument(), then performing a flush on the
 * StreamResult Writer, split with this Writerm and then continue 
 * with startDocument().
 *
 * Most of the code is copied from OpenJDK's BufferedWriter. Because
 * BufferedWriter uses a private instance of the internal writer, it can not be
 * extended.
 */
public class SplitWriter extends Writer {

    private Writer out;
    private char cb[];
    private int nChars, nextChar;

    /**
     * Creates a buffered character-output stream that uses a default-sized
     * output buffer.
     *
     * @param out A Writer
     */
    public SplitWriter(Writer out) {
        this(out, 8192);
    }

    /**
     * Creates a new buffered character-output stream that uses an output buffer
     * of the given size.
     *
     * @param out A Writer
     * @param sz Output-buffer size, a positive integer
     *
     * @exception IllegalArgumentException If sz is <= 0
     */
    public SplitWriter(Writer out, int sz) {
        this.lock = this;
        this.out = out;
        cb = new char[sz];
        nChars = sz;
        nextChar = 0;
    }

    /**
     * Checks to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (out == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Flushes the output buffer to the underlying character stream, without
     * flushing the stream itself. This method is non-private only so that it
     * may be invoked by PrintStream.
     */
    void flushBuffer() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar == 0) {
                return;
            }
            out.write(cb, 0, nextChar);
            nextChar = 0;
        }
    }

    /**
     * Writes a single character.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void write(int c) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (nextChar >= nChars) {
                flushBuffer();
            }
            cb[nextChar++] = (char) c;
        }
    }

    /**
     * Our own little min method, to avoid loading java.lang.Math if we've run
     * out of file descriptors and we're trying to print a stack trace.
     */
    private int min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    /**
     * Writes a portion of an array of characters.
     *
     * <p> Ordinarily this method stores characters from the given array into
     * this stream's buffer, flushing the buffer to the underlying stream as
     * needed. If the requested length is at least as large as the buffer,
     * however, then this method will flush the buffer and write the characters
     * directly to the underlying stream. Thus redundant
     * <code>BufferedWriter</code>s will not copy data unnecessarily.
     *
     * @param cbuf A character array
     * @param off Offset from which to start reading characters
     * @param len Number of characters to write
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void write(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0)
                    || ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }

            if (len >= nChars) {
                /*
                 * If the request length exceeds the size of the output buffer,
                 * flush the buffer and then write the data directly. In this
                 * way buffered streams will cascade harmlessly.
                 */
                flushBuffer();
                out.write(cbuf, off, len);
                return;
            }

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                System.arraycopy(cbuf, b, cb, nextChar, d);
                b += d;
                nextChar += d;
                if (nextChar >= nChars) {
                    flushBuffer();
                }
            }
        }
    }

    /**
     * Writes a portion of a String.
     *
     * <p> If the value of the <tt>len</tt> parameter is negative then no
     * characters are written. This is contrary to the specification of this
     * method in the {@linkplain java.io.Writer#write(java.lang.String,int,int)
     * superclass}, which requires that an {@link IndexOutOfBoundsException} be
     * thrown.
     *
     * @param s String to be written
     * @param off Offset from which to start reading characters
     * @param len Number of characters to be written
     *
     * @exception IOException If an I/O error occurs
     */
    public void write(String s, int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();

            int b = off, t = off + len;
            while (b < t) {
                int d = min(nChars - nextChar, t - b);
                s.getChars(b, b + d, cb, nextChar);
                b += d;
                nextChar += d;
                if (nextChar >= nChars) {
                    flushBuffer();
                }
            }
        }
    }

    /**
     * Flushes the stream.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            flushBuffer();
            out.flush();
        }
    }

    /**
     * Closes the stream.
     *
     * @exception IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (out == null) {
                return;
            }
            try {
                flushBuffer();
            } finally {
                out.close();
                out = null;
                cb = null;
            }
        }
    }

    /**
     * Split this Writer by stopping writing with the current Writer and
     * continue with a new Writer. The buffer is flushed, the writer closed, and
     * the internal buffer is refreshed. All operations after the split will be
     * taken on the new Writer.
     *
     * @param writer the new Writer
     * @throws IOException If an I/O error occurs
     */
    public void split(Writer writer) throws IOException {
        synchronized (lock) {
            if (out != null) {
                try {
                    flushBuffer();
                } finally {
                    out.flush();
                    out.close();
                    cb = new char[nChars];
                    nextChar = 0;
                }
            }
            out = writer;
        }
    }
}
