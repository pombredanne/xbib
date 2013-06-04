/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xbib.sequential;

import java.io.IOException;
import java.io.Reader;

/**
 * SequentialCharStream is a buffered character input reader. Buffering allows
 * reading from character streams more efficiently. If the default size of the
 * buffer is not practical, another size may be specified. Reading a character
 * from a Reader class usually involves reading a character from its Stream or
 * subsequent Reader. It is advisable to wrap a BufferedReader around those
 * Readers whose read operations may have high latency. For example, the
 * following code
 * <p/>
 * <
 * pre>
 * SequentialCharStream inReader = new SequentialCharStream(new FileReader(&quot;file.java&quot;));
 * </pre>
 * <p/>
 * will buffer input for the file
 * <code>file.java</code>.
 *
 */
public class SequentialCharStream extends Reader implements CharStream {

    private Reader in;
    private char[] buf;
    private int marklimit = -1;
    private int count;
    private int markpos = -1;
    private int pos;
    private CharStreamListener listener;
    private final static int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Constructs a new SequentialCharStream on the Reader
     * <code>in</code>. The default buffer size (8K) is allocated and all reads
     * can now be filtered through this BufferedReader.
     *
     * @param in the Reader to buffer reads on.
     */
    public SequentialCharStream(Reader in) {
        this(in, null);
    }

    /**
     * Constructs a new BufferedReader on the Reader
     * <code>in</code>. The default buffer size (8K) is allocated and all reads
     * can now be filtered through this BufferedReader.
     *
     * @param in the Reader to buffer reads on.
     */
    public SequentialCharStream(Reader in, CharStreamListener listener) {
        this(in, DEFAULT_BUFFER_SIZE, listener);
    }

    /**
     * Constructs a new BufferedReader on the Reader
     * <code>in</code>. The buffer size is specified by the parameter
     * <code>size</code> and all reads can now be filtered through this
     * BufferedReader.
     *
     * @param in the Reader to buffer reads on.
     * @param size the size of buffer to allocate.
     * @throws IllegalArgumentException if the size is <= 0
     */
    public SequentialCharStream(Reader in, int size, CharStreamListener listener) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException();
        }
        this.in = in;
        this.buf = new char[size];
        this.listener = listener;
    }

    /**
     * Close the Reader. This implementation closes the Reader being filtered
     * and releases the buffer used by this reader. If this BufferedReader has
     * already been closed, nothing is done.
     *
     * @throws java.io.IOException If an error occurs attempting to close this
     * BufferedReader.
     */
    @Override
    public void close() throws IOException {
        if (listener != null) {
            listener.markFile();
        }
        synchronized (lock) {
            if (!isClosed()) {
                in.close();
                buf = null;
            }
        }
    }

    private int fillbuf() throws IOException {
        if (markpos == -1 || (pos - markpos >= marklimit)) {
            /*
             * Mark position not set or exceeded readlimit
             */
            int result = in.read(buf, 0, buf.length);
            if (result > 0) {
                markpos = -1;
                pos = 0;
                count = result == -1 ? 0 : result;
            }
            return result;
        }
        if (markpos == 0 && marklimit > buf.length) {
            /*
             * Increase buffer size to accommodate the readlimit
             */
            int newLength = buf.length * 2;
            if (newLength > marklimit) {
                newLength = marklimit;
            }
            char[] newbuf = new char[newLength];
            System.arraycopy(buf, 0, newbuf, 0, buf.length);
            buf = newbuf;
        } else if (markpos > 0) {
            System.arraycopy(buf, markpos, buf, 0, buf.length - markpos);
        }

        /*
         * Set the new position and mark position
         */
        pos -= markpos;
        count = markpos = 0;
        int charsread = in.read(buf, pos, buf.length - pos);
        count = charsread == -1 ? pos : pos + charsread;
        return charsread;
    }

    /**
     * Answer a boolean indicating whether or not this BufferedReader is closed.
     *
     * @return
     * <code>true</code> if this reader is closed,
     * <code>false</code> otherwise
     */
    private boolean isClosed() {
        return buf == null;
    }

    /**
     * Set a Mark position in this BufferedReader. The parameter
     * <code>readLimit</code> indicates how many characters can be read before a
     * mark is invalidated. Sending reset() will reposition the reader back to
     * the marked position provided
     * <code>readLimit</code> has not been surpassed.
     *
     * @param readlimit an int representing how many characters must be read
     * before invalidating the mark.
     * @throws java.io.IOException If an error occurs attempting mark this
     * BufferedReader.
     * @throws IllegalArgumentException If readlimit is < 0
     */
    @Override
    public void mark(int readlimit) throws IOException {
        if (readlimit < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            marklimit = readlimit;
            markpos = pos;
        }
    }

    /**
     * Answers a boolean indicating whether or not this Reader supports mark()
     * and reset(). This implementation answers
     * <code>true</code>.
     *
     * @return
     * <code>true</code> if mark() and reset() are supported,
     * <code>false</code> otherwise
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Reads a single character from this reader and returns the result as an
     * int. The 2 higher-order characters are set to 0. If the end of reader was
     * encountered then return -1. This implementation either returns a
     * character from the buffer or if there are no characters available, fill
     * the buffer then return a character or -1.
     *
     * @return the character read or -1 if end of reader.
     * @throws java.io.IOException If the BufferedReader is already closed or some other
     * IO error occurs.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            /*
             * Are there buffered characters available?
             */
            if (pos < count || fillbuf() != -1) {
                return buf[pos++];
            }
            return -1;
        }
    }

    /**
     * Reads at most
     * <code>length</code> characters from this BufferedReader and stores them
     * at
     * <code>offset</code> in the character array
     * <code>buffer</code>. Returns the number of characters actually read or -1
     * if the end of reader was encountered. If all the buffered characters have
     * been used, a mark has not been set, and the requested number of
     * characters is larger than this Readers buffer size, this implementation
     * bypasses the buffer and simply places the results directly into
     * <code>buffer</code>.
     *
     * @param buffer character array to store the read characters
     * @param offset offset in buf to store the read characters
     * @param length maximum number of characters to read
     * @return number of characters read or -1 if end of reader.
     * @throws java.io.IOException If the BufferedReader is already closed or some other
     * IO error occurs.
     */
    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if (offset < 0 || offset > buffer.length - length || length < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (length == 0) {
                return 0;
            }
            int required;
            if (pos < count) {
                /*
                 * There are bytes available in the buffer.
                 */
                int copylength = count - pos >= length ? length : count - pos;
                System.arraycopy(buf, pos, buffer, offset, copylength);
                pos += copylength;
                if (copylength == length || !in.ready()) {
                    return copylength;
                }
                offset += copylength;
                required = length - copylength;
            } else {
                required = length;
            }

            while (true) {
                int read;
                /*
                 * If we're not marked and the required size is greater than the
                 * buffer, simply read the bytes directly bypassing the buffer.
                 */
                if (markpos == -1 && required >= buf.length) {
                    read = in.read(buffer, offset, required);
                    if (read == -1) {
                        return required == length ? -1 : length - required;
                    }
                } else {
                    if (fillbuf() == -1) {
                        return required == length ? -1 : length - required;
                    }
                    read = count - pos >= required ? required : count - pos;
                    System.arraycopy(buf, pos, buffer, offset, read);
                    pos += read;
                }
                required -= read;
                if (required == 0) {
                    return length;
                }
                if (!in.ready()) {
                    return length - required;
                }
                offset += read;
            }
        }
    }

    /**
     * Answers a
     * <code>String</code> representing the next line of text available in this
     * BufferedReader. A line is represented by 0 or more characters followed by
     * <code>'\n'</code>,
     * <code>'\r'</code>,
     * <code>'\r\n'</code> or end of stream. The
     * <code>String</code> does not include the newline sequence.
     *
     * @return the contents of the line or null if no characters were read
     * before end of stream.
     * @throws java.io.IOException If the BufferedReader is already closed or some other
     * IO error occurs.
     */
    public String readLine() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            /*
             * Are there buffered characters available?
             */
            if ((pos >= count) && (fillbuf() == -1)) {
                return null;
            }
            for (int charPos = pos; charPos < count; charPos++) {
                char ch = buf[charPos];
                if (ch > '\r') {
                    continue;
                }
                if (ch == '\n') {
                    String res = new String(buf, pos, charPos - pos);
                    pos = charPos + 1;
                    return res;
                } else if (ch == '\r') {
                    String res = new String(buf, pos, charPos - pos);
                    pos = charPos + 1;
                    if (((pos < count) || (fillbuf() != -1))
                            && (buf[pos] == '\n')) {
                        pos++;
                    }
                    return res;
                }
            }

            char eol = '\0';
            StringBuilder result = new StringBuilder(80);
            /*
             * Typical Line Length
             */

            result.append(buf, pos, count - pos);
            pos = count;
            while (true) {
                /*
                 * Are there buffered characters available?
                 */
                if (pos >= count) {
                    if (eol == '\n') {
                        return result.toString();
                    }
                    // attempt to fill buffer
                    if (fillbuf() == -1) {
                        // characters or null.
                        return result.length() > 0 || eol != '\0' ? result.toString() : null;
                    }
                }
                for (int charPos = pos; charPos < count; charPos++) {
                    if (eol == '\0') {
                        if ((buf[charPos] == '\n' || buf[charPos] == '\r')) {
                            eol = buf[charPos];
                        }
                    } else if (eol == '\r' && (buf[charPos] == '\n')) {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos + 1;
                        return result.toString();
                    } else if (eol != '\0') {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos;
                        return result.toString();
                    }
                }
                if (eol == '\0') {
                    result.append(buf, pos, count - pos);
                } else {
                    result.append(buf, pos, count - pos - 1);
                }
                pos = count;
            }
        }

    }

    /**
     * Answers a
     * <code>boolean</code> indicating whether or not this Reader is ready to be
     * read without blocking. If the result is
     * <code>true</code>, the next
     * <code>read()</code> will not block. If the result is
     * <code>false</code> this Reader may or may not block when
     * <code>read()</code> is sent.
     *
     * @return
     * <code>true</code> if the receiver will not block when
     * <code>read()</code> is called,
     * <code>false</code> if unknown or blocking will occur.
     * @throws java.io.IOException If the BufferedReader is already closed or some other
     * IO error occurs.
     */
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(); //$NON-NLS-1$
            }
            return ((count - pos) > 0) || in.ready();
        }
    }

    /**
     * Reset this BufferedReader's position to the last
     * <code>mark()</code> location. Invocations of
     * <code>read()/skip()</code> will occur from this new location. If this
     * Reader was not marked, throw IOException.
     *
     * @throws java.io.IOException If a problem occurred, the receiver does not support
     * <code>mark()/reset()</code>, or no mark has been set.
     */
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            if (markpos == -1) {
                throw new IOException();
            }
            pos = markpos;
        }
    }

    /**
     * Skips
     * <code>amount</code> number of characters in this Reader. Subsequent
     * <code>read()</code>'s will not return these characters unless
     * <code>reset()</code> is used. Skipping characters may invalidate a mark
     * if marklimit is surpassed.
     *
     * @param amount the maximum number of characters to skip.
     * @return the number of characters actually skipped.
     * @throws java.io.IOException If the BufferedReader is already closed or some other
     * IO error occurs.
     * @throws IllegalArgumentException If amount is negative
     */
    @Override
    public long skip(long amount) throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException(); //$NON-NLS-1$
            }
            if (amount < 1) {
                return 0;
            }
            if (count - pos >= amount) {
                pos += amount;
                return amount;
            }

            long read = count - pos;
            pos = count;
            while (read < amount) {
                if (fillbuf() == -1) {
                    return read;
                }
                if (count - pos >= amount - read) {
                    pos += amount - read;
                    return amount;
                }
                // Couldn't get all the characters, skip what we read
                read += (count - pos);
                pos = count;
            }
            return amount;
        }
    }

    @Override
    public Reader getReader() {
        return in;
    }

    public void setListener(CharStreamListener listener) {
        this.listener = listener;
    }

    @Override
    public String readData() throws IOException {
        synchronized (lock) {
            if (isClosed()) {
                throw new IOException();
            }
            /*
             * Are there buffered characters available?
             */
            if ((pos >= count) && (fillbuf() == -1)) {
                return null;
            }
            for (int charPos = pos; charPos < count; charPos++) {
                char ch = buf[charPos];
                if (isSep(ch)) {
                    String res = normalize(new String(buf, pos, charPos - pos));
                    if (listener != null) {
                        if (res.length() > 0) {
                            listener.data(res);
                        }
                        sendEvent(ch);
                    }
                    pos = charPos + 1;
                    return res;
                }
            }
            char eod = '\0';
            StringBuilder result = new StringBuilder(80);
            /*
             * Typical Line Length
             */
            result.append(buf, pos, count - pos);
            pos = count;
            while (true) {
                /*
                 * Are there buffered characters available?
                 */
                if (pos >= count) {
                    if (isSep(eod)) {
                        String s = normalize(result);
                        if (listener != null) {
                            listener.data(s);
                        }
                        return s;
                    }
                    // attempt to fill buffer
                    if (fillbuf() == -1) {
                        // characters or null.
                        if (result.length() > 0 || eod != '\0') {
                            String s = normalize(result);
                            if (listener != null) {
                                listener.data(s);
                            }
                            return s;
                        } else {
                            return null;
                        }
                    }
                }
                for (int charPos = pos; charPos < count; charPos++) {
                    if (eod == '\0') {
                        if (isSep(buf[charPos])) {
                            eod = buf[charPos];
                        }
                    } else if (eod != '\0') {
                        if (charPos > pos) {
                            result.append(buf, pos, charPos - pos - 1);
                        }
                        pos = charPos;
                        String s = normalize(result);
                        if (listener != null) {
                            listener.data(s);
                            sendEvent(eod);
                        }
                        return s;
                    }
                }
                if (eod == '\0') {
                    result.append(buf, pos, count - pos);
                } else {
                    result.append(buf, pos, count - pos - 1);
                }
                pos = count;
            }
        }
    }

    protected boolean isSep(char ch) {
        return (ch == Separable.FS || ch == Separable.GS || ch == Separable.RS || ch == Separable.US);
    }

    protected String normalize(CharSequence ch) {
        return ch.toString(); //Normalizer.normalize(ch, Normalizer.Form.NFKD);
    }

    private void sendEvent(char ch) {
        switch (ch) {
            case Separable.FS: {
                listener.markFile();
                break;
            }
            case Separable.GS: {
                listener.markGroup();
                break;
            }
            case Separable.RS: {
                listener.markRecord();
                break;
            }
            case Separable.US: {
                listener.markUnit();
                break;
            }
        }
    }
}