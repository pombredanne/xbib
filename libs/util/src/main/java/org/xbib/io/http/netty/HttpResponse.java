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
package org.xbib.io.http.netty;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import java.net.URI;

public class HttpResponse extends HttpPacket {
    
    private URI uri;
    private int statusCode;
    private FluentCaseInsensitiveStringsMap headers;
    private String body;
    private Throwable throwable;

    public HttpResponse() {
    }
    
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    public URI getURI() {
        return uri;
    }
    
    public HttpResponse setStatusCode(int code) {
        this.statusCode = code;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }
    
    public HttpResponse setHeaders(FluentCaseInsensitiveStringsMap headers) {
        this.headers = headers;
        return this;
    }
    
    public FluentCaseInsensitiveStringsMap getHeaders() {
        return headers;
    }
    
    public HttpResponse setBody(String body) {
        this.body = body;
        return this;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
    public boolean ok() {
       return getStatusCode() == 200 && getThrowable() == null;
    }

    public boolean forbidden() {
        return getStatusCode() == 403;
    }

    public boolean notfound() {
        return getStatusCode() == 404;
    }

    public boolean fatal() {
        return (getStatusCode() >= 500 && getStatusCode() < 600) || getThrowable() != null;
    }


}
