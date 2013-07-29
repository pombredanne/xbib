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
import org.xbib.io.http.HttpSession;
import org.xbib.io.http.netty.DefaultHttpSession;
import org.xbib.io.http.HttpRequest;
import org.xbib.io.util.URIUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.SyntaxException;
import org.xbib.sru.Diagnostics;
import org.xbib.sru.SRUConstants;
import org.xbib.sru.SRURequest;
import org.xbib.sru.SRUResponse;
import org.xbib.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.sru.searchretrieve.SearchRetrieveResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * A default SRU client
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DefaultSRUClient implements SRUClient<SearchRetrieveRequest,SearchRetrieveResponse> {

    private final Logger logger = LoggerFactory.getLogger(DefaultSRUClient.class.getName());

    private final HttpSession session;

    private final URI uri;

    public DefaultSRUClient() throws IOException {
        this(null);
    }

    public DefaultSRUClient(URI uri) throws IOException {
        this.session = new DefaultHttpSession();
        session.open(Session.Mode.READ);
        this.uri = uri;
    }

    @Override
    public void close() throws IOException {
        session.close();
    }

    public URI getClientIdentifier() {
        return uri;
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

    @Override
    public SearchRetrieveRequest newSearchRetrieveRequest() {
        return new ClientSearchRetrieveRequest()
                .setVersion(getVersion())
                .setRecordPacking(getRecordPacking())
                .setRecordSchema(getRecordSchema());
    }

    @Override
    public SearchRetrieveResponse execute(String operation, SearchRetrieveRequest request)
            throws IOException {
        if (request == null) {
            throw new IOException("request not set");
        }
        if (request.getURI() == null) {
            throw new IOException("request URI not set");
        }
        SearchRetrieveResponse response = null;
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

    protected SearchRetrieveResponse searchRetrieve(SearchRetrieveRequest request)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        final SearchRetrieveResponse response = new SearchRetrieveResponse(request);

        HttpRequest req = session.newRequest()
                .setMethod("GET")
                .setURL(request.getURI())
                .addParameter(SRUConstants.OPERATION_PARAMETER, "searchRetrieve")
                .addParameter(SRUConstants.VERSION_PARAMETER, request.getVersion())
                .addParameter(SRUConstants.QUERY_PARAMETER, URIUtil.encode(request.getQuery(), Charset.forName("UTF-8")))
                .addParameter(SRUConstants.START_RECORD_PARAMETER, Integer.toString(request.getStartRecord()))
                .addParameter(SRUConstants.MAXIMUM_RECORDS_PARAMETER, Integer.toString(request.getMaximumRecords()));
        if (request.getRecordPacking() != null && !request.getRecordPacking().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_PACKING_PARAMETER, request.getRecordPacking());
        }
        if (request.getRecordSchema() != null && !request.getRecordSchema().isEmpty()) {
            req.addParameter(SRUConstants.RECORD_SCHEMA_PARAMETER, request.getRecordSchema());
        }
        req.prepare().execute(response).waitFor();
        return response;
    }

    /*
    public ExplainResponse explain(Explain explain)
            throws IOException, SyntaxException {
        HttpRequest req = session.newRequest("GET")
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


