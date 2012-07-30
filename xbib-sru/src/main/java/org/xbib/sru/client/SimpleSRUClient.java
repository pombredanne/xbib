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
package org.xbib.sru.client;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.concurrent.TimeUnit;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.xbib.io.Mode;
import org.xbib.io.http.netty.HttpOperation;
import org.xbib.io.http.netty.HttpRequest;
import org.xbib.io.http.netty.HttpResult;
import org.xbib.io.http.netty.HttpSession;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.ExplainResponse;
import org.xbib.sru.SRU;
import org.xbib.sru.SRUResultProcessor;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.sru.explain.Explain;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xbib.xml.XMLFilterReader;
import org.xml.sax.InputSource;

public class SimpleSRUClient implements SRUClient {

    private HttpSession session = new HttpSession();

    private StylesheetTransformer transformer;
    
    
    @Override
    public URI getURI() {
        return null;
    }
    
    @Override
    public String getVersion() {
        return null;        
    }

    @Override
    public String getRecordSchema() {
        return null;        
    }

    @Override
    public String getRecordPacking() {
        return null;        
    }

    @Override
    public String getEncoding() {
        return null;        
    }

    @Override
    public String getStylesheet() {
        return null;        
    }

    @Override
    public String getUsername() {
        return null;        
    }
    
    @Override
    public String getPassword() {
        return null;
    }
    
    @Override
    public void setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public HttpOperation explain(Explain explain, ExplainResponse response)
            throws IOException, SyntaxException {
        open();
        HttpRequest req = new HttpRequest("GET").setURI(explain.getURI()).addParameter(SRU.OPERATION_PARAMETER, "explain");
        session.addRequest(req);
        ExplainProcessor processor = new ExplainProcessor(explain, response);
        HttpOperation operation = new HttpOperation();
        operation.addProcessor(processor);
        operation.prepareExecution(session);
        operation.execute(30L, TimeUnit.SECONDS);
        return operation;
    }

    @Override
    public HttpOperation searchRetrieve(SearchRetrieve request, SearchRetrieveResponse response)
            throws IOException, SyntaxException {
        if (transformer == null) {
            throw new IOException("style sheet transformer not set");
        }

        // transport parameters into XSL transformer style sheets
        transformer.addParameter("version", request.getVersion());
        transformer.addParameter("operation", "searchRetrieve");
        transformer.addParameter("query", request.getQuery());
        transformer.addParameter("startRecord", request.getStartRecord());
        transformer.addParameter("maximumRecords", request.getMaximumRecords());
        transformer.addParameter("recordPacking", request.getRecordPacking());
        transformer.addParameter("recordSchema", request.getRecordSchema());
        transformer.addParameter("origin", request.getURI());

        response.setOrigin(request.getURI());
        
        open();
        HttpRequest req = new HttpRequest("GET").setURI(request.getURI())
                .addParameter(SRU.OPERATION_PARAMETER, "searchRetrieve")
                .addParameter(SRU.VERSION_PARAMETER, request.getVersion())
                .addParameter(SRU.QUERY_PARAMETER, request.getQuery())
                .addParameter(SRU.START_RECORD_PARAMETER, Integer.toString(request.getStartRecord()))
                .addParameter(SRU.MAXIMUM_RECORDS_PARAMETER, Integer.toString(request.getMaximumRecords()));
        req.setUser(request.getUsername()).setPassword(request.getPassword());
        if (request.getRecordPacking() != null && !request.getRecordPacking().isEmpty()) {
            req.addParameter(SRU.RECORD_PACKING_PARAMETER, request.getRecordPacking());
        }
        if (request.getRecordSchema() != null && !request.getRecordSchema().isEmpty()) {
            req.addParameter(SRU.RECORD_SCHEMA_PARAMETER, request.getRecordSchema());
        }
        /*if (getEncoding() != null && !getEncoding().isEmpty()) {
            req.setEncoding(getEncoding());
        }*/
        session.addRequest(req);        
        SearchRetrieveProcessor processor = new SearchRetrieveProcessor(request, response);
        HttpOperation operation = new HttpOperation();
        operation.addProcessor(processor);
        operation.prepareExecution(session); 
        operation.execute(30L, TimeUnit.SECONDS);
        return operation;
    }

    private class SearchRetrieveProcessor implements SRUResultProcessor {

        final private SearchRetrieve request;
        final private SearchRetrieveResponse response;

        SearchRetrieveProcessor(SearchRetrieve request, SearchRetrieveResponse response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void process(HttpResult result) throws IOException {
            if (result.getBody() == null) {
                throw new IOException("empty result");
            }
            try {
                XMLFilterReader reader = new SearchRetrieveFilterReader(request, response);
                InputSource source = new InputSource(new StringReader(Normalizer.normalize(result.getBody(), Form.NFC)));
                StreamResult target = response.getOutput() != null
                        ? new StreamResult(response.getOutput())
                        : new StreamResult(response.getWriter());
                transformer.setSource(reader, source).setTarget(target).apply();                
            } catch (TransformerException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void processError(HttpResult result) throws IOException {
            throw new IOException(result.getStatusCode() + " " + result.getBody());
        }
        
        @Override
        public void close() throws IOException {
            if (response.getOutput() != null) {
                response.getOutput().close();
            }
            if (response.getWriter() != null) {
                response.getWriter().close();
            }
        }
    }

    private class ExplainProcessor implements SRUResultProcessor {

        private final Explain request;
        private final ExplainResponse response;

        ExplainProcessor(Explain request, ExplainResponse response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void process(HttpResult result) throws IOException {
            if (result.getBody() == null) {
                throw new IOException("empty result");
            }
            try {
                XMLFilterReader reader = new ExplainFilterReader(request, response);
                InputSource source = new InputSource(new StringReader(result.getBody()));
                StreamResult target = response.getOutput() != null
                        ? new StreamResult(response.getOutput())
                        : new StreamResult(response.getWriter());
                transformer.setSource(reader, source).setTarget(target).apply();
            } catch (TransformerException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void processError(HttpResult result) throws IOException {
            throw new IOException(result.getStatusCode() + " " + result.getBody());
        }
                
        @Override
        public void close() throws IOException {
            if (response.getOutput() != null) {
                response.getOutput().close();
            }
            if (response.getWriter() != null) {
                response.getWriter().close();
            }
        }
    }

    private void open() throws IOException {
        if (session != null) {
            return;
        }
        session = new HttpSession();
        session.open(Mode.READ);
    }

    @Override
    public void close() throws IOException {
        if (session != null) {
            session.close();
            session = null;
        }
                /// ?
        //if (processor != null)    processor.close();

    }
}


