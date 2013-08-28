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
package org.xbib.io.compress.gzip;

import java.lang.ref.SoftReference;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * GZIP-codec-specific "extension" to {@link org.xbib.io.compress.BufferRecycler},
 * used for recycling expensive objects.
 */
public final class GZIPRecycler {

    protected final static ThreadLocal<SoftReference<GZIPRecycler>> _recyclerRef = new ThreadLocal();
    protected Inflater _inflater;
    protected Deflater _deflater;

    /**
     * Accessor to get thread-local recycler instance
     */
    public static GZIPRecycler instance() {
        SoftReference<GZIPRecycler> ref = _recyclerRef.get();
        GZIPRecycler br = (ref == null) ? null : ref.get();
        if (br == null) {
            br = new GZIPRecycler();
            _recyclerRef.set(new SoftReference(br));
        }
        return br;
    }

    /*
     * API
     */
    public Deflater allocDeflater() {
        Deflater d = _deflater;
        if (d == null) { // important: true means 'dont add zlib header'; gzip has its own
            d = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        } else {
            _deflater = null;
        }
        return d;
    }

    public void releaseDeflater(Deflater d) {
        if (d != null) {
            d.reset();
            _deflater = d;
        }
    }

    public Inflater allocInflater() {
        Inflater i = _inflater;
        if (i == null) { // important: true means 'dont add zlib header'; gzip has its own
            i = new Inflater(true);
        } else {
            _inflater = null;
        }
        return i;
    }

    public void releaseInflater(Inflater i) {
        if (i != null) {
            i.reset();
            _inflater = i;
        }
    }
}
