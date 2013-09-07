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
import java.io.OutputStream;

/**
 * Output stream wrapper that tracks the number of bytes that have been written
 * through the stream. When data is written through this stream the count of
 * bytes is increased, and at a set minimum interval (eg after at least 1024
 * bytes) a {@link org.xbib.io.progress.BytesProgressWatcher} implementation is notified of the count
 * of bytes read since the last notification.
 *
 */
public class ProgressMonitoredOutputStream extends OutputStream implements OutputStreamWrapper {

    private OutputStream outputStream = null;
    private BytesProgressWatcher progressWatcher = null;

    /**
     * Construts the output stream around an underlying stream and sends
     * notification messages to a progress watcher when bytes are written to the
     * stream.
     *
     * @param outputStream the output stream to wrap, whose byte transfer count
     * will be monitored.
     * @param progressWatcher a watcher object that stores information about the
     * bytes read from a stream, and allows calculations to be perfomed using
     * this information.
     */
    public ProgressMonitoredOutputStream(OutputStream outputStream, BytesProgressWatcher progressWatcher) {
        if (outputStream == null) {
            throw new IllegalArgumentException(
                    "ProgressMonitoredOutputStream cannot run with a null OutputStream");
        }
        this.outputStream = outputStream;
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
    public void write(int b) throws IOException {
        outputStream.write(b);
        sendNotificationUpdate(1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
        sendNotificationUpdate(len - off);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b.length);
        sendNotificationUpdate(b.length);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public OutputStream getWrappedOutputStream() {
        return outputStream;
    }
}