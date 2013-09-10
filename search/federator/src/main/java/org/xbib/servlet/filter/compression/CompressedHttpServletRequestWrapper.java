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
package org.xbib.servlet.filter.compression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import static org.xbib.servlet.filter.common.Constants.HTTP_ACCEPT_ENCODING_HEADER;
import static org.xbib.servlet.filter.common.Constants.HTTP_CONTENT_ENCODING_HEADER;

public final class CompressedHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final HttpServletRequest request;
    private final EncodedStreamsFactory encodedStreamsFactory;
    private CompressedServletInputStream compressedStream;
    private BufferedReader bufferedReader;
    private boolean getInputStreamCalled;
    private boolean getReaderCalled;

    public CompressedHttpServletRequestWrapper(HttpServletRequest request, EncodedStreamsFactory encodedStreamsFactory) {
        super(request);
        this.request = request;
        this.encodedStreamsFactory = encodedStreamsFactory;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (getReaderCalled) {
            throw new IllegalStateException("getReader() has been already called");
        }
        getInputStreamCalled = true;
        return getCompressedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (getInputStreamCalled) {
            throw new IllegalStateException("getInputStream() has been already called");
        }
        getReaderCalled = true;
        if (bufferedReader == null) {
            bufferedReader = new BufferedReader(new InputStreamReader(getCompressedServletInputStream(),
                    getCharacterEncoding()));
        }
        return bufferedReader;
    }

    private CompressedServletInputStream getCompressedServletInputStream() throws IOException {
        if (compressedStream == null) {
            compressedStream = new CompressedServletInputStream(request.getInputStream(),
                    encodedStreamsFactory);
        }
        return compressedStream;
    }

    private static boolean skippedHeader(String headerName) {
        return HTTP_ACCEPT_ENCODING_HEADER.equalsIgnoreCase(headerName) ||
                HTTP_CONTENT_ENCODING_HEADER.equalsIgnoreCase(headerName);
    }

    @Override
    public String getHeader(String header) {
        return skippedHeader(header) ? null : super.getHeader(header);
    }

    @Override
    public Enumeration<String> getHeaders(String header) {
        Enumeration<String> original = super.getHeaders(header);
        if (original == null) {
            return null;
        }
        return skippedHeader(header) ? Collections.enumeration(Collections.<String>emptyList()) : original;
    }

    @Override
    public long getDateHeader(String header) {
        return skippedHeader(header) ? -1L : super.getDateHeader(header);
    }

    @Override
    public int getIntHeader(String header) {
        return skippedHeader(header) ? -1 : super.getIntHeader(header);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Enumeration<String> originalHeaderNames = super.getHeaderNames();
        if (originalHeaderNames == null) {
            return null;
        }

        Collection<String> headerNames = new ArrayList();
        while (originalHeaderNames.hasMoreElements()) {
            String headerName = originalHeaderNames.nextElement();
            if (!skippedHeader(headerName)) {
                headerNames.add(headerName);
            }
        }
        return Collections.enumeration(headerNames);
    }

}