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
package org.xbib.io.progress;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream wrapper that tracks the number of bytes that have been read
 * through the stream. When data is read through this stream the count of bytes
 * is increased and the associated
 * {@link org.xbib.io.progress.BytesProgressWatcher} object is notified of the count of bytes read.
 */
public class ProgressMonitoredInputStream extends InputStream implements InputStreamWrapper {

    private InputStream inputStream = null;
    private BytesProgressWatcher progressWatcher = null;

    /**
     * Construts the input stream around an underlying stream and sends
     * notification messages to a progress watcher when bytes are read from the
     * stream.
     *
     * @param inputStream the input stream to wrap, whose byte transfer count
     * will be monitored.
     * @param progressWatcher a watcher object that stores information about the
     * bytes read from a stream, and allows calculations to be perfomed using
     * this information.
     */
    public ProgressMonitoredInputStream(InputStream inputStream, BytesProgressWatcher progressWatcher) {
        if (inputStream == null) {
            throw new IllegalArgumentException(
                    "ProgressMonitoredInputStream cannot run with a null InputStream");
        }
        this.inputStream = inputStream;
        this.progressWatcher = progressWatcher;
    }

    /**
     * Checks how many bytes have been transferred since the last notification,
     * and sends a notification message if this number exceeds the minimum bytes
     * transferred value.
     *
     * @param bytesTransmitted
     */
    public void sendNotificationUpdate(long bytesTransmitted) {
        progressWatcher.updateBytesTransferred(bytesTransmitted);
    }

    public void resetProgressMonitor() {
        progressWatcher.resetWatcher();
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        if (read != -1) {
            sendNotificationUpdate(1);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = inputStream.read(b, off, len);
        if (read != -1) {
            sendNotificationUpdate(read);
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = inputStream.read(b);
        if (read != -1) {
            sendNotificationUpdate(read);
        }
        return read;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public InputStream getWrappedInputStream() {
        return inputStream;
    }
}