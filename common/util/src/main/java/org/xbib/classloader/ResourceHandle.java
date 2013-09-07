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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * This is a handle (a connection) to some resource, which may
 * be a class, native library, text file, image, etc. Handles are returned
 * by a ResourceFinder. A resource handle allows easy access to the resource data
 * (using methods {@link #getInputStream} or {@link #getBytes}) as well as
 * access resource metadata, such as attributes, certificates, etc.
 * <p/>
 * As soon as the handle is no longer in use, it should be explicitly
 * {@link #close}d, similarly to I/O streams.
 *
 */
public interface ResourceHandle {
    /**
     * Return the name of the resource. The name is a "/"-separated path
     * name that identifies the resource.
     */
    String getName();

    /**
     * Returns the URL of the resource.
     */
    URL getUrl();

    /**
     * Does this resource refer to a directory.  Directory resources are commly used
     * as the basis for a URL in client application.  A directory resource has 0 bytes for it's content. 
     */
    boolean isDirectory();

    /**
     * Returns the CodeSource URL for the class or resource.
     */
    URL getCodeSourceUrl();

    /**
     * Returns and InputStream for reading this resource data.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the length of this resource data, or -1 if unknown.
     */
    int getContentLength();

    /**
     * Returns this resource data as an array of bytes.
     */
    byte[] getBytes() throws IOException;

    /**
     * Returns the Manifest of the JAR file from which this resource
     * was loaded, or null if none.
     */
    Manifest getManifest() throws IOException;

    /**
     * Return the Certificates of the resource, or null if none.
     */
    Certificate[] getCertificates();

    /**
     * Return the Attributes of the resource, or null if none.
     */
    Attributes getAttributes() throws IOException;

    /**
     * Closes a connection to the resource indentified by this handle. Releases
     * any I/O objects associated with the handle.
     */
    void close();
}
