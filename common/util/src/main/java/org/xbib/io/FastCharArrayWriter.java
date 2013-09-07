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
import java.util.Arrays;

/**
 * A similar class to {@link java.io.CharArrayWriter} allowing to get the underlying <tt>char[]</tt> buffer.
 */
public class FastCharArrayWriter extends Writer {

    /**
     * The buffer where data is stored.
     */
    protected char buf[];

    /**
     * The number of chars in the buffer.
     */
    protected int count;

    /**
     * Creates a new CharArrayWriter.
     */
    public FastCharArrayWriter() {
        this(1024);
    }

    /**
     * Creates a new CharArrayWriter with the specified initial size.
     *
     * @param initialSize an int specifying the initial buffer size.
     * @throws IllegalArgumentException if initialSize is negative
     */
    public FastCharArrayWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: "
                    + initialSize);
        }
        buf = new char[initialSize];
    }

    /**
     * Writes a character to the buffer.
     */
    public void write(int c) {
        int newcount = count + 1;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        buf[count] = (char) c;
        count = newcount;
    }

    /**
     * Writes characters to the buffer.
     *
     * @param c   the data to be written
     * @param off the start offset in the data
     * @param len the number of chars that are written
     */
    public void write(char c[], int off, int len) {
        if ((off < 0) || (off > c.length) || (len < 0) ||
                ((off + len) > c.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newcount = count + len;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(c, off, buf, count, len);
        count = newcount;
    }

    /**
     * Write a portion of a string to the buffer.
     *
     * @param str String to be written from
     * @param off Offset from which to start reading characters
     * @param len Number of characters to be written
     */
    public void write(String str, int off, int len) {
        int newcount = count + len;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        str.getChars(off, off + len, buf, count);
        count = newcount;
    }

    /**
     * Writes the contents of the buffer to another character stream.
     *
     * @param out the output stream to write to
     * @throws java.io.IOException If an I/O error occurs.
     */
    public void writeTo(Writer out) throws IOException {
        out.write(buf, 0, count);
    }

    /**
     * Appends the specified character sequence to this writer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(csq)</tt>
     * behaves in exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.write(csq.toString()) </pre>
     *
     * <p> Depending on the specification of <tt>toString</tt> for the
     * character sequence <tt>csq</tt>, the entire sequence may not be
     * appended. For instance, invoking the <tt>toString</tt> method of a
     * character buffer will return a subsequence whose content depends upon
     * the buffer's position and limit.
     *
     * @param csq The character sequence to append.  If <tt>csq</tt> is
     *            <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *            appended to this writer.
     * @return This writer
     */
    public FastCharArrayWriter append(CharSequence csq) {
        String s = (csq == null ? "null" : csq.toString());
        write(s, 0, s.length());
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this writer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in
     * exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.write(csq.subSequence(start, end).toString()) </pre>
     *
     * @param csq   The character sequence from which a subsequence will be
     *              appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *              will be appended as if <tt>csq</tt> contained the four
     *              characters <tt>"null"</tt>.
     * @param start The index of the first character in the subsequence
     * @param end   The index of the character following the last character in the
     *              subsequence
     * @return This writer
     * @throws IndexOutOfBoundsException If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *                                   is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *                                   <tt>csq.length()</tt>
     */
    public FastCharArrayWriter append(CharSequence csq, int start, int end) {
        String s = (csq == null ? "null" : csq).subSequence(start, end).toString();
        write(s, 0, s.length());
        return this;
    }

    /**
     * Appends the specified character to this writer.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(c)</tt>
     * behaves in exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.write(c) </pre>
     *
     * @param c The 16-bit character to append
     * @return This writer
     */
    public FastCharArrayWriter append(char c) {
        write(c);
        return this;
    }

    /**
     * Resets the buffer so that you can use it again without
     * throwing away the already allocated buffer.
     */
    public void reset() {
        count = 0;
    }

    /**
     * Returns a copy of the input data.
     *
     * @return an array of chars copied from the input data.
     */
    public char toCharArray()[] {
        return Arrays.copyOf(buf, count);
    }

    /**
     * Returns the underlying char array. Note, use {@link #size()} in order to know the size of
     * of the actual content within the array.
     */
    public char[] unsafeCharArray() {
        return buf;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return an int representing the current size of the buffer.
     */
    public int size() {
        return count;
    }

    /**
     * Converts input data to a string.
     *
     * @return the string.
     */
    public String toString() {
        return new String(buf, 0, count);
    }

    /**
     * Converts the input data to a string with trimmed whitespaces.
     */
    public String toStringTrim() {
        int st = 0;
        int len = count;
        char[] val = buf;    /* avoid getfield opcode */

        while ((st < len) && (val[st] <= ' ')) {
            st++;
            len--;
        }
        while ((st < len) && (val[len - 1] <= ' ')) {
            len--;
        }
        return new String(buf, st, len);
    }

    /**
     * Flush the stream.
     */
    public void flush() {
    }

    /**
     * Close the stream.  This method does not release the buffer, since its
     * contents might still be required. Note: Invoking this method in this class
     * will have no effect.
     */
    public void close() {
    }

}
