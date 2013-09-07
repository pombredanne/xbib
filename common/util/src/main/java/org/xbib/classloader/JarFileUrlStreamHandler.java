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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileUrlStreamHandler extends URLStreamHandler {

    public static URL createUrl(JarFile jarFile, JarEntry jarEntry) throws MalformedURLException {
        return createUrl(jarFile, jarEntry, new File(jarFile.getName()).toURI().toURL());
    }

    public static URL createUrl(JarFile jarFile, JarEntry jarEntry, URL codeSource) throws MalformedURLException {
        JarFileUrlStreamHandler handler = new JarFileUrlStreamHandler(jarFile, jarEntry);
        URL url = new URL("jar", "", -1, codeSource + "!/" + jarEntry.getName(), handler);
        handler.setExpectedUrl(url);
        return url;
    }
    private URL expectedUrl;
    private final JarFile jarFile;
    private final JarEntry jarEntry;

    public JarFileUrlStreamHandler(JarFile jarFile, JarEntry jarEntry) {
        if (jarFile == null) {
            throw new NullPointerException("jarFile is null");
        }
        if (jarEntry == null) {
            throw new NullPointerException("jarEntry is null");
        }

        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }

    public void setExpectedUrl(URL expectedUrl) {
        if (expectedUrl == null) {
            throw new NullPointerException("expectedUrl is null");
        }
        this.expectedUrl = expectedUrl;
    }

    public URLConnection openConnection(URL url) throws IOException {
        if (expectedUrl == null) {
            throw new IllegalStateException("expectedUrl was not set");
        }

        // the caller copied the URL reusing a stream handler from a previous call
        if (!expectedUrl.equals(url)) {
            // the new url is supposed to be within our context, so it must have a jar protocol
            if (!url.getProtocol().equals("jar")) {
                throw new IllegalArgumentException("Unsupported protocol " + url.getProtocol());
            }

            // split the path at "!/" into the file part and entry part
            String path = url.getPath();
            String[] chunks = path.split("!/", 2);

            // if we only got only one chunk, it didn't contain the required "!/" delimiter
            if (chunks.length == 1) {
                throw new MalformedURLException("Url does not contain a '!' character: " + url);
            }

            String file = chunks[0];
            String entryPath = chunks[1];

            // this handler only supports jars on the local file system
            if (!file.startsWith("file:")) {
                // let the system handler deal with this
                return new URL(url.toExternalForm()).openConnection();
            }
            file = file.substring("file:".length());

            // again the new url is supposed to be within our context so it must reference the same jar file
            if (!jarFile.getName().equals(file)) {
                // let the system handler deal with this
                return new URL(url.toExternalForm()).openConnection();
            }

            // get the entry
            JarEntry newEntry = jarFile.getJarEntry(entryPath);
            if (newEntry == null) {
                throw new FileNotFoundException("Entry not found: " + url);
            }
            return new JarFileUrlConnection(url, jarFile, newEntry);
        }

        return new JarFileUrlConnection(url, jarFile, jarEntry);
    }
}
