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
import org.xbib.io.compress.lzf.util.ChunkDecoderFactory;

/**
 * Decoder that handles decoding of sequence of encoded LZF chunks, combining
 * them into a single contiguous result byte array. As of version 0.9, this
 * class has been mostly replaced by {@link ChunkDecoder}, although static
 * methods are left here and may still be used for convenience. All static
 * methods use {@link ChunkDecoderFactory#optimalInstance} to find actual
 * {@link ChunkDecoder} instance to use.
 *
 */
public class LZFDecoder {

    public static byte[] decode(final byte[] inputBuffer) throws IOException {
        return decode(inputBuffer, 0, inputBuffer.length);
    }

    public static byte[] decode(final byte[] inputBuffer, int offset, int length) throws IOException {
        return ChunkDecoderFactory.optimalInstance().decode(inputBuffer, offset, length);
    }

    public static int decode(final byte[] inputBuffer, final byte[] targetBuffer) throws IOException {
        return decode(inputBuffer, 0, inputBuffer.length, targetBuffer);
    }

    public static int decode(final byte[] sourceBuffer, int offset, int length, final byte[] targetBuffer) throws IOException {
        return ChunkDecoderFactory.optimalInstance().decode(sourceBuffer, offset, length, targetBuffer);
    }

    public static int calculateUncompressedSize(byte[] data, int offset, int length) throws IOException {
        return ChunkDecoder.calculateUncompressedSize(data, length, length);
    }
}
