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
package org.xbib.io.archivers;

import org.xbib.io.ObjectPacket;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.StreamCodecService;
import org.xbib.io.archivers.tar.TarArchiveEntry;
import org.xbib.io.archivers.tar.TarArchiveInputStream;
import org.xbib.io.archivers.tar.TarArchiveOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tar Session
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class TarSession implements Session {

    private final StreamCodecService codecFactory = StreamCodecService.getInstance();
    private boolean isOpen;
    private FileInputStream fin;
    private FileOutputStream fout;
    private TarArchiveInputStream in;
    private TarArchiveOutputStream out;
    private String scheme;
    private String part;

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setName(String name) {
        this.part = name;
    }

    public String getName() {
        return part;
    }

    @Override
    public synchronized void open(Mode mode) throws IOException {
        if (isOpen) {
            return;
        }
        switch (mode) {
            case READ:
                if (scheme.startsWith("targz")) {
                    String s = part + ".tar.gz";
                    File f = new File(s);
                    if (f.isFile() && f.canRead()) {
                        this.fin = new FileInputStream(f);
                        this.in = new TarArchiveInputStream(codecFactory.getCodec("gz").decode(fin));
                        this.isOpen = true;
                    } else {
                        throw new FileNotFoundException("check existence or access rights: " + s);
                    }
                } else if (scheme.startsWith("tarbz2")) {
                    String s = part + ".tar.bz2";
                    File f = new File(s);
                    if (f.isFile() && f.canRead()) {
                        this.fin = new FileInputStream(f);
                        this.in = new TarArchiveInputStream(codecFactory.getCodec("bz2").decode(fin));
                        this.isOpen = true;
                    } else {
                        throw new FileNotFoundException("check existence or access rights: " + s);
                    }
                } else if (scheme.startsWith("tarxz")) {
                    String s = part + ".tar.xz";
                    File f = new File(s);
                    if (f.isFile() && f.canRead()) {
                        this.fin = new FileInputStream(f);
                        this.in = new TarArchiveInputStream(codecFactory.getCodec("xz").decode(fin));
                        this.isOpen = true;
                    } else {
                        throw new FileNotFoundException("check existence or access rights: " + s);
                    }
                } else {
                    String s = part + ".tar";
                    File f = new File(s);
                    if (f.isFile() && f.canRead()) {
                        this.fin = new FileInputStream(f);
                        this.in = new TarArchiveInputStream(fin);
                        this.isOpen = true;
                    } else {
                        throw new FileNotFoundException("check existence or access rights: " + s);
                    }
                }
                break;
            case WRITE:
                if (scheme.equals("targz")) {
                    createFileOutputStream(".tar.gz");
                    this.out = new TarArchiveOutputStream(codecFactory.getCodec("gz").encode(fout));
                    out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    this.isOpen = true;
                } else if (scheme.equals("tarbz2")) {
                    createFileOutputStream(".tar.bz2");
                    this.out = new TarArchiveOutputStream(codecFactory.getCodec("bz2").encode(fout));
                    out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    this.isOpen = true;
                } else if (scheme.equals("tarxz")) {
                    createFileOutputStream(".tar.xz");
                    this.out = new TarArchiveOutputStream(codecFactory.getCodec("xz").encode(fout));
                    out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    this.isOpen = true;
                } else {
                    createFileOutputStream(".tar");
                    this.out = new TarArchiveOutputStream(fout);
                    out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
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

    TarArchiveInputStream getInputStream() {
        return in;
    }

    TarArchiveOutputStream getOutputStream() {
        return out;
    }

    /**
     * Helper method for creating the FileOutputStream. Creates the directory if
     * it doesnot exist.
     *
     * @param suffix
     * @throws java.io.IOException
     */
    private synchronized void createFileOutputStream(String suffix) throws IOException {
        File f = new File(part + (part.endsWith(suffix) ? "" : suffix));
        if (!f.getAbsoluteFile().getParentFile().exists()
                && !f.getAbsoluteFile().getParentFile().mkdirs()) {
            throw new RuntimeException(
                    "Could not create directories to store file: " + f);
        }
        if (f.exists()) {
            throw new IOException("file " + f.getAbsolutePath() + " already exists");
        }
        this.fout = new FileOutputStream(f);
    }

    @Override
    public Packet read() throws IOException {
        if (!isOpen()) {
            throw new IOException("not open");
        }
        if (getInputStream() == null) {
            throw new IOException("no tar input stream");
        }
        TarArchiveEntry entry = getInputStream().getNextTarEntry();
        if (entry == null) {
            return null;
        }
        ObjectPacket packet = new ObjectPacket();
        String name = entry.getName();
        packet.setName(name);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(getInputStream(), bout);
        packet.setPacket(new String(bout.toByteArray()));
        return packet;
    }

    @Override
    public void write(Packet packet) throws IOException {
        if (packet == null || packet.toString() == null) {
            throw new IOException("no packet to write");
        }
        byte[] buf = packet.toString().getBytes();
        if (buf.length > 0) {
            String name = createEntryName(packet.getName(), packet.getNumber());
            TarArchiveEntry entry = new TarArchiveEntry(name);
            entry.setModTime(new Date());
            entry.setSize(buf.length);
            getOutputStream().putArchiveEntry(entry);
            getOutputStream().write(buf);
            getOutputStream().closeArchiveEntry();
        }
    }

    private AtomicLong counter = new AtomicLong();
    private static final NumberFormat nf = DecimalFormat.getIntegerInstance();

    static {
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(12);
    }


    private String createEntryName(String name, String number) {
        StringBuilder sb = new StringBuilder();
        if (name != null && name.length() > 0) {
            sb.append(name);
        }
        if (number != null) {
            // distribute numbered entries over 12-digit directories (10.000 per directory)
            String d = nf.format(counter.incrementAndGet());
            sb.append("/").append(d.substring(0, 4))
                    .append("/").append(d.substring(4, 8))
                    .append("/").append(d.substring(8, 12))
                    .append("/").append(number);
        }
        return sb.toString();
    }

    @Override
    public Packet newPacket() {
        return new ObjectPacket();
    }
}
