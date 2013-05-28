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
package org.xbib.sru;

import org.xbib.io.Session;
import org.xbib.io.http.netty.HttpRequest;
import org.xbib.io.http.netty.HttpResponse;
import org.xbib.io.http.netty.HttpResponseListener;
import org.xbib.io.http.netty.HttpSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;

import java.io.IOException;
import java.net.URI;

public class DefaultSRUClient implements SRUClient {

    private final Logger logger = LoggerFactory.getLogger(DefaultSRUClient.class.getName());

    private SRUService service;

    private URI uri;

    private String username;

    private String password;

    public DefaultSRUClient(SRUService service) {
        this.service = service;
    }

    protected SRUService getService() {
        return service;
    }

    @Override
    public String getRecordSchema() {
        return "mods";
    }

    @Override
    public String getRecordPacking() {
        return "xml";
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }

    @Override
    public String getVersion() {
        return "1.2";
    }

    public DefaultSRUClient setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri == null ? URI.create("http://localhost") : uri;
    }

    public DefaultSRUClient setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public DefaultSRUClient setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public SearchRetrieveRequest newSearchRetrieveRequest() {
        return new ClientSearchRetrieveRequest()
                .setURI(getURI())
                .setVersion(getVersion())
                .setRecordPacking(getRecordPacking())
                .setRecordSchema(getRecordSchema());
    }

    @Override
    public SearchRetrieveResponse execute(SearchRetrieveRequest request)
            throws IOException {
        if (request == null) {
            throw new IOException("request not set");
        }
        if (request.getURI() == null) {
            throw new IOException("request URI not set");
        }
        SearchRetrieveResponse response = newSearchRetrieveResponse(request);
        if (request.getRecordSchema() != null && !service.getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema());
        }
        if (request.getRecordPacking() != null && !service.getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking());
        }
        try {
            searchRetrieve(request, response);
        } catch (SyntaxException e) {
            logger.error("CQL syntax error", e);
            throw new Diagnostics(10, e.getMessage());
        } catch (IOException e) {
            logger.error("SRU is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        }
        return response;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public SearchRetrieveResponse newSearchRetrieveResponse(SearchRetrieveRequest request) {
        return new SearchRetrieveResponse(request);
    }

    protected void searchRetrieve(final SearchRetrieveRequest request, final SearchRetrieveResponse response) throws IOException {
        HttpRequest req = new HttpRequest("GET")
                .setURI(request.getURI())
                .addParameter(SRUConstants.OPERATION_PARAMETER, "searchRetrieve")
                .addParameter(SRUConstants.VERSION_PARAMETER, request.getVersion())
                .addParameter(SRUConstants.QUERY_PARAMETER, request.getQuery())
                .addParameter(SRUConstants.START_RECORD_PARAMETER, Integer.toString(request.getStartRecord()))
                .addParameter(SRUConstants.MAXIMUM_RECORDS_PARAMETER, Integer.toString(request.getMaximumRecords()));
        req.setUser(username).setPassword(password);
        if (request.getRecordPacking() != null && !request.getRecordPacking().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_PACKING_PARAMETER, request.getRecordPacking());
        }
        if (request.getRecordSchema() != null && !request.getRecordSchema().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_SCHEMA_PARAMETER, request.getRecordSchema());
        }
        HttpSession session = new HttpSession();
        session.open(Session.Mode.READ);
        session.add(req);
        HttpResponseListener listener = new HttpResponseListener() {
            @Override
            public void receivedResponse(HttpResponse result) {
                response.receivedResponse(result);
            }
        };
        session.addListener(listener);
        session.execute();
        session.close();
    }

    /*
    public ExplainResponse explain(Explain explain)
            throws IOException, SyntaxException {
        HttpSession session = new HttpSession();
        session.open(Session.Mode.READ);
        HttpRequest req = new HttpRequest("GET")
                .setURI(explain.getURI())
                .addParameter(SRU.OPERATION_PARAMETER, "explain");
        session.add(req);
        ExplainListener listener = new ExplainListener();
        session.addListener(listener);
        session.execute();
        // build explainresponse here
        session.removeListener(listener);
        session.close();
        return null;
    }*/

    class ClientSearchRetrieveRequest extends SearchRetrieveRequest {

    }

}


