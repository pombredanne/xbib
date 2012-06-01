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

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHandler.STATE;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpOperation {

    private static final Logger logger = Logger.getLogger(HttpOperation.class.getName());
    private HttpResultProcessor processor;
    private String encoding;
    private long millis;
    private Map<URI, AsyncHttpClient.BoundRequestBuilder> builders;
    private Map<URI, HttpResult> results;

    public HttpOperation() {
        this.builders = new HashMap();
        this.results = new HashMap();

    }

    public HttpOperation setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }

    public void addProcessor(HttpResultProcessor processor) {
        this.processor = processor;
    }

    public HttpOperation prepareExecution(HttpSession session) throws IOException {
        HttpRequest request;
        while ((request = session.nextRequest()) != null) {
            AsyncHttpClient client = request.buildClient();
            Request req = request.buildRequest();
            logger.log(Level.INFO, "method=[{0}] uri=[{1}] parameter=[{2}]",
                    new Object[]{request.getMethod(), request.getURI(), req.getQueryParams()});
            builders.put(request.getURI(), client.prepareRequest(req));
        }
        return this;
    }

    class Handler implements AsyncHandler<HttpResult> {

        private final HttpResult result = new HttpResult();
        private final StringBuilder sb = new StringBuilder();

        Handler(URI uri) {
            result.setURI(uri);
        }

        @Override
        public STATE onStatusReceived(HttpResponseStatus hrs) throws Exception {
            result.setStatusCode(hrs.getStatusCode());
            return STATE.CONTINUE;
        }

        @Override
        public STATE onHeadersReceived(HttpResponseHeaders hrh) throws Exception {
            result.setHeaders(hrh.getHeaders());
            return STATE.CONTINUE;
        }

        @Override
        public STATE onBodyPartReceived(HttpResponseBodyPart hrbp) throws Exception {
            sb.append(new String(hrbp.getBodyPartBytes(), encoding != null ? encoding : "UTF-8"));
            return STATE.CONTINUE;
        }

        @Override
        public void onThrowable(Throwable t) {
            result.setThrowable(t);
        }

        @Override
        public HttpResult onCompleted() throws Exception {
            result.setBody(sb.toString());
            return result;
        }
    }

    public synchronized void execute(long l, TimeUnit tu) throws IOException {
        long t0 = System.currentTimeMillis();
        List<ListenableFuture<HttpResult>> futures = new ArrayList();
        for (Map.Entry<URI, AsyncHttpClient.BoundRequestBuilder> me : builders.entrySet()) {
            futures.add((me.getValue().execute(new Handler(me.getKey()))));            
        }
        for (int i = 0; i < futures.size(); i++) {
            try {
                HttpResult r = futures.get(i).get(l, tu);
                results.put(r.getURI(), r);
                if (r != null) {
                    if (processor != null) {
                        if (r.getThrowable() != null) {
                            continue;
                        }
                        if (r.ok()) {
                            processor.process(r);
                        } else {
                            processor.processError(r);
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException | TimeoutException | IOException t) {
                logger.log(Level.SEVERE, t.getMessage(), t);
            }
        }
        long t1 = System.currentTimeMillis();
        this.millis = t1 - t0;
    }

    public void execute() throws IOException {
        execute(30L, TimeUnit.SECONDS);
    }

    public long getResponseMillis() {
        return millis;
    }

    public Map<URI, HttpResult> getResults() {
        return results;
    }

    public HttpResult getResult(URI uri) {
        return results != null && results.containsKey(uri) ? results.get(uri) : null;
    }

    public String getContentType(URI uri) {
        return results != null && results.containsKey(uri) ? results.get(uri).getHeaders().getFirstValue("Content-Type") : null;
    }
}
