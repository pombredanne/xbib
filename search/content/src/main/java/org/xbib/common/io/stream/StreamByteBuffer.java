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
package org.xbib.common.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * An in-memory buffer that provides OutputStream and InputStream interfaces.
 *
 * This is more efficient than using ByteArrayOutputStream/ByteArrayInputStream
 *
 * This is not thread-safe, it is intended to be used by a single Thread.
 *
 */
public class StreamByteBuffer {

    private static final int DEFAULT_CHUNK_SIZE = 8192;

    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    private LinkedList<StreamByteBufferChunk> chunks = new LinkedList();

    private StreamByteBufferChunk currentWriteChunk;

    private StreamByteBufferChunk currentReadChunk = null;

    private int chunkSize;

    private StreamByteBufferOutputStream output;

    private StreamByteBufferInputStream input;

    private int totalBytesUnreadInList = 0;

    private int totalBytesUnreadInIterator = 0;

    private ReadMode readMode;

    private Iterator<StreamByteBufferChunk> readIterator;

    public enum ReadMode {

        REMOVE_AFTER_READING,
        RETAIN_AFTER_READING
    }

    public StreamByteBuffer() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public StreamByteBuffer(int chunkSize) {
        this(chunkSize, ReadMode.REMOVE_AFTER_READING);
    }

    public StreamByteBuffer(int chunkSize, ReadMode readMode) {
        this.chunkSize = chunkSize;
        this.readMode = readMode;
        currentWriteChunk = new StreamByteBufferChunk(chunkSize);
        output = new StreamByteBufferOutputStream();
        input = new StreamByteBufferInputStream();
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public InputStream getInputStream() {
        return input;
    }

    public void writeTo(OutputStream target) throws IOException {
        while (prepareRead() != -1) {
            currentReadChunk.writeTo(target);
        }
    }

    public byte[] readAsByteArray() {
        byte[] buf = new byte[totalBytesUnread()];
        input.readImpl(buf, 0, buf.length);
        return buf;
    }

    public String readAsString(String encoding) throws CharacterCodingException {
        Charset charset = Charset.forName(encoding);
        return readAsString(charset);
    }

    public String readAsString(Charset charset) throws CharacterCodingException {
        int unreadSize = totalBytesUnread();
        if (unreadSize > 0) {
            CharsetDecoder decoder = charset.newDecoder().onMalformedInput(
                    CodingErrorAction.REPLACE).onUnmappableCharacter(
                    CodingErrorAction.REPLACE);
            CharBuffer charbuffer = CharBuffer.allocate(unreadSize);
            ByteBuffer buf = null;
            while (prepareRead() != -1) {
                buf = currentReadChunk.readToNioBuffer();
                boolean endOfInput = (prepareRead() == -1);
                CoderResult result = decoder.decode(buf, charbuffer, endOfInput);
                if (endOfInput) {
                    if (!result.isUnderflow()) {
                        result.throwException();
                    }
                }
            }
            CoderResult result = decoder.flush(charbuffer);
            if (buf.hasRemaining()) {
                throw new IllegalStateException("There's a bug here, buffer wasn't read fully.");
            }
            if (!result.isUnderflow()) {
                result.throwException();
            }
            charbuffer.flip();
            String str;
            if (charbuffer.hasArray()) {
                int len = charbuffer.remaining();
                char[] ch = charbuffer.array();
                if (len != ch.length) {
                    ch = subarray(ch, 0, len);
                }
                str = new String(ch);
            } else {
                str = charbuffer.toString();
            }
            return str;
        }
        return null;
    }

    public static char[] subarray(char[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }
        if (endIndexExclusive > array.length) {
            endIndexExclusive = array.length;
        }
        int newSize = endIndexExclusive - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_CHAR_ARRAY;
        }

        char[] subarray = new char[newSize];
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }

    public int totalBytesUnread() {
        int total = 0;
        if (readMode == ReadMode.REMOVE_AFTER_READING) {
            total = totalBytesUnreadInList;
        } else if (readMode == ReadMode.RETAIN_AFTER_READING) {
            prepareRetainAfterReading();
            total = totalBytesUnreadInIterator;
        }
        if (currentReadChunk != null) {
            total += currentReadChunk.bytesUnread();
        }
        if (currentWriteChunk != currentReadChunk && currentWriteChunk != null) {
            if (readMode == ReadMode.REMOVE_AFTER_READING) {
                total += currentWriteChunk.bytesUnread();
            } else if (readMode == ReadMode.RETAIN_AFTER_READING) {
                total += currentWriteChunk.bytesUsed();
            }
        }
        return total;
    }

    protected int allocateSpace() {
        int spaceLeft = currentWriteChunk.spaceLeft();
        if (spaceLeft == 0) {
            chunks.add(currentWriteChunk);
            totalBytesUnreadInList += currentWriteChunk.bytesUnread();
            currentWriteChunk = new StreamByteBufferChunk(chunkSize);
            spaceLeft = currentWriteChunk.spaceLeft();
        }
        return spaceLeft;
    }

    protected int prepareRead() {
        prepareRetainAfterReading();
        int bytesUnread = (currentReadChunk != null) ? currentReadChunk.bytesUnread() : 0;
        if (bytesUnread == 0) {
            if (readMode == ReadMode.REMOVE_AFTER_READING && !chunks.isEmpty()) {
                currentReadChunk = chunks.removeFirst();
                bytesUnread = currentReadChunk.bytesUnread();
                totalBytesUnreadInList -= bytesUnread;
            } else if (readMode == ReadMode.RETAIN_AFTER_READING && readIterator.hasNext()) {
                currentReadChunk = readIterator.next();
                currentReadChunk.reset();
                bytesUnread = currentReadChunk.bytesUnread();
                totalBytesUnreadInIterator -= bytesUnread;
            } else if (currentReadChunk != currentWriteChunk) {
                currentReadChunk = currentWriteChunk;
                bytesUnread = currentReadChunk.bytesUnread();
            } else {
                bytesUnread = -1;
            }
        }
        return bytesUnread;
    }

    public void reset() {
        if (readMode == ReadMode.RETAIN_AFTER_READING) {
            readIterator = null;
            prepareRetainAfterReading();
            if (currentWriteChunk != null) {
                currentWriteChunk.reset();
            }
        }
    }

    private void prepareRetainAfterReading() {
        if (readMode == ReadMode.RETAIN_AFTER_READING && readIterator == null) {
            readIterator = chunks.iterator();
            totalBytesUnreadInIterator = totalBytesUnreadInList;
            currentReadChunk = null;
        }
    }

    public ReadMode getReadMode() {
        return readMode;
    }

    public void setReadMode(ReadMode readMode) {
        this.readMode = readMode;
    }

    public void retainAfterReadingMode() {
        setReadMode(ReadMode.RETAIN_AFTER_READING);
    }

    class StreamByteBufferChunk {

        private int pointer = 0;
        private byte[] buffer;
        private int size;
        private int used = 0;

        public StreamByteBufferChunk(int size) {
            this.size = size;
            buffer = new byte[size];
        }

        public ByteBuffer readToNioBuffer() {
            if (pointer < used) {
                ByteBuffer result;
                if (pointer > 0 || used < size) {
                    result = ByteBuffer.wrap(buffer, pointer, used - pointer);
                } else {
                    result = ByteBuffer.wrap(buffer);
                }
                pointer = used;
                return result;
            }

            return null;
        }

        public boolean write(byte b) {
            if (used < size) {
                buffer[used++] = b;
                return true;
            }

            return false;
        }

        public void write(byte[] b, int off, int len) {
            System.arraycopy(b, off, buffer, used, len);
            used = used + len;
        }

        public void read(byte[] b, int off, int len) {
            System.arraycopy(buffer, pointer, b, off, len);
            pointer = pointer + len;
        }

        public void writeTo(OutputStream target) throws IOException {
            if (pointer < used) {
                target.write(buffer, pointer, used - pointer);
                pointer = used;
            }
        }

        public void reset() {
            pointer = 0;
        }

        public int bytesUsed() {
            return used;
        }

        public int bytesUnread() {
            return used - pointer;
        }

        public int read() {
            if (pointer < used) {
                return buffer[pointer++] & 0xff;
            }

            return -1;
        }

        public int spaceLeft() {
            return size - used;
        }
    }

    class StreamByteBufferOutputStream extends OutputStream {

        private boolean closed = false;

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }

            if ((off < 0) || (off > b.length) || (len < 0)
                    || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }

            if (len == 0) {
                return;
            }

            int bytesLeft = len;
            int currentOffset = off;
            while (bytesLeft > 0) {
                int spaceLeft = allocateSpace();
                int writeBytes = Math.min(spaceLeft, bytesLeft);
                currentWriteChunk.write(b, currentOffset, writeBytes);
                bytesLeft -= writeBytes;
                currentOffset += writeBytes;
            }
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public void write(int b) throws IOException {
            allocateSpace();
            currentWriteChunk.write((byte) b);
        }

        public StreamByteBuffer getBuffer() {
            return StreamByteBuffer.this;
        }
    }

    class StreamByteBufferInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            prepareRead();
            return currentReadChunk.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return readImpl(b, off, len);
        }

        int readImpl(byte[] b, int off, int len) {
            if (b == null) {
                throw new NullPointerException();
            }

            if ((off < 0) || (off > b.length) || (len < 0)
                    || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }

            if (len == 0) {
                return 0;
            }

            int bytesLeft = len;
            int currentOffset = off;
            int bytesUnread = prepareRead();
            int totalBytesRead = 0;
            while (bytesLeft > 0 && bytesUnread != -1) {
                int readBytes = Math.min(bytesUnread, bytesLeft);
                currentReadChunk.read(b, currentOffset, readBytes);
                bytesLeft -= readBytes;
                currentOffset += readBytes;
                totalBytesRead += readBytes;
                bytesUnread = prepareRead();
            }
            if (totalBytesRead > 0) {
                return totalBytesRead;
            }

            return -1;
        }

        @Override
        public synchronized void reset() throws IOException {
            if (readMode == ReadMode.RETAIN_AFTER_READING) {
                StreamByteBuffer.this.reset();
            } else {
                // reset isn't supported in ReadMode.REMOVE_AFTER_READING
                super.reset();
            }
        }

        @Override
        public int available() throws IOException {
            return totalBytesUnread();
        }

        public StreamByteBuffer getBuffer() {
            return StreamByteBuffer.this;
        }
    }
}