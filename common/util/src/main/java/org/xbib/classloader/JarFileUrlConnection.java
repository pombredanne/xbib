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
package org.xbib.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarFileUrlConnection extends JarURLConnection {

    public static final URL DUMMY_JAR_URL;

    static {
        try {
            DUMMY_JAR_URL = new URL("jar", "", -1, "file:dummy!/", new URLStreamHandler() {
                protected URLConnection openConnection(URL u) {
                    throw new UnsupportedOperationException();
                }
            });
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private final URL url;
    private final JarFile jarFile;
    private final JarEntry jarEntry;
    private final URL jarFileUrl;

    public JarFileUrlConnection(URL url, JarFile jarFile, JarEntry jarEntry) throws MalformedURLException {
        super(DUMMY_JAR_URL);

        if (url == null) {
            throw new NullPointerException("url is null");
        }
        if (jarFile == null) {
            throw new NullPointerException("jarFile is null");
        }
        if (jarEntry == null) {
            throw new NullPointerException("jarEntry is null");
        }

        this.url = url;
        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
        jarFileUrl = new File(jarFile.getName()).toURI().toURL();
    }

    @Override
    public JarFile getJarFile() throws IOException {
        if (getUseCaches()) {
            return jarFile;
        } else {
            return new JarFile(jarFile.getName());
        }
    }

    @Override
    public synchronized void connect() {
    }

    public URL getJarFileURL() {
        return jarFileUrl;
    }

    public String getEntryName() {
        return getJarEntry().getName();
    }

    public Manifest getManifest() throws IOException {
        return jarFile.getManifest();
    }

    public JarEntry getJarEntry() {
        if (getUseCaches()) {
            return jarEntry;
        } else {
            //return (JarEntry) jarEntry.clone();
            // There is a clone method, but the below way might be safer.
            return jarFile.getJarEntry(jarEntry.getName());
        }
    }

    public Attributes getAttributes() throws IOException {
        return getJarEntry().getAttributes();
    }

    public Attributes getMainAttributes() throws IOException {
        return getManifest().getMainAttributes();
    }

    public Certificate[] getCertificates() throws IOException {
        return getJarEntry().getCertificates();
    }

    public URL getURL() {
        return url;
    }

    public int getContentLength() {
        long size = getJarEntry().getSize();
        if (size > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) size;
    }

    public long getLastModified() {
        return getJarEntry().getTime();
    }

    public synchronized InputStream getInputStream() throws IOException {
        return jarFile.getInputStream(jarEntry);
    }

    public Permission getPermission() throws IOException {
        URL jarFileUrl = new File(jarFile.getName()).toURI().toURL();
        return jarFileUrl.openConnection().getPermission();
    }

    public String toString() {
        return JarFileUrlConnection.class.getName() + ":" + url;
    }
}
