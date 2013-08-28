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
package org.xbib.io.compress.xz;

import java.io.IOException;
import org.xbib.io.compress.xz.delta.DeltaEncoder;

class DeltaOutputStream extends FinishableOutputStream {
    private static final int TMPBUF_SIZE = 4096;

    private FinishableOutputStream out;
    private final DeltaEncoder delta;
    private final byte[] tmpbuf = new byte[TMPBUF_SIZE];

    private boolean finished = false;
    private IOException exception = null;

    static int getMemoryUsage() {
        return 1 + TMPBUF_SIZE / 1024;
    }

    DeltaOutputStream(FinishableOutputStream out, DeltaOptions options) {
        this.out = out;
        delta = new DeltaEncoder(options.getDistance());
    }

    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte)b;
        write(buf, 0, 1);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length)
            throw new IndexOutOfBoundsException();

        if (exception != null)
            throw exception;

        if (finished)
            throw new XZIOException("Stream finished");

        try {
            while (len > TMPBUF_SIZE) {
                delta.encode(buf, off, TMPBUF_SIZE, tmpbuf);
                out.write(tmpbuf);
                off += TMPBUF_SIZE;
                len -= TMPBUF_SIZE;
            }

            delta.encode(buf, off, len, tmpbuf);
            out.write(tmpbuf, 0, len);
        } catch (IOException e) {
            exception = e;
            throw e;
        }
    }

    public void flush() throws IOException {
        if (exception != null)
            throw exception;

        if (finished)
            throw new XZIOException("Stream finished or closed");

        try {
            out.flush();
        } catch (IOException e) {
            exception = e;
            throw e;
        }
    }

    public void finish() throws IOException {
        if (!finished) {
            if (exception != null)
                throw exception;

            try {
                out.finish();
            } catch (IOException e) {
                exception = e;
                throw e;
            }

            finished = true;
        }
    }

    public void close() throws IOException {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                if (exception == null)
                    exception = e;
            }

            out = null;
        }

        if (exception != null)
            throw exception;
    }
}
