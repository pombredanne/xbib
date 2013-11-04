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

import org.asynchttpclient.Realm;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import org.xbib.io.http.HttpPacket;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.PreparedHttpRequest;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A default HTTP request
 */
public class NettyHttpRequest extends HttpPacket implements HttpRequest {

    private final Logger logger = LoggerFactory.getLogger(NettyHttpRequest.class.getName());

    private final NettyHttpSession session;

    private String method = "GET";

    private URI uri;

    private RequestBuilder builder;

    private Realm.RealmBuilder realmBuilder;

    private Request request;

    protected NettyHttpRequest(NettyHttpSession session) {
        this.session = session;
        this.realmBuilder = new Realm.RealmBuilder();
    }

    @Override
    public NettyHttpRequest setURL(URI uri) {
        if (uri.getUserInfo() != null) {
            String[] userInfo = uri.getUserInfo().split(":");
            realmBuilder = realmBuilder.setPrincipal(userInfo[0]).setPassword(userInfo[1]).setUsePreemptiveAuth(true).setScheme(Realm.AuthScheme.BASIC);
        }
        String authority = uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
        try {
            this.uri = new URI(uri.getScheme(), authority, uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException ex) {
            // ignore
        }
        return this;
    }

    @Override
    public URI getURL() {
        return uri;
    }

    @Override
    public NettyHttpRequest setMethod(String method) {
        this.method = method;
        this.builder = new RequestBuilder(method);
        return this;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public NettyHttpRequest addParameter(String name, String value) {
        if (value != null && value.length() > 0 && builder != null) {
            builder.addQueryParameter(name, value);
        }
        return this;
    }

    @Override
    public NettyHttpRequest addHeader(String name, String value) {
        if (value != null && value.length() > 0 && builder != null) {
            builder.addHeader(name, value);
        }
        return this;
    }

    public NettyHttpRequest setBody(String body) {
        if (builder != null) {
            builder.setBody(body);
        }
        return this;
    }

    public NettyHttpRequest setUser(String user) {
        realmBuilder = realmBuilder.setPrincipal(user);
        return this;
    }

    public NettyHttpRequest setPassword(String password) {
        realmBuilder = realmBuilder.setPassword(password);
        return this;
    }

    @Override
    public PreparedHttpRequest prepare() throws IOException {
        if (uri == null) {
            throw new IOException("no URL set");
        }
        if (request == null) {
            this.request = builder
                    .setUrl(uri.toASCIIString())
                    .setRealm(realmBuilder.build())
                    .build();
            logger.debug("prepared " + toString());
        }
        return session.prepare(this);
    }

    protected Request getRequest() {
        return request;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[method=").append(method).append("]")
                .append("[uri=").append(uri).append("]")
                .append("[parameter=").append(builder
                .setUrl(uri.toASCIIString())
                .setRealm(realmBuilder.build())
                .build()).append("]");
        return sb.toString();
    }

}
