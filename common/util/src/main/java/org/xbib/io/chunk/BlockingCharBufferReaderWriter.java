package org.xbib.io.chunk;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingCharBufferReaderWriter extends CharBufferReaderWriter {

    public BlockingCharBufferReaderWriter() {
        this("UTF-8");
    }

    public BlockingCharBufferReaderWriter(String encoding) {
        this(new LinkedBlockingQueue<CharBuffer>(), encoding);
    }

    public BlockingCharBufferReaderWriter(BlockingQueue<CharBuffer> queue, String encoding) {
        super(queue, encoding);
    }

    protected void put(CharBuffer buffer) throws IOException {
        try {
            ((BlockingQueue<CharBuffer>)queue).put(buffer);
        } catch (InterruptedException e) {
            throw new IOException("interrupted");
        }
    }

    protected CharBuffer get() throws IOException  {
        try {
            return ((BlockingQueue<CharBuffer>)queue).take();
        } catch (InterruptedException e) {
            throw new IOException("interrupted");
        }
    }

}