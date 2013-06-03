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
package org.xbib.oai.client;

import java.io.IOException;
import java.net.URI;

import org.xbib.io.http.netty.DefaultHttpSession;
import org.xbib.io.http.HttpResponse;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.OAIConstants;
import org.xbib.oai.identify.IdentifyResponse;
import org.xbib.oai.identify.ListIdentifiersResponse;
import org.xbib.oai.metadata.ListMetadataFormatsResponse;
import org.xbib.oai.record.GetRecordRequest;
import org.xbib.oai.identify.IdentifyRequest;
import org.xbib.oai.identify.ListIdentifiersRequest;
import org.xbib.oai.metadata.ListMetadataFormatsRequest;
import org.xbib.oai.record.GetRecordResponse;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponse;
import org.xbib.oai.set.ListSetsRequest;
import org.xbib.oai.set.ListSetsResponse;
import org.xbib.oai.util.ResumptionToken;

/**
 * Default OAI client
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class DefaultOAIClient extends DefaultHttpSession implements OAIClient {

    private final Logger logger = LoggerFactory.getLogger(DefaultOAIClient.class.getName());

    private URI uri;

    private String username;

    private String password;

    private String proxyhost;

    private int proxyport;

    private int timeout;

    private HttpResponse response;

    @Override
    public DefaultOAIClient setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public DefaultOAIClient setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public DefaultOAIClient setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public DefaultOAIClient setProxy(String host, int port) {
        this.proxyhost = host;
        this.proxyport = port;
        return this;
    }

    @Override
    public DefaultOAIClient setTimeout(long millis) {
        this.timeout = (int) millis;
        return this;
    }

    @Override
    public DefaultHttpSession getSession() {
        return this;
    }

    @Override
    public IdentifyRequest newIdentifyRequest() {
        ensureOpen();
        return new IdentifyRequest(this);
    }

    @Override
    public IdentifyRequest resume(IdentifyRequest request, IdentifyResponse response) {
        if (response == null) {
            return null;
        }
        ensureOpen();
        if (response == null) {
            return request;
        }
        ResumptionToken token = response.getRequest().getResumptionToken();
        if (token != null) {
            request.addParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER, token.toString());
        }
        return request;
    }

    @Override
    public ListRecordsRequest newListRecordsRequest() {
        ensureOpen();
        return new ListRecordsRequest(this);
    }

    @Override
    public ListRecordsRequest resume(ListRecordsRequest request, ListRecordsResponse response) {
        if (response == null) {
            return null;
        }
        ensureOpen();
        ResumptionToken token = response.getRequest().getResumptionToken();
        if (token != null) {
            request.addParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER, token.toString());
        }
        return request;
    }

    @Override
    public ListIdentifiersRequest newListIdentifiersRequest() {
        ensureOpen();
        return new ListIdentifiersRequest(this);
    }

    @Override
    public ListIdentifiersRequest resume(ListIdentifiersRequest request, ListIdentifiersResponse response) {
        if (response == null) {
            return null;
        }
        ensureOpen();
        ResumptionToken token = response.getRequest().getResumptionToken();
        if (token != null) {
            request.addParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER, token.toString());
        }
        return request;
    }

    @Override
    public ListMetadataFormatsRequest newListMetadataFormatsRequest() {
        ensureOpen();
        return new ListMetadataFormatsRequest(this);
    }

    @Override
    public ListMetadataFormatsRequest resume(ListMetadataFormatsRequest request, ListMetadataFormatsResponse response) {
        if (response == null) {
            return null;
        }
        ensureOpen();
        ResumptionToken token = response.getRequest().getResumptionToken();
        if (token != null) {
            request.addParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER, token.toString());
        }
        return request;
    }

    @Override
    public ListSetsRequest newListSetsRequest() {
        ensureOpen();
        return new ListSetsRequest(this);
    }

    @Override
    public ListSetsRequest resume(ListSetsRequest request, ListSetsResponse response) {
        if (response == null) {
            return null;
        }
        ensureOpen();
        ResumptionToken token = response.getRequest().getResumptionToken();
        if (token != null) {
            request.addParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER, token.toString());
        }
        return request;
    }

    @Override
    public GetRecordRequest newGetRecordRequest() {
        ensureOpen();
        return new GetRecordRequest(this);
    }

    @Override
    public GetRecordRequest resume(GetRecordRequest request, GetRecordResponse response) {
        if (response == null) {
            return null;
        }
        ensureOpen();
        ResumptionToken token = response.getRequest().getResumptionToken();
        if (token != null) {
            request.addParameter(OAIConstants.RESUMPTION_TOKEN_PARAMETER, token.toString());
        }
        return request;
    }

    private void ensureOpen() {
        if (getURI() == null) {
            throw new IllegalArgumentException("no URL set for session");
        }
        if (!isOpen()) {
            try {
                open(Mode.READ);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
