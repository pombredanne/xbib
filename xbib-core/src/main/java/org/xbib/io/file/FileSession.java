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
package org.xbib.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import org.xbib.io.Identifiable;
import org.xbib.io.Mode;
import org.xbib.io.Packet;
import org.xbib.io.PacketSession;
import org.xbib.io.StreamCodecService;
import org.xbib.io.operator.CreateOperator;
import org.xbib.io.operator.ReadOperator;

/**
 * A File session
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class FileSession implements PacketSession<FileSession> {

    private final StreamCodecService factory = StreamCodecService.getInstance();
    private final FileReadOperation readOp = new FileReadOperation();
    private final FileWriteOperation writeOp = new FileWriteOperation();
    private URI uri;
    private boolean isOpen;
    private File file;
    private OutputStream out;
    private InputStream in;

    public FileSession(URI uri) {
        this.isOpen = false;
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        this.uri = uri;
    }

    @Override
    public void open(Mode mode) throws IOException {
        if (isOpen()) {
            return;
        }
        this.isOpen = false;
        String filename = getName();
        if ("filegz".equals(uri.getScheme()) && !filename.endsWith(".gz")) {
            filename = filename + ".gz";
        }
        if ("filebz2".equals(uri.getScheme()) && !filename.endsWith(".bz2")) {
            filename = filename + ".bz2";
        }
        this.file = new File(filename);
        switch (mode) {
            case READ:
                if (!file.exists()) {
                    throw new IOException("file does not exist: " + uri);
                }
                this.in = new FileInputStream(file);
                if (filename.endsWith(".gz")) {
                    this.in =  factory.getCodec("gz").decode(in);
                }
                else if (filename.endsWith(".bz2")) {
                    this.in = factory.getCodec("bz2").decode(in);
                }
                else if (filename.endsWith(".xz")) {
                    this.in = factory.getCodec("xz").decode(in);
                }
                this.isOpen = true;
                break;
            case WRITE:
                if (file.exists()) {
                    throw new IOException("not overwriting file: " + uri);
                } else {
                    // create directories if required
                    file.getParentFile().mkdirs();
                }
                this.out = new FileOutputStream(file);
                if (filename.endsWith(".gz")) {
                    this.out =  factory.getCodec("gz").encode(out);
                }
                if (filename.endsWith(".bz2")) {
                    this.out =  factory.getCodec("bz2").encode(out);
                }
                this.isOpen = true;
                break;
            case APPEND:
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                this.out = new FileOutputStream(file, true);
                if (filename.endsWith(".gz")) {
                    this.out =  factory.getCodec("gz").encode(out);
                }
                if (filename.endsWith(".bz2")) {
                    this.out =  factory.getCodec("bz2").encode(out);
                }
                this.isOpen = true;
                break;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
            this.isOpen = false;
        }
        if (out != null) {
            out.close();
            this.isOpen = false;
        }
    }

    @Override
    public ReadOperator<FileSession, Identifiable, Packet> createPacketReadOperator() {
        return readOp;
    }

    @Override
    public CreateOperator<FileSession, Identifiable, Packet> createPacketWriteOperator() {
        return writeOp;
    }

    @Override
    public String getName() {
        return uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getPath();
    }
}
