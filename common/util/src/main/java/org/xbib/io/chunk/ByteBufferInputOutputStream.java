package org.xbib.io.chunk;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.Queue;

public class ByteBufferInputOutputStream extends InputStream implements Appendable, Closeable {

    protected final Queue<ByteBuffer> queue;

    protected final CharsetEncoder encoder;

    protected ByteBuffer byteBuffer;

    protected OutputStream out;

    public ByteBufferInputOutputStream() {
        this("UTF-8");
    }

    public ByteBufferInputOutputStream(String encoding) {
        this(new LinkedList<ByteBuffer>(), encoding);
    }

    public ByteBufferInputOutputStream(Queue<ByteBuffer> queue, String encoding) {
        super();
        this.queue = queue;
        this.encoder = Charset.forName(encoding).newEncoder();
    }

    public void add(char[] chars, int off, int len) throws IOException {
        add(CharBuffer.wrap(chars, off, len));
    }

    public void add(char[] chars) throws IOException {
        add(CharBuffer.wrap(chars));
    }

    public void add(CharSequence chars) throws IOException {
        add(CharBuffer.wrap(chars));
    }

    public void add(CharSequence chars, int off, int len) throws IOException {
        add(CharBuffer.wrap(chars, off, len));
    }

    public void add(CharBuffer source) throws IOException {
        ByteBuffer buffer = encoder.encode(source);
        if (byteBuffer == null) {
            byteBuffer = buffer;
        } else {
            put(buffer);
        }
    }

    public void add(byte[] bytes, int off, int len) throws IOException {
        add(ByteBuffer.wrap(bytes, off, len));
    }

    public void add(byte[] bytes) throws IOException {
        add(ByteBuffer.wrap(bytes));
    }

    public void add(ByteBuffer buffer) throws IOException {
        if (byteBuffer == null) {
            byteBuffer = buffer;
        } else {
            put(buffer);
        }
    }
    public int read() throws IOException {
        if (byteBuffer == null) {
            return -1;
        }
        try {
            return byteBuffer.get();
        } catch (BufferUnderflowException ex) {
            byteBuffer = get();
            return read();
        }
    }

    @Override
    public synchronized int read(byte[] buf, int off, int len) throws IOException {
        if (byteBuffer == null) {
            return -1;
        }
        int nextlen = 0;
        int remaining = byteBuffer.remaining();
        if (len > remaining) {
            nextlen = len - remaining;
            len = remaining;
        }
        byteBuffer.get(buf, off, len);
        if (nextlen > 0) {
            byteBuffer = get();
            return read(buf, off + len, nextlen);
        } else {
            return len;
        }
    }

    @Override
    public void close() throws IOException {
        byteBuffer = null;
        queue.clear();
    }

    protected void put(ByteBuffer buffer) throws IOException {
        queue.add(buffer);
    }

    protected ByteBuffer get() throws IOException {
        return queue.poll();
    }

    public OutputStream getOutputStream() {
        if (out == null) {
            out = new ByteBufferStream();
        }
        return out;
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

    class ByteBufferStream extends OutputStream {

        public ByteBufferStream() {
            super();
        }

        @Override
        public void write(int ch) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            add(buf, off, len);
        }

    }
}