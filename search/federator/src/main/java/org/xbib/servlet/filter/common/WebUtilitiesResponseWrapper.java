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
package org.xbib.servlet.filter.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import static org.xbib.servlet.filter.common.Constants.HTTP_CONTENT_TYPE_HEADER;

/**
 * Common Simple Servlet Response Wrapper using WebUtilitiesResponseOutputStream
 *
 */
public class WebUtilitiesResponseWrapper extends HttpServletResponseWrapper {

    private WebUtilitiesResponseOutputStream stream;
    private Map<String, Object> headers = new HashMap<>();
    private Set<Cookie> cookies = new HashSet<>();
    private String contentType;
    private int status = 0;
    private boolean getWriterCalled = false;
    private boolean getStreamCalled = false;
    private PrintWriter printWriter;

    @Override
    public void addCookie(Cookie cookie) {
        super.addCookie(cookie);
        cookies.add(cookie);
    }

    @Override
    public void setStatus(int sc, String sm) {
        if (this.status != 0) {
            return;
        }
        super.setStatus(sc, sm);
        this.status = sc;
    }

    @Override
    public void setStatus(int sc) {
        if (this.status != 0) {
            return;
        }
        super.setStatus(sc);
        this.status = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        if (this.status != 0) {
            return;
        }
        super.sendError(sc);
        this.status = sc;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (this.status != 0) {
            return;
        }
        super.sendError(sc, msg);
        this.status = sc;
    }

    @Override
    public void addDateHeader(String name, long date) {
        super.addDateHeader(name, date);
        headers.put(name, date);
    }

    @Override
    public void addHeader(String name, String value) {
        if (HTTP_CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            this.setContentType(value);
        } else {
            super.addHeader(name, value);
            headers.put(name, value);
        }
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        headers.put(name, value);
    }

    @Override
    public void setDateHeader(String name, long date) {
        super.setDateHeader(name, date);
        headers.put(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        if (HTTP_CONTENT_TYPE_HEADER.equalsIgnoreCase(name)) {
            this.setContentType(value);
        } else {
            super.setHeader(name, value);
            headers.put(name, value);
        }
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        headers.put(name, value);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String type) {
        super.setContentType(type);
        this.contentType = type;
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (getWriterCalled) {
            throw new IllegalStateException("getWriter already called.");
        }
        getStreamCalled = true;
        return this.stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (getStreamCalled) {
            throw new IllegalStateException("getStream already called.");
        }
        getWriterCalled = true;
        if (printWriter == null) {
            printWriter = new PrintWriter(stream);
        }
        return printWriter;
    }

    public int getStatus() {
        return status;
    }

    private void flushWriter() {
        if (getWriterCalled && printWriter != null) {
            printWriter.flush();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (stream != null) {
            stream.flush();
        }
    }

    @Override
    public void reset() {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (stream != null) {
            stream.reset();
        }
        //getResponse().reset();

    }

    @Override
    public void resetBuffer() {
        flushWriter(); // make sure nothing is buffered in the writer, if applicable
        if (stream != null) {
            stream.reset();
        }
        //getResponse().resetBuffer();
    }

    public String getContents() {
        return new String(getBytes());
    }

    public byte[] getBytes() {
        return stream.getByteArrayOutputStream().toByteArray();
    }

    public WebUtilitiesResponseWrapper(HttpServletResponse response) {
        super(response);
        stream = new WebUtilitiesResponseOutputStream(this);
    }

    public void fill(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(this.getCharacterEncoding());
        response.setContentType(this.getContentType());

        for (Cookie cookie : this.getCookies()) {
            response.addCookie(cookie);
        }
        for (String headerName : this.getHeaders().keySet()) {
            Object value = this.getHeaders().get(headerName);
            if (value instanceof Long) {
                response.setDateHeader(headerName, ((Long) value));
            } else if (value instanceof Integer) {
                response.setIntHeader(headerName, ((Integer) value));
            } else {
                response.setHeader(headerName, value.toString());
            }
        }
        response.setStatus(this.getStatus());
        this.flushWriter();
        try {
            response.getOutputStream().write(this.getBytes());
            response.getOutputStream().close();
        } catch (RuntimeException ex) {
            try {
                response.getWriter().write(this.getContents());
                response.getWriter().close();
            } catch (Exception ex1) {
                throw new IOException(ex);
            }
        }

        if (response instanceof WebUtilitiesResponseWrapper) {
            ((WebUtilitiesResponseWrapper) response).fill((HttpServletResponse) ((WebUtilitiesResponseWrapper) response).getResponse());
        }

    }
}