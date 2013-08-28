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

import java.io.InputStream;
import org.xbib.io.compress.xz.simple.ARM;
import org.xbib.io.compress.xz.simple.ARMThumb;
import org.xbib.io.compress.xz.simple.IA64;
import org.xbib.io.compress.xz.simple.PowerPC;
import org.xbib.io.compress.xz.simple.SPARC;
import org.xbib.io.compress.xz.simple.SimpleFilter;
import org.xbib.io.compress.xz.simple.X86;

class BCJDecoder extends BCJCoder implements FilterDecoder {
    private final long filterID;
    private final int startOffset;

    BCJDecoder(long filterID, byte[] props)
            throws UnsupportedOptionsException {
        assert isBCJFilterID(filterID);
        this.filterID = filterID;

        if (props.length == 0) {
            startOffset = 0;
        } else if (props.length == 4) {
            int n = 0;
            for (int i = 0; i < 4; ++i)
                n |= (props[i] & 0xFF) << (i * 8);

            startOffset = n;
        } else {
            throw new UnsupportedOptionsException(
                    "Unsupported BCJ filter properties");
        }
    }

    public int getMemoryUsage() {
        return SimpleInputStream.getMemoryUsage();
    }

    public InputStream getInputStream(InputStream in) {
        SimpleFilter simpleFilter = null;

        if (filterID == X86_FILTER_ID)
            simpleFilter = new X86(false, startOffset);
        else if (filterID == POWERPC_FILTER_ID)
            simpleFilter = new PowerPC(false, startOffset);
        else if (filterID == IA64_FILTER_ID)
            simpleFilter = new IA64(false, startOffset);
        else if (filterID == ARM_FILTER_ID)
            simpleFilter = new ARM(false, startOffset);
        else if (filterID == ARMTHUMB_FILTER_ID)
            simpleFilter = new ARMThumb(false, startOffset);
        else if (filterID == SPARC_FILTER_ID)
            simpleFilter = new SPARC(false, startOffset);
        else
            assert false;

        return new SimpleInputStream(in, simpleFilter);
    }
}
