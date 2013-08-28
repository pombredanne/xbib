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
package org.xbib.io.compress;

import java.io.IOException;

/**
 * Abstract class that defines "push" style API for various uncompressors
 * (aka decompressors or decoders). Implements are alternatives to stream
 * based uncompressors (such as {@link org.xbib.io.compress.lzf.LZFInputStream})
 * in cases where "push" operation is important and/or blocking is not allowed;
 * for example, when handling asynchronous HTTP responses.
 * Note that API does not define the way that listener is attached: this is
 * typically passed through to constructor of the implementation.
 * 
 */
public abstract class Uncompressor
{
    /**
     * Method called to feed more compressed data to be uncompressed, and
     * sent to possible listeners.
     */
    public abstract void feedCompressedData(byte[] comp, int offset, int len)
        throws IOException;

    /**
     * Method called to indicate that all data to uncompress has already been fed.
     * This typically results in last block of data being uncompressed, and results
     * being sent to listener(s); but may also throw an exception if incomplete
     * block was passed.
     */
    public abstract void complete() throws IOException;
}
