/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.io;


import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class BytesArray implements BytesReference {

    public static final byte[] EMPTY_ARRAY = new byte[0];
    public static final BytesArray EMPTY = new BytesArray(EMPTY_ARRAY, 0, 0);

    protected byte[] bytes;
    protected int offset;
    protected int length;

    public BytesArray(String bytes) {
        this(toBytes(bytes));
    }
    
    private static byte[] toBytes(String bytes) {
        try {
        return bytes.getBytes("UTF-8");
        } catch(UnsupportedEncodingException e) {
            return null;
        }
    }

    public BytesArray(byte[] bytes) {
        this.bytes = bytes;
        this.offset = 0;
        this.length = bytes.length;
    }

    public BytesArray(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    
    public byte get(int index) {
        return bytes[offset + index];
    }

    
    public int length() {
        return length;
    }

    
    public BytesReference slice(int from, int length) {
        if (from < 0 || (from + length) > this.length) {
            throw new IllegalArgumentException("can't slice a buffer with length [" + this.length + "], with slice parameters from [" + from + "], length [" + length + "]");
        }
        return new BytesArray(bytes, offset + from, length);
    }

    
    public StreamInput streamInput() {
        return new BytesStreamInput(bytes, offset, length, false);
    }

    
    public void writeTo(OutputStream os) throws IOException {
        os.write(bytes, offset, length);
    }

    
    public byte[] toBytes() {
        if (offset == 0 && bytes.length == length) {
            return bytes;
        }
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    
    public BytesArray toBytesArray() {
        return this;
    }

    
    public BytesArray copyBytesArray() {
        return new BytesArray(Arrays.copyOfRange(bytes, offset, offset + length));
    }

    
    public boolean hasArray() {
        return true;
    }

    
    public byte[] array() {
        return bytes;
    }

    
    public int arrayOffset() {
        return offset;
    }

    
    public String toUtf8() {
        if (length == 0) {
            return "";
        }
        try {
            return new String(bytes, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    
    public boolean equals(Object obj) {
        return bytesEquals((BytesArray) obj);
    }

    public boolean bytesEquals(BytesArray other) {
        if (length == other.length) {
            int otherUpto = other.offset;
            final byte[] otherBytes = other.bytes;
            final int end = offset + length;
            for (int upto = offset; upto < end; upto++, otherUpto++) {
                if (bytes[upto] != otherBytes[otherUpto]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    
    public int hashCode() {
        int result = 0;
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            result = 31 * result + bytes[i];
        }
        return result;
    }


    /**
     * Returns an array size >= minTargetSize, generally
     * over-allocating exponentially to achieve amortized
     * linear-time cost as the array grows.
     * <p/>
     * NOTE: this was originally borrowed from Python 2.4.2
     * listobject.c sources (attribution in LICENSE.txt), but
     * has now been substantially changed based on
     * discussions from java-dev thread with subject "Dynamic
     * array reallocation algorithms", started on Jan 12
     * 2010.
     *
     * @param minTargetSize   Minimum required value to be returned.
     * @param bytesPerElement Bytes used by each element of
     *                        the array.
     * @lucene.internal
     */

    public static int oversize(int minTargetSize, int bytesPerElement) {

        if (minTargetSize < 0) {
            // catch usage that accidentally overflows int
            throw new IllegalArgumentException("invalid array size " + minTargetSize);
        }

        if (minTargetSize == 0) {
            // wait until at least one element is requested
            return 0;
        }

        // asymptotic exponential growth by 1/8th, favors
        // spending a bit more CPU to not tie up too much wasted
        // RAM:
        int extra = minTargetSize >> 3;

        if (extra < 3) {
            // for very small arrays, where constant overhead of
            // realloc is presumably relatively high, we grow
            // faster
            extra = 3;
        }

        int newSize = minTargetSize + extra;

        // add 7 to allow for worst case byte alignment addition below:
        if (newSize + 7 < 0) {
            // int overflowed -- return max allowed array size
            return Integer.MAX_VALUE;
        }

        if (JRE_IS_64BIT) {
            // round up to 8 byte alignment in 64bit env
            switch (bytesPerElement) {
                case 4:
                    // round up to multiple of 2
                    return (newSize + 1) & 0x7ffffffe;
                case 2:
                    // round up to multiple of 4
                    return (newSize + 3) & 0x7ffffffc;
                case 1:
                    // round up to multiple of 8
                    return (newSize + 7) & 0x7ffffff8;
                case 8:
                    // no rounding
                default:
                    // odd (invalid?) size
                    return newSize;
            }
        } else {
            // round up to 4 byte alignment in 64bit env
            switch (bytesPerElement) {
                case 2:
                    // round up to multiple of 2
                    return (newSize + 1) & 0x7ffffffe;
                case 1:
                    // round up to multiple of 4
                    return (newSize + 3) & 0x7ffffffc;
                case 4:
                case 8:
                    // no rounding
                default:
                    // odd (invalid?) size
                    return newSize;
            }
        }
    }

    public static final boolean JRE_IS_64BIT;
    static {
        String OS_ARCH = System.getProperty("os.arch");
        String x = System.getProperty("sun.arch.data.model");
        if (x != null) {
            JRE_IS_64BIT = x.indexOf("64") != -1;
        } else {
            if (OS_ARCH != null && OS_ARCH.indexOf("64") != -1) {
                JRE_IS_64BIT = true;
            } else {
                JRE_IS_64BIT = false;
            }
        }
    }

}