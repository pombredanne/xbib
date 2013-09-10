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
package org.xbib.servlet.filter.compression;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletInputStream;

final class CompressedServletInputStream extends ServletInputStream {

    private final InputStream compressedStream;

    private boolean closed;

    CompressedServletInputStream(InputStream inputStream, EncodedStreamsFactory encodedStreamsFactory) throws IOException {
        this.compressedStream = encodedStreamsFactory.getCompressedStream(inputStream).getCompressedInputStream();
    }

    @Override
    public int read() throws IOException {
        assertOpen();
        return compressedStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        assertOpen();
        return compressedStream.read(b);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        assertOpen();
        return compressedStream.read(b, offset, length);
    }

    @Override
    public long skip(long n) throws IOException {
        assertOpen();
        return compressedStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        assertOpen();
        return compressedStream.available();
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            compressedStream.close();
            closed = true;
        }
    }

    @Override
    public synchronized void mark(int limit) {
        assertOpen();
        compressedStream.mark(limit);
    }

    @Override
    public synchronized void reset() throws IOException {
        assertOpen();
        compressedStream.reset();
    }

    @Override
    public boolean markSupported() {
        assertOpen();
        return compressedStream.markSupported();
    }

    private void assertOpen() {
        if (closed) {
            throw new IllegalStateException("Stream has been already closed.");
        }
    }

}
