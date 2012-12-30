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
package org.xbib.io.sequential;

import java.io.IOException;

public abstract class FilterCharStream extends SequentialCharStream {

    protected volatile CharStream stream;

    /**
     * Creates a <code>FilterCharStream</code>
     * by assigning the  argument <code>stream</code>
     * to the field <code>this.stream</code> so as
     * to remember it for later use.
     *
     * @param   stream   the underlying data stream, or <code>null</code> if
     *          this instance is to be created without an underlying stream.
     */
    public FilterCharStream(CharStream stream) {
        super(stream != null ? stream.getReader() : null);
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(char c[]) throws IOException {
        return read(c, 0, c.length);
    }

    @Override
    public int read(char c[], int off, int len) throws IOException {
        return stream.read(c, off, len);
    }

    /**
     * Skips characters.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }

    /**
     * Tells whether this stream is ready to be read.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public boolean ready() throws IOException {
        return stream.ready();
    }

    /**
     * Tells whether this stream supports the mark() operation.
     */
    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    /**
     * Marks the present position in the stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void mark(int readAheadLimit) throws IOException {
        stream.mark(readAheadLimit);
    }

    /**
     * Resets the stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
