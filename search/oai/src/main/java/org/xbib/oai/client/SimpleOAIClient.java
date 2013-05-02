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
import java.util.concurrent.TimeUnit;

import org.xbib.io.Session;
import org.xbib.io.http.netty.FatalException;
import org.xbib.io.http.netty.ForbiddenException;
import org.xbib.io.http.netty.HttpResponse;
import org.xbib.io.http.netty.HttpSession;
import org.xbib.date.DateUtil;
import org.xbib.io.http.netty.NotFoundException;
import org.xbib.oai.GetRecordRequest;
import org.xbib.oai.IdentifyRequest;
import org.xbib.oai.IdentifyResponse;
import org.xbib.oai.ListIdentifiersRequest;
import org.xbib.oai.ListMetadataFormatsRequest;
import org.xbib.oai.ListRecordsRequest;
import org.xbib.oai.ListRecordsResponse;
import org.xbib.oai.ListSetsRequest;
import org.xbib.oai.ListSetsResponse;
import org.xbib.oai.MetadataReader;
import org.xbib.oai.OAI;
import org.xbib.oai.OAIRequest;
import org.xbib.oai.OAIResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class SimpleOAIClient implements OAIClient {

    private HttpSession session;
    private AbstractResponseListener listener;
    private OAIResponse response;
    private StylesheetTransformer transformer;
    private URI uri;
    private String proxyhost;
    private int proxyport;
    private MetadataReader metadataReader;
    private int timeout;

    @Override
    public SimpleOAIClient setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public SimpleOAIClient setProxy(String host, int port) {
        this.proxyhost = host;
        this.proxyport = port;
        return this;
    }

    @Override
    public SimpleOAIClient setTimeout(long millis) {
        this.timeout = (int) millis;
        return this;
    }

    @Override
    public SimpleOAIClient setStylesheetTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public SimpleOAIClient setMetadataReader(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
        return this;
    }

    @Override
    public SimpleOAIClient prepareIdentify(IdentifyRequest request, IdentifyResponse response)
            throws IOException {
        open();
        session.add(new OAIRequest(uri).addParameter(OAI.VERB_PARAMETER, "Identify"));
        listener = new IdentifyResponseListener(request, response, transformer);
        return this;
    }

    @Override
    public SimpleOAIClient prepareListIdentifiers(ListIdentifiersRequest request, OAIResponse response)
            throws IOException {
        open();
        OAIRequest oai = new OAIRequest(uri).addParameter(OAI.VERB_PARAMETER, "ListIdentifiers");
        session.add(oai);
        return this;
    }

    @Override
    public SimpleOAIClient prepareListMetadataFormats(ListMetadataFormatsRequest request, OAIResponse response)
            throws IOException {
        open();
        OAIRequest oai = new OAIRequest(uri).addParameter(OAI.VERB_PARAMETER, "ListMetadataFormats");
        session.add(oai);
        return this;
    }

    @Override
    public SimpleOAIClient prepareListSets(ListSetsRequest request, ListSetsResponse response)
            throws IOException {
        open();
        final OAIRequest oai = request.getResumptionToken() != null
                ? new OAIRequest(uri).addParameter(OAI.RESUMPTION_TOKEN_PARAMETER, request.getResumptionToken().toString())
                : new OAIRequest(uri);
        oai.addParameter(OAI.VERB_PARAMETER, "ListSets");
        session.add(oai);
        ListSetsResponseListener p = new ListSetsResponseListener(request, response, transformer);
        this.listener = p;
        return this;
    }

    @Override
    public SimpleOAIClient prepareListRecords(ListRecordsRequest request, ListRecordsResponse response)
            throws IOException {
        open();
        final OAIRequest oai = request.getResumptionToken() != null
                ? new OAIRequest(uri).addParameter(OAI.RESUMPTION_TOKEN_PARAMETER, request.getResumptionToken().toString())
                : new OAIRequest(uri).addParameter(OAI.METADATA_PREFIX_PARAMETER, request.getMetadataPrefix()).addParameter(OAI.SET_PARAMETER, request.getSet()).addParameter(OAI.FROM_PARAMETER, DateUtil.formatDateISO(request.getFrom())).addParameter(OAI.UNTIL_PARAMETER, DateUtil.formatDateISO(request.getUntil()));
        oai.addParameter(OAI.VERB_PARAMETER, "ListRecords");
        oai.setProxy(proxyhost, proxyport);
        oai.setTimeout(timeout);
        session.add(oai);
        ListRecordsResponseListener p = new ListRecordsResponseListener(request, response, transformer);
        p.setMetadataReader(metadataReader);
        this.listener = p;
        return this;
    }

    @Override
    public SimpleOAIClient prepareGetRecord(GetRecordRequest request, OAIResponse response)
            throws IOException {
        open();
        OAIRequest oai = new OAIRequest(uri).addParameter(OAI.VERB_PARAMETER, "GetRecord");
        session.add(oai);
        // TODO
        return this;
    }

    @Override
    public void execute() throws IOException {
        execute(30, TimeUnit.SECONDS);
    }

    @Override
    public void execute(long l, TimeUnit tu) throws IOException {
        if (session != null && session.isOpen()) {
            if (listener != null) {
                session.addListener(listener);
            }
            session.execute(l, tu);
            if (listener != null) {
                session.removeListener(listener);
            }
            HttpResponse response = session.getResult(uri);
            if (response != null) {
                if (response.notfound()) {
                    throw new NotFoundException(uri);
                }
                if (response.forbidden()) {
                    throw new ForbiddenException(uri);
                }
                if (response.fatal()) {
                    throw new FatalException(uri);
                }
            }
        } else throw new IOException("can't execute");
    }

    private void open() throws IOException {
        if (session != null) {
            return;
        }
        session = new HttpSession();
        session.open(Session.Mode.READ);
    }

    public void close() throws IOException {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public HttpResponse getResponse() {
        return session.getResult(uri);
    }
}
