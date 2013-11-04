package org.xbib.io.chunk;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.Queue;

public class CharBufferReaderWriter extends Reader implements Appendable, Closeable {

    protected final Queue<CharBuffer> queue;

    protected final CharsetEncoder encoder;

    protected CharBuffer charBuffer;

    protected Writer writer;

    public CharBufferReaderWriter() {
        this("UTF-8");
    }

    public CharBufferReaderWriter(String encoding) {
        this(new LinkedList<CharBuffer>(), encoding);
    }

    public CharBufferReaderWriter(Queue<CharBuffer> queue, String encoding) {
        super();
        this.queue = queue;
        this.encoder = Charset.forName(encoding).newEncoder();
    }

    public void add(CharSequence chars) throws IOException {
        add(CharBuffer.wrap(chars));
    }

    public void add(CharSequence chars, int off, int len) throws IOException {
        add(CharBuffer.wrap(chars.subSequence(off, len + off)));
    }

    public void add(char[] chars, int off, int len) throws IOException {
        add(CharBuffer.wrap(chars, off, len));
    }

    public void add(char[] chars) throws IOException {
        add(CharBuffer.wrap(chars));
    }

    public void add(CharBuffer source) throws IOException {
        CharBuffer buffer = encoder.encode(source).asCharBuffer();
        if (charBuffer == null) {
            charBuffer = buffer;
        } else {
            put(buffer);
        }
        synchronized (this) {
            notify();
        }
    }

    public void waitFor() throws InterruptedException {
        synchronized (this) {
            wait();
        }
    }

    public int read() throws IOException {
        if (charBuffer == null) {
            return -1;
        }
        try {
            return charBuffer.get();
        } catch (BufferUnderflowException ex) {
            charBuffer = get();
            return read();
        }
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (charBuffer == null) {
            return -1;
        }
        int nextlen = 0;
        int remaining = charBuffer.remaining();
        if (len > remaining) {
            nextlen = len - remaining;
            len = remaining;
        }
        // move charBuffer into cbuf
        charBuffer.get(cbuf, off, len);
        if (nextlen > 0) {
            charBuffer = get();
            return read(cbuf, off + len, nextlen);
        } else {
            return len;
        }
    }

    @Override
    public int read(CharBuffer buffer) throws IOException {
        return read(buffer, 0, buffer.length());
    }

    public int read(CharBuffer buffer, int off, int len) throws IOException {
        if (charBuffer == null) {
            return -1;
        }
        int nextlen = 0;
        int remaining = buffer.remaining();
        if (len > remaining) {
            nextlen = len - remaining;
            len = remaining;
        }
        buffer.append(charBuffer, off, off + len - 1);
        if (nextlen > 0) {
            charBuffer = get();
            return read(buffer, off + len, nextlen);
        } else {
            return len;
        }
    }

    @Override
    public void close() throws IOException {
        charBuffer = null;
        queue.clear();
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        add(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        add(csq, start, end - start);
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void put(CharBuffer buffer) throws IOException {
        queue.add(buffer);
    }

    protected CharBuffer get() throws IOException {
        return queue.poll();
    }

    public Writer getWriter() {
        if (writer == null) {
            writer = new CharBufferWriter();
        }
        return writer;
    }

    class CharBufferWriter extends Writer {

        public CharBufferWriter() {
            super();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            add(cbuf, off, len);
        }

        public void write(CharSequence str) throws IOException {
            add(str);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            add(str, off, len);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

    }
}