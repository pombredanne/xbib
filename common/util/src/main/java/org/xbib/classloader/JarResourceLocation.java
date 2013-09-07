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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

public class JarResourceLocation extends AbstractUrlResourceLocation {

    private JarFile jarFile;
    private byte content[];

    public JarResourceLocation(URL codeSource, File cacheFile) throws IOException {
        super(codeSource);
        try {
            jarFile = new JarFile(cacheFile);
        } catch (ZipException ze) {
            // We get this exception on windows when the
            // path to the jar file gets too long (Bug ID: 6374379)
            InputStream is = null;
            try {
                is = new FileInputStream(cacheFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int bytesRead = -1;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                this.content = baos.toByteArray();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    public ResourceHandle getResourceHandle(String resourceName) {
        if (jarFile != null) {
            JarEntry jarEntry = jarFile.getJarEntry(resourceName);
            if (jarEntry != null) {
                try {
                    URL url = new URL(getCodeSource(), resourceName);
                    return new JarResourceHandle(jarFile, jarEntry, getCodeSource());
                } catch (MalformedURLException e) {
                }
            }
        } else {
            try {
                final JarInputStream is = new JarInputStream(new ByteArrayInputStream(this.content));
                JarEntry jarEntry = null;
                while ((jarEntry = is.getNextJarEntry()) != null) {
                    if (jarEntry.getName().equals(resourceName)) {
                        try {
                            URL url = new URL(getCodeSource(), resourceName);
                            final JarEntry jarEntry2 = jarEntry;
                            return new JarEntryResourceHandle(jarEntry2, is);
                        } catch (MalformedURLException e) {
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    public Manifest getManifest() throws IOException {
        if (jarFile != null) {
            return jarFile.getManifest();
        } else {
            try {
                JarInputStream is = new JarInputStream(new ByteArrayInputStream(this.content));
                return is.getManifest();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public void close() {
        if (jarFile != null) {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private class JarEntryResourceHandle extends AbstractResourceHandle {

        private final JarEntry jarEntry2;
        private final JarInputStream is;

        public JarEntryResourceHandle(JarEntry jarEntry2, JarInputStream is) {
            this.jarEntry2 = jarEntry2;
            this.is = is;
        }

        public String getName() {
            return jarEntry2.getName();
        }

        public URL getUrl() {
            try {
                return new URL("jar", "", -1, getCodeSource() + "!/" + jarEntry2.getName());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isDirectory() {
            return jarEntry2.isDirectory();
        }

        public URL getCodeSourceUrl() {
            return getCodeSource();
        }

        public InputStream getInputStream() throws IOException {
            return is;
        }

        public int getContentLength() {
            return (int) jarEntry2.getSize();
        }
    }
}
