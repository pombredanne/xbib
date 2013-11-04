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
package org.xbib.io.chunk;

import org.xbib.io.chunk.ChunkedStream;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An in-memory buffer that provides OutputStream and InputStream interfaces.
 *
 * This is more efficient than using ByteArrayOutputStream/ByteArrayInputStream
 *
 * This implementation is thread safe and blocks writers until there is a reader
 * that processes chunks.
 *
 */
public class BlockingChunkedStream extends ChunkedStream {

    private final static Logger logger = LoggerFactory.getLogger(BlockingChunkedStream.class.getName());

    public BlockingChunkedStream() {
        this(DEFAULT_CHUNK_SIZE);
    }

    public BlockingChunkedStream(int chunkSize) {
        this(new LinkedBlockingQueue<StreamChunk>(), chunkSize, "UTF-8");
    }

    public BlockingChunkedStream(BlockingQueue<StreamChunk> queue, int chunkSize, String encoding) {
        super(queue, chunkSize, encoding);
    }

    protected void produce(StreamChunk chunk) throws IOException {
        try {
            ((BlockingQueue<StreamChunk>)chunks).put(chunk);
        } catch (InterruptedException e) {
            throw new IOException("interrupted");
        }
    }

    protected StreamChunk consume() throws IOException {
        try {
           return ((BlockingQueue<StreamChunk>)chunks).take();
        } catch (InterruptedException e) {
            throw new IOException("interrupted");
        }
    }

    public synchronized void closeProduction() throws IOException {
        if (!closed) {
            output.close();
            closed = true;
            if (currentWriteChunk.bytesUnread() > 0) {
                try {
                    ((BlockingQueue<StreamChunk>)chunks).put(currentWriteChunk);
                } catch (InterruptedException e) {
                    throw new IOException("interrupted");
                }
            }
        }
    }

    public synchronized void closeConsumption() throws IOException {
        input.close();
    }

    protected int prepareRead() throws IOException {
        int bytesUnread = currentReadChunk != null ? currentReadChunk.bytesUnread() : 0;
        if (bytesUnread == 0) {
            currentReadChunk = consume();
            bytesUnread = currentReadChunk.bytesUnread();
            totalBytesUnreadInList -= bytesUnread;
        }
        return bytesUnread;
    }

}