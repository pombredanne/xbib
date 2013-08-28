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

import java.lang.ref.SoftReference;

/**
 * Simple helper class to encapsulate details of basic buffer
 * recycling scheme, which helps a lot (as per profiling) for
 * smaller encoding cases.
 * 
 */
public final class BufferRecycler
{
    private final static int MIN_ENCODING_BUFFER = 4000;

    private final static int MIN_OUTPUT_BUFFER = 8000;
    
    /**
     * This <code>ThreadLocal</code> contains a {@link java.lang.ref.SoftReference}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling for buffers we need for encoding, decoding.
     */
     final protected static ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef
         = new ThreadLocal<SoftReference<BufferRecycler>>();
   

    private byte[] _inputBuffer;
    private byte[] _outputBuffer;

    private byte[] _decodingBuffer;
    private byte[] _encodingBuffer;

    private int[] _encodingHash;
    
    /**
     * Accessor to get thread-local recycler instance
     */
    public static BufferRecycler instance()
    {
        SoftReference<BufferRecycler> ref = _recyclerRef.get();
        BufferRecycler br = (ref == null) ? null : ref.get();
        if (br == null) {
            br = new BufferRecycler();
            _recyclerRef.set(new SoftReference<BufferRecycler>(br));
        }
        return br;
    }

    /*
    ///////////////////////////////////////////////////////////////////////
    // Buffers for encoding (output)
    ///////////////////////////////////////////////////////////////////////
     */
    
    public byte[] allocEncodingBuffer(int minSize)
    {
        byte[] buf = _encodingBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_ENCODING_BUFFER)];
        } else {
            _encodingBuffer = null;
        }
        return buf;
    }

    public void releaseEncodeBuffer(byte[] buffer)
    {
        if (_encodingBuffer == null || buffer.length > _encodingBuffer.length) {
            _encodingBuffer = buffer;
        }
    }
    
    public byte[] allocOutputBuffer(int minSize)
    {
        byte[] buf = _outputBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_OUTPUT_BUFFER)];
        } else {
            _outputBuffer = null;
        }
        return buf;
    }

    public void releaseOutputBuffer(byte[] buffer)
    {
        if (_outputBuffer == null || (buffer != null && buffer.length > _outputBuffer.length)) {
            _outputBuffer = buffer;
        }
    }

    public int[] allocEncodingHash(int suggestedSize)
    {
        int[] buf = _encodingHash;
        if (buf == null || buf.length < suggestedSize) {
            buf = new int[suggestedSize];
        } else {
            _encodingHash = null;
        }
        return buf;
    }

    public void releaseEncodingHash(int[] buffer)
    {
        if (_encodingHash == null || (buffer != null && buffer.length > _encodingHash.length)) {
            _encodingHash = buffer;
        }
    }

    public byte[] allocInputBuffer(int minSize)
    {
        byte[] buf = _inputBuffer;
        if (buf == null || buf.length < minSize) {
            buf = new byte[Math.max(minSize, MIN_OUTPUT_BUFFER)];
        } else {
            _inputBuffer = null;
        }
        return buf;
    }

    public void releaseInputBuffer(byte[] buffer)
    {
        if (_inputBuffer == null || (buffer != null && buffer.length > _inputBuffer.length)) {
            _inputBuffer = buffer;
        }
    }
    
    public byte[] allocDecodeBuffer(int size)
    {
        byte[] buf = _decodingBuffer;
        if (buf == null || buf.length < size) {
            buf = new byte[size];
        } else {
            _decodingBuffer = null;
        }
        return buf;
    }

    public void releaseDecodeBuffer(byte[] buffer)
    {
        if (_decodingBuffer == null || (buffer != null && buffer.length > _decodingBuffer.length)) {
            _decodingBuffer = buffer;
        }
    }
    
}
