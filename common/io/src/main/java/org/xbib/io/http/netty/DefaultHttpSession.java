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

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import org.xbib.io.http.HttpPacket;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.HttpSession;
import org.xbib.io.http.PreparedHttpRequest;

import java.io.IOException;

/**
 * HTTP Session
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DefaultHttpSession implements HttpSession {

    private AsyncHttpClientConfig.Builder config;

    private AsyncHttpClient client;

    private boolean isOpen;

    @Override
    public HttpSession setProxy(String host, int port) {
        if (host != null) {
            config.setProxyServer(new ProxyServer(host, port));
        }
        return this;
    }

    @Override
    public HttpSession setTimeout(int millis) {
        if (millis > 0) {
            config.setRequestTimeoutInMs(millis);
        }
        return this;
    }

    @Override
    public HttpRequest newRequest() {
        return new DefaultHttpRequest(this);
    }

    protected PreparedHttpRequest prepare(DefaultHttpRequest request) throws IOException {
        if (!isOpen()) {
            open(Mode.READ);
        }
        return new DefaultPreparedHttpRequest(request, client.prepareRequest(request.getRequest()));
    }

    @Override
    public void open(Mode mode) throws IOException {
        if (!isOpen()) {
            this.config = new AsyncHttpClientConfig.Builder()
                    .setRequestTimeoutInMs(30000)
                    .setAllowPoolingConnection(true)
                    .setFollowRedirects(true)
                    .setMaxRequestRetry(1)
                    .setCompressionEnabled(true);
            this.client = new AsyncHttpClient(new NettyAsyncHttpProvider(config.build()));
            this.isOpen = true;
        }
    }

    @Override
    public void close() throws IOException {
        if (isOpen()) {
            this.isOpen = false;
            client.close();
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public HttpPacket newPacket() {
        return null;
    }

    @Override
    public HttpPacket read() throws IOException {
        return null;
    }

    @Override
    public void write(HttpPacket packet) throws IOException {
    }

}
