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
import java.io.OutputStream;
import org.xbib.io.compress.BufferRecycler;

/**
 * Decorator {@link OutputStream} implementation that will compress output using
 * LZF compression algorithm, given uncompressed input to write. Its counterpart
 * is {@link LZFInputStream}; although in some ways
 * {@link LZFCompressingInputStream} can be seen as the opposite.
 *
 */
public class LZFOutputStream extends OutputStream {

    private final ChunkEncoder _encoder;
    private final BufferRecycler _recycler;
    protected final OutputStream _outputStream;
    protected byte[] _outputBuffer;
    protected int _position = 0;
    /**
     * Configuration setting that governs whether basic 'flush()' should first
     * complete a block or not. <p> Default value is 'true'
     */
    protected boolean _cfgFinishBlockOnFlush = true;
    /**
     * Flag that indicates if we have already called '_outputStream.close()' (to
     * avoid calling it multiple times)
     */
    protected boolean _outputStreamClosed;

    /*
     // Construction, configuration
     */
    public LZFOutputStream(final OutputStream outputStream) {
        this(outputStream, LZFChunk.MAX_CHUNK_LEN);
    }

    public LZFOutputStream(final OutputStream outputStream, int bufsize) {
        _encoder = new ChunkEncoder(bufsize);
        _recycler = BufferRecycler.instance();
        _outputStream = outputStream;
        _outputBuffer = _recycler.allocOutputBuffer(bufsize);
        _outputStreamClosed = false;
    }

    /**
     * Method for defining whether call to {@link #flush} will also complete
     * current block (similar to calling {@link #finishBlock()}) or not.
     */
    public LZFOutputStream setFinishBlockOnFlush(boolean b) {
        _cfgFinishBlockOnFlush = b;
        return this;
    }

    /*
     // OutputStream impl
     */
    @Override
    public void write(final int singleByte) throws IOException {
        checkNotClosed();
        if (_position >= _outputBuffer.length) {
            writeCompressedBlock();
        }
        _outputBuffer[_position++] = (byte) singleByte;
    }

    @Override
    public void write(final byte[] buffer, int offset, int length) throws IOException {
        checkNotClosed();

        final int BUFFER_LEN = _outputBuffer.length;

        // simple case first: buffering only (for trivially short writes)
        int free = BUFFER_LEN - _position;
        if (free >= length) {
            System.arraycopy(buffer, offset, _outputBuffer, _position, length);
            _position += length;
            return;
        }
        // otherwise, copy whatever we can, flush
        System.arraycopy(buffer, offset, _outputBuffer, _position, free);
        offset += free;
        length -= free;
        _position += free;
        writeCompressedBlock();

        // then write intermediate full block, if any, without copying:
        while (length >= BUFFER_LEN) {
            _encoder.encodeAndWriteChunk(buffer, offset, BUFFER_LEN, _outputStream);
            offset += BUFFER_LEN;
            length -= BUFFER_LEN;
        }

        // and finally, copy leftovers in buffer, if any
        if (length > 0) {
            System.arraycopy(buffer, offset, _outputBuffer, 0, length);
        }
        _position = length;
    }

    @Override
    public void flush() throws IOException {
        checkNotClosed();
        if (_cfgFinishBlockOnFlush && _position > 0) {
            writeCompressedBlock();
        }
        _outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        if (!_outputStreamClosed) {
            if (_position > 0) {
                writeCompressedBlock();
            }
            _outputStream.flush();
            _encoder.close();
            byte[] buf = _outputBuffer;
            if (buf != null) {
                _outputBuffer = null;
                _recycler.releaseOutputBuffer(buf);
            }
            _outputStreamClosed = true;
            _outputStream.close();
        }
    }

    /*
     ///////////////////////////////////////////////////////////////////////
     // Additional public methods
     ///////////////////////////////////////////////////////////////////////
     */
    /**
     * Method that can be used to find underlying {@link OutputStream} that we
     * write encoded LZF encoded data into, after compressing it. Will never
     * return null; although underlying stream may be closed (if this stream has
     * been closed).
     *
     * @since 0.8
     */
    public OutputStream getUnderlyingOutputStream() {
        return _outputStream;
    }

    /**
     * Accessor for checking whether call to "flush()" will first finish the
     * current block or not
     *
     * @since 0.8
     */
    public boolean getFinishBlockOnFlush() {
        return _cfgFinishBlockOnFlush;
    }

    /**
     * Method that can be used to force completion of the current block, which
     * means that all buffered data will be compressed into an LZF block. This
     * typically results in lower compression ratio as larger blocks compress
     * better; but may be necessary for network connections to ensure timely
     * sending of data.
     *
     * @since 0.8
     */
    public LZFOutputStream finishBlock() throws IOException {
        checkNotClosed();
        if (_position > 0) {
            writeCompressedBlock();
        }
        return this;
    }

    /*
     ///////////////////////////////////////////////////////////////////////
     // Internal methods
     ///////////////////////////////////////////////////////////////////////
     */
    /**
     * Compress and write the current block to the OutputStream
     */
    protected void writeCompressedBlock() throws IOException {
        int left = _position;
        _position = 0;
        int offset = 0;

        do {
            int chunkLen = Math.min(LZFChunk.MAX_CHUNK_LEN, left);
            _encoder.encodeAndWriteChunk(_outputBuffer, offset, chunkLen, _outputStream);
            offset += chunkLen;
            left -= chunkLen;
        } while (left > 0);
    }

    protected void checkNotClosed() throws IOException {
        if (_outputStreamClosed) {
            throw new IOException(getClass().getName() + " already closed");
        }
    }
}
