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
package org.xbib.io.tar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.xbib.io.Session;
import org.xbib.io.StreamCodecService;
import org.xbib.io.StringPacket;

/**
 * Tar Session
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class TarSession implements Session<StringPacket> {

    private final StreamCodecService codecFactory = StreamCodecService.getInstance();
    private boolean isOpen;
    private FileInputStream fin;
    private FileOutputStream fout;
    private TarInputStream in;
    private TarOutputStream out;
    private URI uri;
    private String encoding = System.getProperty("file.encoding");

    public void setURI(URI uri) {
        this.uri = uri;
    }

    @Override
    public synchronized void open(Mode mode) throws IOException {
        if (isOpen) {
            return;
        }
        String scheme = uri.getScheme();
        switch (mode) {
            case READ:
                if (scheme.equals("targz")) {
                    FileInputStream fin = createFileInputStream(".tar.gz");
                    this.in = new TarInputStream(codecFactory.getCodec("gz").decode(fin));
                    this.isOpen = true;
                } else if (scheme.equals("tarbz2")) {
                    FileInputStream fin = createFileInputStream(".tar.bz2");
                    this.in = new TarInputStream(codecFactory.getCodec("bz2").decode(fin));
                    this.isOpen = true;
                } else if (scheme.equals("tarxz")) {
                    FileInputStream fin = createFileInputStream(".tar.xz");
                    this.in = new TarInputStream(codecFactory.getCodec("xz").decode(fin));
                    this.isOpen = true;
                } else {
                    FileInputStream fin = createFileInputStream(".tar");
                    this.in = new TarInputStream(fin);
                    this.isOpen = true;
                }
                break;
            case WRITE:
                if (scheme.equals("targz")) {
                    FileOutputStream fout = createFileOutputStream(".tar.gz");
                    this.out = new TarOutputStream(codecFactory.getCodec("gz").encode(fout));
                    out.setLongFileMode(TarOutputStream.LONGFILE_GNU);
                    this.isOpen = true;
                } else if (scheme.equals("tarbz2")) {
                    FileOutputStream fout = createFileOutputStream(".tar.bz2");
                    this.out = new TarOutputStream(codecFactory.getCodec("bz2").encode(fout));
                    out.setLongFileMode(TarOutputStream.LONGFILE_GNU);
                    this.isOpen = true;
                } else if (scheme.equals("tarxz")) {
                    FileOutputStream fout = createFileOutputStream(".tar.xz");
                    this.out = new TarOutputStream(codecFactory.getCodec("xz").encode(fout));
                    out.setLongFileMode(TarOutputStream.LONGFILE_GNU);
                    this.isOpen = true;
                } else {
                    FileOutputStream fout = createFileOutputStream(".tar");
                    this.out = new TarOutputStream(fout);
                    out.setLongFileMode(TarOutputStream.LONGFILE_GNU);
                    this.isOpen = true;
                }
                break;
        }
    }

    /**
     * Close Tar session
     */
    @Override
    public synchronized void close() throws IOException {
        if (!isOpen) {
            return;
        }
        if (out != null) {
            out.flush();
            out.close();
            out = null;
        }
        if (in != null) {
            in.close();
        }
        this.isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    private synchronized FileOutputStream createFileOutputStream(String suffix) throws IOException {
        String part = uri.getSchemeSpecificPart();
        String name = part.endsWith(suffix) ? part : part + suffix;
        File f = new File(name);
        if (!f.getAbsoluteFile().getParentFile().exists()
                && !f.getAbsoluteFile().getParentFile().mkdirs()) {
            throw new RuntimeException(
                    "Could not create directories to store file: " + f);
        }
        if (f.exists()) {
            throw new IOException("file " + f.getAbsolutePath() + " already exists");
        }
        return new FileOutputStream(f);
    }

    private synchronized FileInputStream createFileInputStream(String suffix) throws IOException {
        String part = uri.getSchemeSpecificPart();
        String name = part.endsWith(suffix) ? part : part + suffix;
        File f = new File(name);
        if (f.isFile() && f.canRead()) {
            return new FileInputStream(f);
        }
        throw new FileNotFoundException("check existence or access rights: " + f.getAbsolutePath());
    }

    @Override
    public StringPacket read() throws IOException {
        if (!isOpen()) {
            throw new IOException(" not open");
        }
        if (in == null) {
            throw new IOException("no input stream");
        }
        TarEntry entry = in.getNextEntry();
        if (entry == null) {
            return null;
        }
        StringPacket packet = new StringPacket();
        String name = entry.getName();
        packet.name(name);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        in.copyEntryContents(bout);
        packet.packet(new String(bout.toByteArray(), encoding));
        return packet;
    }

    @Override
    public void write(StringPacket packet) throws IOException {
        if (packet == null || packet.packet() == null) {
            throw new IOException("no packet to write");
        }
        byte[] buf = packet.packet().getBytes();
        if (buf.length > 0) {
            String name = createEntryName(packet.name(), packet.number());
            System.err.println("PACKET WRITE!!! "+ new String(buf));
            TarEntry entry = new TarEntry(name);
            entry.setModTime(new Date());
            entry.setSize(buf.length);
            out.putNextEntry(entry);
            out.write(buf);
            out.closeEntry();
        }
    }

    private AtomicLong counter = new AtomicLong();

    private static final NumberFormat nf = DecimalFormat.getIntegerInstance();

    static {
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(12);
    }

    private String createEntryName(String name, long number) {
        // distribute files over directories (0-9999 = 10.000 per directory)
        String d = nf.format(counter.incrementAndGet());
        String nameComponent = name != null && name.length() > 0 ? name + "/" : "";
        String numberComponent =  "/" + number;
        StringBuilder sb = new StringBuilder();
        sb.append(nameComponent).append(d.substring(0, 4)).append("/").append(d.substring(4, 8)).append(numberComponent);
        return sb.toString();
    }

    @Override
    public StringPacket newPacket() {
        return new StringPacket();
    }
}
