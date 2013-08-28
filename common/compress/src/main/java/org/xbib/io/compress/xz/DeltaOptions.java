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

/**
 * Delta filter options. The Delta filter can be used only as a non-last
 * filter in the chain, for example Delta + LZMA2.
 * <p>
 * Currently only simple byte-wise delta is supported. The only option
 * is the delta distance, which you should set to match your data.
 * It's not possible to provide a generic default value for it.
 * <p>
 * For example, with distance = 2 and eight-byte input
 * A1 B1 A2 B3 A3 B5 A4 B7, the output will be A1 B1 01 02 01 02 01 02.
 * <p>
 * The Delta filter can be good with uncompressed bitmap images. It can
 * also help with PCM audio, although special-purpose compressors like
 * FLAC will give much smaller result at much better compression speed.
 */
public class DeltaOptions extends FilterOptions {
    /**
     * Smallest supported delta calculation distance.
     */
    public static final int DISTANCE_MIN = 1;

    /**
     * Largest supported delta calculation distance.
     */
    public static final int DISTANCE_MAX = 256;

    private int distance = DISTANCE_MIN;

    /**
     * Creates new Delta options and sets the delta distance to 1 byte.
     */
    public DeltaOptions() {}

    /**
     * Creates new Delta options and sets the distance to the given value.
     */
    public DeltaOptions(int distance) throws UnsupportedOptionsException {
        setDistance(distance);
    }

    /**
     * Sets the delta distance in bytes. The new distance must be in
     * the range [DISTANCE_MIN, DISTANCE_MAX].
     */
    public void setDistance(int distance) throws UnsupportedOptionsException {
        if (distance < DISTANCE_MIN || distance > DISTANCE_MAX)
            throw new UnsupportedOptionsException(
                    "Delta distance must be in the range [" + DISTANCE_MIN
                    + ", " + DISTANCE_MAX + "]: " + distance);

        this.distance = distance;
    }

    /**
     * Gets the delta distance.
     */
    public int getDistance() {
        return distance;
    }

    public int getEncoderMemoryUsage() {
        return DeltaOutputStream.getMemoryUsage();
    }

    public FinishableOutputStream getOutputStream(FinishableOutputStream out) {
        return new DeltaOutputStream(out, this);
    }

    public int getDecoderMemoryUsage() {
        return 1;
    }

    public InputStream getInputStream(InputStream in) {
        return new DeltaInputStream(in, distance);
    }

    FilterEncoder getFilterEncoder() {
        return new DeltaEncoder(this);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            assert false;
            throw new RuntimeException();
        }
    }
}
