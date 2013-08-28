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
package org.xbib.io.compress.lzf;

import java.io.IOException;

/**
 * Encoder that handles splitting of input into chunks to encode, calls
 * {@link ChunkEncoder} to compress individual chunks and combines resulting
 * chunks into contiguous output byte array.
 */
public class LZFEncoder {
    // Static methods only, no point in instantiating

    private LZFEncoder() {
    }

    public static byte[] encode(byte[] data) throws IOException {
        return encode(data, data.length);
    }

    /**
     * Method for compressing given input data using LZF encoding and block
     * structure (compatible with lzf command line utility). Result consists of
     * a sequence of chunks.
     */
    public static byte[] encode(byte[] data, int length) throws IOException {
        return encode(data, 0, length);
    }

    /**
     * Method for compressing given input data using LZF encoding and block
     * structure (compatible with lzf command line utility). Result consists of
     * a sequence of chunks.
     */
    public static byte[] encode(byte[] data, int offset, int length) throws IOException {
        ChunkEncoder enc = new ChunkEncoder(length);
        byte[] result = encode(enc, data, offset, length);
        // important: may be able to reuse buffers
        enc.close();
        return result;
    }

    public static byte[] encode(ChunkEncoder enc, byte[] data, int length)
            throws IOException {
        return encode(enc, data, 0, length);
    }

    /**
     * @since 0.8.1
     */
    public static byte[] encode(ChunkEncoder enc, byte[] data, int offset, int length)
            throws IOException {
        int left = length;
        int chunkLen = Math.min(LZFChunk.MAX_CHUNK_LEN, left);
        LZFChunk first = enc.encodeChunk(data, offset, chunkLen);
        left -= chunkLen;
        // shortcut: if it all fit in, no need to coalesce:
        if (left < 1) {
            return first.getData();
        }
        // otherwise need to get other chunks:
        int resultBytes = first.length();
        offset += chunkLen;
        LZFChunk last = first;

        do {
            chunkLen = Math.min(left, LZFChunk.MAX_CHUNK_LEN);
            LZFChunk chunk = enc.encodeChunk(data, offset, chunkLen);
            offset += chunkLen;
            left -= chunkLen;
            resultBytes += chunk.length();
            last.setNext(chunk);
            last = chunk;
        } while (left > 0);
        // and then coalesce returns into single contiguous byte array
        byte[] result = new byte[resultBytes];
        int ptr = 0;
        for (; first != null; first = first.next()) {
            ptr = first.copyTo(result, ptr);
        }
        return result;
    }
}
