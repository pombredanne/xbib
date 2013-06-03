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

import org.xbib.io.Session;
import org.xbib.io.http.netty.DefaultHttpResponseListener;
import org.xbib.io.http.netty.DefaultHttpSession;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.http.HttpResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUConstants;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;
import org.xbib.sru.service.SRUService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * A default SRU client
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DefaultSRUClient implements SRUClient {

    private final Logger logger = LoggerFactory.getLogger(DefaultSRUClient.class.getName());

    private SRUService service;

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
        return "2.0";
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
        SearchRetrieveResponse response = null;
        if (request.getRecordSchema() != null && !service.getRecordSchema().equals(request.getRecordSchema())) {
            throw new Diagnostics(66, request.getRecordSchema() + " != " + service.getRecordSchema());
        }
        if (request.getRecordPacking() != null && !service.getRecordPacking().equals(request.getRecordPacking())) {
            throw new Diagnostics(6, request.getRecordPacking() + " != " + service.getRecordPacking());
        }
        try {
            response = searchRetrieve(request);
        } catch (SyntaxException e) {
            logger.error("CQL syntax error", e);
            throw new Diagnostics(10, e.getMessage());
        } catch (Exception e) {
            logger.error("SRU is unresponsive", e);
            throw new Diagnostics(1, e.getMessage());
        }
        return response;
    }

    @Override
    public void close() throws IOException {
    }

    protected SearchRetrieveResponse searchRetrieve(SearchRetrieveRequest request)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        final SearchRetrieveResponse response = new SearchRetrieveResponse(request);
        DefaultHttpSession session = new DefaultHttpSession();
        session.open(Session.Mode.READ);
        HttpRequest req = session.newRequest()
                .setMethod("GET")
                .setURI(request.getURI())
                .setUser(username)
                .setPassword(password)
                .addParameter(SRUConstants.OPERATION_PARAMETER, "searchRetrieve")
                .addParameter(SRUConstants.VERSION_PARAMETER, request.getVersion())
                .addParameter(SRUConstants.QUERY_PARAMETER, request.getQuery())
                .addParameter(SRUConstants.START_RECORD_PARAMETER, Integer.toString(request.getStartRecord()))
                .addParameter(SRUConstants.MAXIMUM_RECORDS_PARAMETER, Integer.toString(request.getMaximumRecords()));
        if (request.getRecordPacking() != null && !request.getRecordPacking().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_PACKING_PARAMETER, request.getRecordPacking());
        }
        if (request.getRecordSchema() != null && !request.getRecordSchema().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_SCHEMA_PARAMETER, request.getRecordSchema());
        }
        req.prepare().execute(new DefaultHttpResponseListener() {
            @Override
            public void receivedResponse(HttpResponse result) {
                response.receivedResponse(result);
            }
        }).waitFor();
        return response;
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


