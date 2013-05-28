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
package org.xbib.sru.searchretrieve;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;

import org.xbib.io.OutputFormat;
import org.xbib.io.Request;
import org.xbib.io.http.netty.HttpResponse;
import org.xbib.io.http.netty.HttpResponseListener;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.AbstractSRUResponse;
import org.xbib.sru.SRUResponse;
import org.xbib.text.Normalizer;
import org.xbib.xml.XMLFilterReader;
import org.xml.sax.InputSource;

public class SearchRetrieveResponse extends AbstractSRUResponse
        implements SRUResponse, SearchRetrieveResponseListener, HttpResponseListener {

    private final Logger logger = LoggerFactory.getLogger(SearchRetrieveResponse.class.getName());

    private final SearchRetrieveRequest request;

    private OutputFormat format;

    private URI origin;

    private Collection<XMLEvent> events;

    private HttpResponse httpResponse;

    long t0;

    long t1;

    public SearchRetrieveResponse(SearchRetrieveRequest request) {
        super();
        this.request = request;
        this.t0 = System.currentTimeMillis();
    }

    public SearchRetrieveResponse(SearchRetrieveRequest request, Writer writer) {
        super(writer);
        this.request = request;
        this.t0 = System.currentTimeMillis();
    }

    public SearchRetrieveRequest getRequest() {
        return request;
    }

    @Override
    public SearchRetrieveResponse setOutputFormat(OutputFormat format) {
        this.format = format;
        return this;
    }

    public OutputFormat getOutputFormat() {
        return format;
    }

    @Override
    public void write() throws IOException {
    }

    public void receivedResponse(HttpResponse response) {
        this.httpResponse = response;
        this.t1 = System.currentTimeMillis();
    }

    @Override
    public void onConnect(Request request) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.onConnect(request);
        }
    }

    @Override
    public void onDisconnect(Request request) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.onDisconnect(request);
        }
    }

    @Override
    public void onError(Request request, int count, byte[] errorMessage) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.onError(request, count, errorMessage);
        }
    }

    @Override
    public void onReceive(Request request, int count, byte[] message) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.onReceive(request, count, message);
        }
    }


    @Override
    public void version(String version) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.version(version);
        }
    }

    @Override
    public void numberOfRecords(long numberOfRecords) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.numberOfRecords(numberOfRecords);
        }
    }
    
    @Override
    public void beginRecord() {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.beginRecord();
        }
    }

    @Override
    public void recordSchema(String recordSchema) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.recordSchema(recordSchema);
        }
    }

    @Override
    public void recordPacking(String recordPacking) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.recordPacking(recordPacking);
        }
    }

    @Override
    public void recordIdentifier(String recordIdentifier) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.recordIdentifier(recordIdentifier);
        }
    }
    @Override
    public void recordPosition(int recordPosition) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.recordPosition(recordPosition);
        }
    }

    @Override
    public void recordData(Collection<XMLEvent> record) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.recordData(record);
        }
    }

    @Override
    public void facetedResults(Collection<XMLEvent> record) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.facetedResults(record);
        }
    }

    @Override
    public void extraRecordData(Collection<XMLEvent> record) {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.extraRecordData(record);
        }
    }

    @Override
    public void endRecord() {
        for (SearchRetrieveResponseListener listener : this.request.getListeners()) {
            listener.endRecord();
        }
    }
    
    public SearchRetrieveResponse setOrigin(URI origin) {
        this.origin = origin;
        return this;
    }

    public URI getOrigin() {
        return origin;
    }
    
    public void setEvents(Collection<XMLEvent> events) {
        this.events = events;
    }
    
    public Collection<XMLEvent> getEvents() {
        return events;
    }

    @Override
    public SearchRetrieveResponse to(HttpServletResponse servletResponse) throws IOException {
        servletResponse.addHeader("X-SRU-version",
                request.getVersion());
        servletResponse.addHeader("X-SRU-recordSchema",
                request.getRecordSchema());
        servletResponse.addHeader("X-SRU-recordPacking",
                request.getRecordPacking());
        servletResponse.addHeader("X-SRU-origin",
                request.getURI() != null ? request.getURI().toASCIIString() : "undefined");
        servletResponse.setStatus(200);
        return to(servletResponse.getWriter());
    }

    @Override
    public SearchRetrieveResponse to(Writer writer) throws IOException {
        if (httpResponse == null) {
            return this;
        }
        // transport parameters into XSL transformer style sheets
        getTransformer().addParameter("version", request.getVersion());
        getTransformer().addParameter("operation", "searchRetrieve");
        getTransformer().addParameter("query", request.getQuery());
        getTransformer().addParameter("startRecord", request.getStartRecord());
        getTransformer().addParameter("maximumRecords", request.getMaximumRecords());
        getTransformer().addParameter("recordPacking", request.getRecordPacking());
        getTransformer().addParameter("recordSchema", request.getRecordSchema());

        try {
            XMLFilterReader reader = new SearchRetrieveFilterReader(request);
            InputSource source = new InputSource(new StringReader(Normalizer.normalize(httpResponse.getBody(), Normalizer.Form.C)));
            getTransformer().setSource(reader, source)
                    .setResult(writer);
            if (getStylesheets() != null) {
                getTransformer().transform(Arrays.asList(getStylesheets()));
            } else {
                getTransformer().transform();
            }
        } catch (TransformerException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        } finally {
            logger.info("[{}ms] [uri={}] [status={}] [contenttype={}] [query={}]",
                    t1-t0,
                    request.getURI().toString(),
                    httpResponse.getStatusCode(),
                    httpResponse.getURI(),
                    request.getQuery());
        }
        return this;
    }
}

